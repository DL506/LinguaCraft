package com.linguacraft.backend.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AiDraftDto;
import com.linguacraft.backend.dto.practice.PracticeItemResponse;
import com.linguacraft.backend.dto.practice.SourceDto;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.service.QuestionContentValidator;
import com.linguacraft.backend.util.PracticeRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 出题服务(Spring AI):点即生成、无参数面板(任务书 10.6)。
 *
 * <p>流程:调用模型 → 字段结构校验 → 拆分"下发内容 / 答案"双 JSON → 五道入库校验(与后台题目录入
 * 共用 {@link QuestionContentValidator})→ 持久化到题库(source=ai,题目可被详情/提交/错题重做全链路复用)
 * → 返回 ForAnswer。任一步不合格都<b>不落库</b>,带原因重试一次,仍失败则返回可重试错误。</p>
 *
 * <p>两个出口共用上面这条链路,区别只在最后一步:
 * {@link #generate(String)} 是学生端"点即生成",生成即落库且默认上架;
 * {@link #generateDraft(String)} 是后台录入页的"AI 生成一版"(阶段 A9),<b>只返草稿不写库</b>,
 * 落库与上架由管理员点保存触发。写 {@code question} 内容的 AI 一律要过人,这是本项目定的边界。</p>
 */
@Service
public class AiGenerateService {

    private static final Logger log = LoggerFactory.getLogger(AiGenerateService.class);

    /** 出题最大尝试次数(结构校验失败时重试) */
    private static final int MAX_ATTEMPTS = 2;

    private final AiChatService aiChatService;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;
    private final QuestionContentValidator questionContentValidator;

    /** 生成模型名(写入 source.meta.model) */
    @Value("${spring.ai.openai.chat.options.model:fake-model}")
    private String modelName;

    public AiGenerateService(AiChatService aiChatService,
                             QuestionMapper questionMapper,
                             ObjectMapper objectMapper,
                             QuestionContentValidator questionContentValidator) {
        this.aiChatService = aiChatService;
        this.questionMapper = questionMapper;
        this.objectMapper = objectMapper;
        this.questionContentValidator = questionContentValidator;
    }

    /**
     * 通过五道校验的题草稿。
     *
     * @param raw       模型原始返回(标题/难度/标签仍从这里取)
     * @param content   下发内容(不含答案)
     * @param answerKey 答案与解析
     */
    private record Draft(JsonNode raw, ObjectNode content, ObjectNode answerKey) {
    }

    /**
     * 生成一道题目并持久化。
     *
     * @param type 题型(五种之一)
     * @return 下发的题目(ForAnswer,不含答案)
     * @throws BizException 题型非法(1004)或生成失败(code 500,前端可重试)
     */
    public PracticeItemResponse generate(String type) {
        if (!PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        Question question = buildAndPersist(type, callWithRetry(type));
        log.info("AI 出题完成: id={} type={} title={}", question.getId(), type, question.getTitle());
        return toResponse(question);
    }

    /**
     * 只生成草稿,不落库(阶段 A9,后台录入页「AI 生成一版」)。
     *
     * <p>与学生端 {@link #generate(String)} 的关系:共用同一条「调用模型 → 浅层结构校验 →
     * 拆分双 JSON → 五道闸门」链路和同样的重试次数,唯一区别是<b>到此为止不写库</b>。
     * 因此草稿与已入库题目经过的是同一个质量闸门,不存在"AI 草稿可以绕过校验"的口子。</p>
     *
     * <p>返回的 content 不含 {@code id/type/source}:这三个字段由服务端在保存时按最终题型与来源覆写,
     * 草稿阶段编一个 id 只会误导管理员去改一个马上会被覆盖的值。</p>
     *
     * @param type 题型(五种之一)
     * @return 已通过闸门校验的草稿
     * @throws BizException 题型非法(1004)或生成/校验失败(code 500,文案以「AI 出题结果不合格:」开头)
     */
    public AiDraftDto generateDraft(String type) {
        if (!PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        Draft draft = callWithRetry(type);
        JsonNode json = draft.raw();
        log.info("AI 草稿已生成(未入库): type={} title={}", type, json.path("title").asText(""));
        return AiDraftDto.builder()
                .type(type)
                .title(resolveTitle(json, type))
                .difficulty(resolveDifficulty(json))
                .tags(collectTags(json))
                .content(draft.content())
                .answerKey(draft.answerKey())
                .sourceMeta(buildAiMeta())
                .build();
    }

    /**
     * 调用模型并过五道校验闸门;不合格则回喂原因重试,最终失败抛可重试错误。
     *
     * <p>闸门与后台题目录入(A2)共用 {@link QuestionContentValidator},因此"AI 生成的题"
     * 与"人工录入的题"入库标准一致:占位符与空位编号不齐、答案不在候选项内等缺陷
     * 不会静默落库成脏题。</p>
     *
     * @param type 题型
     * @return 通过校验的草稿
     */
    private Draft callWithRetry(String type) {
        BizException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                JsonNode json = aiChatService.chatForJson(PromptTemplates.generatePrompt(type));
                validateStructure(json, type);
                return buildDraft(type, json);
            } catch (BizException e) {
                lastError = e;
                log.warn("AI 出题第 {} 次尝试失败: {}", attempt, e.getMessage());
            }
        }
        throw lastError != null ? lastError
                : new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 出题失败,请重试");
    }

    /**
     * 拆分双 JSON 列并执行五道校验(只判定,不覆写权威字段)。
     *
     * @param type 题型
     * @param json 模型返回
     * @return 已通过校验的草稿
     */
    private Draft buildDraft(String type, JsonNode json) {
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode answerKey = objectMapper.createObjectNode();
        buildContentByType(type, json, content, answerKey);
        try {
            questionContentValidator.validate(type, content, answerKey);
        } catch (BizException e) {
            // 换成可重试语义:校验器抛的是 400(面向录入),但模型产出缺陷应当重试而不是让前端当作参数错误
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 出题结果不合格:" + e.getMessage());
        }
        return new Draft(json, content, answerKey);
    }

    /**
     * 结构校验:按题型检查关键字段(失败抛 500,触发重试)。
     *
     * @param json AI 返回
     * @param type 题型
     */
    private void validateStructure(JsonNode json, String type) {
        switch (type) {
            case "reading" -> {
                require(PromptTemplates.hasField(json, "passage"), "缺少文章");
                requireArray(json, "questions");
                for (JsonNode item : json.get("questions")) {
                    require(PromptTemplates.hasField(item, "stem"), "缺少题干");
                    requireArray(item, "options");
                    require(PromptTemplates.hasField(item, "answer"), "缺少答案");
                }
            }
            case "matching" -> {
                require(PromptTemplates.hasField(json, "passage"), "缺少文章");
                requireArray(json, "options");
                requireArray(json, "blanks");
                requireBlanksAnswers(json.get("blanks"));
            }
            case "cloze" -> {
                require(PromptTemplates.hasField(json, "passage"), "缺少文章");
                requireArray(json, "blanks");
                for (JsonNode item : json.get("blanks")) {
                    requireArray(item, "options");
                    require(PromptTemplates.hasField(item, "answer"), "缺少答案");
                }
            }
            case "grammar-fill" -> {
                require(PromptTemplates.hasField(json, "passage"), "缺少文章");
                requireArray(json, "blanks");
                requireBlanksAnswers(json.get("blanks"));
            }
            case "writing" -> {
                require(PromptTemplates.hasField(json, "prompt"), "缺少写作题目");
                requireArray(json, "scoringDimensions");
            }
            default -> throw new BizException(ResultCode.TYPE_INVALID);
        }
    }

    /**
     * 校验 blanks 数组每项含答案。
     *
     * @param blanks 空位数组
     */
    private void requireBlanksAnswers(JsonNode blanks) {
        for (JsonNode item : blanks) {
            require(PromptTemplates.hasField(item, "answer"), "缺少空位答案");
        }
    }

    /**
     * 非空校验。
     *
     * @param condition 条件
     * @param message   异常信息
     */
    private void require(boolean condition, String message) {
        if (!condition) {
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 出题结果结构异常:" + message);
        }
    }

    /**
     * 数组字段非空校验。
     *
     * @param node  JSON 节点
     * @param field 字段名
     */
    private void requireArray(JsonNode node, String field) {
        require(node != null && node.get(field) != null && node.get(field).isArray() && !node.get(field).isEmpty(),
                "缺少字段 " + field);
    }

    /**
     * 构建题目实体并持久化(拆分双 JSON 列)。
     *
     * <p>入参草稿已通过五道校验,此处只补权威字段(id/type/source)与分值。</p>
     *
     * @param type  题型
     * @param draft 已通过校验的草稿
     * @return 题目实体
     */
    private Question buildAndPersist(String type, Draft draft) {
        JsonNode json = draft.raw();
        String questionId = type + "-ai-" + Long.toString(System.currentTimeMillis(), 36);

        ObjectNode content = draft.content();
        // source 只建一次,content 与 source_meta 复用同一个节点 —— 两处必须同源,否则学生端看到的来源与后台记录会分叉
        ObjectNode source = buildAiSource();
        content.put("id", questionId);
        content.put("type", type);
        content.set("source", source);

        Question question = new Question();
        question.setId(questionId);
        question.setType(type);
        question.setSourceType("ai");
        question.setSourceMeta(source.toString());
        question.setTitle(resolveTitle(json, type));
        question.setDifficulty(resolveDifficulty(json));
        question.setTags(buildTagsJson(json));
        question.setContent(content.toString());
        question.setAnswerKey(draft.answerKey().toString());
        question.setHasAnswer(!"writing".equals(type));
        question.setTotalScore(PracticeRules.totalScore(type));
        question.setPerQuestionScore(PracticeRules.perQuestionScore(type));
        question.setStatus(1);
        questionMapper.insert(question);
        return question;
    }

    /**
     * AI 来源的 meta 子对象 {@code {model, generatedAt}}。
     *
     * <p>草稿接口返回的就是这一层(而不是外层带 {@code type} 的完整 source),
     * 与题目详情 {@code AdminQuestionDetailDto.sourceMeta} 和录入请求
     * {@code QuestionUpsertRequest.sourceMeta} 同形 —— 前端拿到什么就能原样回填什么,
     * 不需要自己剥一层。</p>
     *
     * @return meta 节点
     */
    private ObjectNode buildAiMeta() {
        ObjectNode meta = objectMapper.createObjectNode();
        meta.put("model", modelName);
        meta.put("generatedAt", LocalDateTime.now().toString());
        return meta;
    }

    /**
     * 来源节点 {@code {type:"ai", meta:{model, generatedAt}}}(生成即记录,与草稿返回同构)。
     *
     * @return 新建的 source 节点(调用方按需 deepCopy 后使用)
     */
    private ObjectNode buildAiSource() {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("type", "ai");
        source.set("meta", buildAiMeta());
        return source;
    }

    /**
     * 标题:AI 给了就用,否则按题型与时间兜底(与录入服务的"留空自动生成"同口径)。
     *
     * @param json AI 返回
     * @param type 题型
     * @return 标题
     */
    private String resolveTitle(JsonNode json, String type) {
        String title = json.path("title").asText("");
        return title.isBlank()
                ? "AI出题·" + PracticeRules.typeName(type) + " · "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                : title;
    }

    /**
     * 难度:夹到 1~5。
     *
     * @param json AI 返回
     * @return 难度
     */
    private int resolveDifficulty(JsonNode json) {
        return Math.max(1, Math.min(5, json.path("difficulty").asInt(3)));
    }

    /**
     * 按题型拆分:content(ForAnswer)+ answerKey(答案),题号/空位按数组顺序重排为 1..n。
     *
     * @param type      题型
     * @param json      AI 返回
     * @param content   下发内容节点(就地填充)
     * @param answerKey 答案节点(就地填充)
     */
    private void buildContentByType(String type, JsonNode json, ObjectNode content, ObjectNode answerKey) {
        switch (type) {
            case "reading" -> {
                content.put("passage", json.path("passage").asText(""));
                ArrayNode questions = content.putArray("questions");
                ArrayNode keyItems = answerKey.putArray("questions");
                int index = 1;
                for (JsonNode item : json.get("questions")) {
                    ObjectNode question = questions.addObject();
                    question.put("id", index);
                    question.put("stem", item.path("stem").asText(""));
                    copyOptions(item.path("options"), question.putArray("options"));
                    ObjectNode key = keyItems.addObject();
                    key.put("id", index);
                    key.put("answer", item.path("answer").asText(""));
                    key.put("explanation", item.path("explanation").asText(""));
                    index++;
                }
            }
            case "matching" -> {
                content.put("passage", json.path("passage").asText(""));
                copyOptions(json.path("options"), content.putArray("options"));
                ArrayNode blanks = content.putArray("blanks");
                ArrayNode keyItems = answerKey.putArray("blanks");
                int index = 1;
                for (JsonNode item : json.get("blanks")) {
                    blanks.addObject().put("id", index);
                    ObjectNode key = keyItems.addObject();
                    key.put("id", index);
                    key.put("answer", item.path("answer").asText(""));
                    key.put("explanation", item.path("explanation").asText(""));
                    index++;
                }
            }
            case "cloze" -> {
                content.put("passage", json.path("passage").asText(""));
                ArrayNode blanks = content.putArray("blanks");
                ArrayNode keyItems = answerKey.putArray("blanks");
                int index = 1;
                for (JsonNode item : json.get("blanks")) {
                    ObjectNode blank = blanks.addObject();
                    blank.put("id", index);
                    copyOptions(item.path("options"), blank.putArray("options"));
                    ObjectNode key = keyItems.addObject();
                    key.put("id", index);
                    key.put("answer", item.path("answer").asText(""));
                    key.put("explanation", item.path("explanation").asText(""));
                    index++;
                }
            }
            case "grammar-fill" -> {
                content.put("passage", json.path("passage").asText(""));
                ArrayNode blanks = content.putArray("blanks");
                ArrayNode keyItems = answerKey.putArray("blanks");
                int index = 1;
                for (JsonNode item : json.get("blanks")) {
                    ObjectNode blank = blanks.addObject();
                    blank.put("id", index);
                    if (item.hasNonNull("hint") && !item.get("hint").asText("").isBlank()) {
                        blank.put("hint", item.get("hint").asText(""));
                    }
                    ObjectNode key = keyItems.addObject();
                    key.put("id", index);
                    key.put("answer", item.path("answer").asText(""));
                    if (item.has("acceptAlternatives") && item.get("acceptAlternatives").isArray()) {
                        ArrayNode alternatives = key.putArray("acceptAlternatives");
                        for (JsonNode alternative : item.get("acceptAlternatives")) {
                            alternatives.add(alternative.asText(""));
                        }
                    }
                    key.put("explanation", item.path("explanation").asText(""));
                    index++;
                }
            }
            case "writing" -> {
                content.put("prompt", json.path("prompt").asText(""));
                ArrayNode requirements = content.putArray("requirements");
                if (json.has("requirements") && json.get("requirements").isArray()) {
                    for (JsonNode requirement : json.get("requirements")) {
                        requirements.add(requirement.asText(""));
                    }
                }
                content.put("wordLimit", json.path("wordLimit").asInt(120));
                ArrayNode dimensions = content.putArray("scoringDimensions");
                for (JsonNode dimension : json.get("scoringDimensions")) {
                    dimensions.add(dimension.asText(""));
                }
                answerKey.put("sampleAnswer", json.path("sampleAnswer").asText(""));
            }
            default -> throw new BizException(ResultCode.TYPE_INVALID);
        }
    }

    /**
     * 复制选项数组(保留 key/text)。
     *
     * @param sourceOptions AI 选项数组
     * @param target        目标数组
     */
    private void copyOptions(JsonNode sourceOptions, ArrayNode target) {
        for (JsonNode option : sourceOptions) {
            ObjectNode node = target.addObject();
            node.put("key", option.path("key").asText(""));
            node.put("text", option.path("text").asText(""));
        }
    }

    /**
     * 标签列表:取 AI 返回 tags(字符串数组,剔除空白项),缺省为 ["AI出题"]。
     *
     * @param json AI 返回
     * @return 标签列表(永不为空)
     */
    private List<String> collectTags(JsonNode json) {
        List<String> tags = new ArrayList<>();
        if (json.has("tags") && json.get("tags").isArray()) {
            for (JsonNode tag : json.get("tags")) {
                String value = tag.asText("");
                if (!value.isBlank()) {
                    tags.add(value);
                }
            }
        }
        if (tags.isEmpty()) {
            tags.add("AI出题");
        }
        return tags;
    }

    /**
     * 标签 JSON:取 AI 返回 tags(字符串数组),缺省为 ["AI出题"]。
     *
     * @param json AI 返回
     * @return 标签 JSON 字符串
     */
    private String buildTagsJson(JsonNode json) {
        try {
            return objectMapper.writeValueAsString(collectTags(json));
        } catch (Exception e) {
            return "[\"AI出题\"]";
        }
    }

    /**
     * 题目实体 -> 下发响应(ForAnswer)。
     *
     * @param question 题目实体
     * @return 下发响应
     */
    private PracticeItemResponse toResponse(Question question) {
        JsonNode sourceNode = objectMapper.valueToTree(null);
        try {
            sourceNode = objectMapper.readTree(question.getSourceMeta());
        } catch (Exception ignored) {
            // sourceMeta 为自身写入,解析失败概率极低
        }
        return PracticeItemResponse.builder()
                .id(question.getId())
                .type(question.getType())
                .source(SourceDto.builder()
                        .type("ai")
                        .meta(sourceNode == null ? null : sourceNode.get("meta"))
                        .build())
                .difficulty(question.getDifficulty())
                .tags(parseTags(question.getTags()))
                .content(readTree(question.getContent()))
                .build();
    }

    /**
     * 解析标签 JSON。
     *
     * @param tagsJson 标签 JSON
     * @return 标签列表
     */
    private List<String> parseTags(String tagsJson) {
        try {
            return objectMapper.readValue(tagsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of("AI出题");
        }
    }

    /**
     * 解析 JSON(容错)。
     *
     * @param json JSON 字符串
     * @return JsonNode 或 null
     */
    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }
}