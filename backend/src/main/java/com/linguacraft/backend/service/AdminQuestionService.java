package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.QuestionAdminDetailDto;
import com.linguacraft.backend.dto.admin.QuestionAdminListItemDto;
import com.linguacraft.backend.dto.admin.QuestionUpsertRequest;
import com.linguacraft.backend.entity.Mistake;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.util.PracticeRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理端题库服务:题目的查询、录入、更新、上下架与引用保护删除。
 *
 * <p>本服务是"答案不下发"不变量的<b>入口守卫</b>:管理员提交的 {@code content}(下发列)
 * 与 {@code answerKey}(答案列)在落库前必须通过五道校验,任何一道不过即 400 拒绝,
 * 从而保证学生端 GET 接口永远不可能读到答案:</p>
 * <ol>
 *   <li>结构校验:按题型检查 content 的关键字段(文章/题干/选项/空位等);</li>
 *   <li>答案防呆:content 的 JSON 树中递归禁止出现 answer/explanation/sampleAnswer/acceptAlternatives 键;</li>
 *   <li>id 集合一致:answerKey 的题号/空位编号集合必须与 content 完全相等(不多不少);</li>
 *   <li>答案合法:客观题的 answer 必须落在该题可选项 key 集合内(五选五还要求答案互不重复);</li>
 *   <li>占位符一致:passage 中的 {@code [[n]]} 编号集合必须与 blanks 的 id 集合相等。</li>
 * </ol>
 *
 * <p>另外三条写入约束:content 的 id/type/source 由服务端强制覆写为权威值;分值一律按题型默认映射
 * (不接受请求覆盖,避免与判分口径分叉);题型创建后不可变更(否则历史提交快照与题目结构会错位)。</p>
 */
@Service
public class AdminQuestionService {

    private static final Logger log = LoggerFactory.getLogger(AdminQuestionService.class);

    /** 合法来源类型(管理端可录入真题/模拟题,也可上架 AI 生成的题) */
    private static final Set<String> VALID_SOURCES = Set.of("real", "teacher", "ai");

    /** content 中禁止出现的键(答案与解析只允许存在于 answer_key 列) */
    private static final Set<String> FORBIDDEN_KEYS = Set.of("answer", "explanation", "sampleAnswer", "acceptAlternatives");

    /** passage 中的空位占位符 [[n]] */
    private static final Pattern BLANK_MARK = Pattern.compile("\\[\\[(\\d+)]]");

    /** 默认标题与生成 ID 使用的时间格式 */
    private static final DateTimeFormatter TIME_STAMP = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final DateTimeFormatter TITLE_STAMP = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final QuestionMapper questionMapper;
    private final PracticeSubmissionMapper submissionMapper;
    private final MistakeMapper mistakeMapper;
    private final ObjectMapper objectMapper;

    public AdminQuestionService(QuestionMapper questionMapper,
                                PracticeSubmissionMapper submissionMapper,
                                MistakeMapper mistakeMapper,
                                ObjectMapper objectMapper) {
        this.questionMapper = questionMapper;
        this.submissionMapper = submissionMapper;
        this.mistakeMapper = mistakeMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 查询 ====================

    /**
     * 题目分页列表(GET /api/admin/questions)。
     *
     * @param type       题型筛选(可空)
     * @param sourceType 来源筛选(可空)
     * @param status     上架状态筛选(可空;0 下架 1 上架)
     * @param keyword    标题/ID 模糊关键词(可空)
     * @param page       页码(从 1 开始)
     * @param size       每页条数(1~100)
     * @return 分页结果(含被提交引用数)
     */
    public PageResult<QuestionAdminListItemDto> list(String type, String sourceType, Integer status,
                                                     String keyword, long page, long size) {
        if (type != null && !type.isEmpty() && !PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        if (sourceType != null && !sourceType.isEmpty() && !VALID_SOURCES.contains(sourceType)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "sourceType 仅接受 real/teacher/ai");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<Question>lambdaQuery();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Question::getType, type);
        }
        if (sourceType != null && !sourceType.isEmpty()) {
            wrapper.eq(Question::getSourceType, sourceType);
        }
        if (status != null) {
            wrapper.eq(Question::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Question::getTitle, kw).or().like(Question::getId, kw));
        }
        wrapper.orderByDesc(Question::getUpdatedAt).orderByAsc(Question::getId);

        Page<Question> questionPage = questionMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<String, Long> refCounts = countSubmissionsByQuestion(
                questionPage.getRecords().stream().map(Question::getId).toList());

        List<QuestionAdminListItemDto> items = new ArrayList<>();
        for (Question question : questionPage.getRecords()) {
            items.add(QuestionAdminListItemDto.builder()
                    .id(question.getId())
                    .type(question.getType())
                    .title(question.getTitle())
                    .sourceType(question.getSourceType())
                    .difficulty(question.getDifficulty())
                    .status(question.getStatus())
                    .hasAnswer(Boolean.TRUE.equals(question.getHasAnswer()))
                    .totalScore(question.getTotalScore())
                    .perQuestionScore(question.getPerQuestionScore())
                    .refSubmissionCount(refCounts.getOrDefault(question.getId(), 0L))
                    .updatedAt(question.getUpdatedAt())
                    .build());
        }
        return new PageResult<>(items, questionPage.getTotal(), safePage, safeSize);
    }

    /**
     * 题目详情(GET /api/admin/questions/{id}):管理端视角,包含 answer_key。
     *
     * @param id 题目 ID
     * @return 详情响应
     * @throws BizException 题目不存在(code 404)
     */
    public QuestionAdminDetailDto detail(String id) {
        return toDetail(findQuestion(id));
    }

    // ==================== 写入 ====================

    /**
     * 新建题目(POST /api/admin/questions)。
     *
     * @param request 录入请求
     * @return 新建后的详情
     */
    @Transactional
    public QuestionAdminDetailDto create(QuestionUpsertRequest request) {
        String type = requireType(request.getType());
        String sourceType = requireSource(request.getSourceType());
        ObjectNode content = validateAndNormalize(type, sourceType, request);

        String id = request.getId() == null || request.getId().isBlank()
                ? type + "-" + sourceType + "-" + LocalDateTime.now().format(TIME_STAMP)
                : request.getId().trim();
        if (questionMapper.selectById(id) != null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "题目 ID 已存在:" + id);
        }
        // content.id 必须与服务端最终 id 一致,故覆写后再序列化入库
        content.put("id", id);

        Question entity = new Question();
        entity.setId(id);
        entity.setType(type);
        entity.setSourceType(sourceType);
        entity.setSourceMeta(buildSourceMeta(sourceType, request.getSourceMeta()).toString());
        entity.setTitle(defaultTitle(request.getTitle(), type));
        entity.setDifficulty(clampDifficulty(request.getDifficulty()));
        entity.setTags(writeJson(request.getTags() == null ? List.of() : request.getTags()));
        entity.setContent(content.toString());
        // 写作可不留范文(走 AI/人工评分),其他题型的答案已在校验里保证非空
        entity.setAnswerKey(writeJson(request.getAnswerKey()));
        // 写作走 AI/人工评分,不存在可比对答案;其余题型通过校验后必然有答案
        entity.setHasAnswer(!"writing".equals(type));
        entity.setTotalScore(PracticeRules.totalScore(type));
        entity.setPerQuestionScore(PracticeRules.perQuestionScore(type));
        entity.setStatus(request.getStatus() == null || request.getStatus() == 1 ? 1 : 0);
        questionMapper.insert(entity);
        log.info("管理端新建题目: id={} type={} source={}", id, type, sourceType);
        return toDetail(entity);
    }

    /**
     * 更新题目(PUT /api/admin/questions/{id})。
     *
     * <p>题型不可变更:历史提交与错题已按原题型快照,改题型会让旧数据无法复现。</p>
     *
     * @param id      路径变量题目 ID
     * @param request 更新请求
     * @return 更新后的详情
     */
    @Transactional
    public QuestionAdminDetailDto update(String id, QuestionUpsertRequest request) {
        Question existing = findQuestion(id);
        String type = existing.getType();
        if (request.getType() != null && !request.getType().isBlank() && !type.equals(request.getType())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "题型不可变更(历史提交与错题按原题型快照)");
        }
        String sourceType = requireSource(request.getSourceType() == null || request.getSourceType().isBlank()
                ? existing.getSourceType() : request.getSourceType());
        ObjectNode content = validateAndNormalize(type, sourceType, request);
        content.put("id", id);

        existing.setSourceType(sourceType);
        // 录入表单不暴露来源 meta(年份/卷别等):请求未提供时沿用原值,避免每次编辑都清空
        JsonNode meta = request.getSourceMeta();
        if (meta == null || meta.isNull() || meta.isEmpty()) {
            meta = extractMeta(existing.getSourceMeta());
        }
        ObjectNode source = buildSourceMeta(sourceType, meta);
        // content.source 与 source_meta 列必须同源,否则学生端看到的来源会与后台记录不一致
        content.set("source", source);
        existing.setSourceMeta(source.toString());
        existing.setTitle(defaultTitle(request.getTitle(), type));
        existing.setDifficulty(clampDifficulty(request.getDifficulty()));
        existing.setTags(writeJson(request.getTags() == null ? List.of() : request.getTags()));
        existing.setContent(content.toString());
        existing.setAnswerKey(writeJson(request.getAnswerKey()));
        existing.setHasAnswer(!"writing".equals(type));
        if (request.getStatus() != null) {
            if (request.getStatus() != 0 && request.getStatus() != 1) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "status 仅接受 0 或 1");
            }
            existing.setStatus(request.getStatus());
        }
        questionMapper.updateById(existing);
        log.info("管理端更新题目: id={} type={}", id, type);
        return toDetail(existing);
    }

    /**
     * 上架/下架(PATCH /api/admin/questions/{id}/status)。
     *
     * <p>下架只影响学生端列表与智能练习(两处均过滤 status=1);
     * 已存在的提交、错题重做与"我的提交"详情仍可正常读取(详情接口不校验 status)。</p>
     *
     * @param id     题目 ID
     * @param status 目标状态:1 上架,0 下架
     */
    public void changeStatus(String id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "status 仅接受 0(下架)或 1(上架)");
        }
        Question question = findQuestion(id);
        question.setStatus(status);
        questionMapper.updateById(question);
        log.info("管理端题目上下架: id={} status={}", id, status);
    }

    /**
     * 物理删除(DELETE /api/admin/questions/{id}),带引用保护。
     *
     * @param id 题目 ID
     * @throws BizException 被提交或错题引用时拒绝(400,提示改用下架)
     */
    @Transactional
    public void delete(String id) {
        findQuestion(id);
        long submissionRefs = countSubmissionsByQuestion(List.of(id)).getOrDefault(id, 0L);
        boolean mistakeRef = mistakeMapper.exists(
                Wrappers.<Mistake>lambdaQuery().eq(Mistake::getQuestionId, id));
        if (submissionRefs > 0 || mistakeRef) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该题已被 " + submissionRefs + " 次提交" + (mistakeRef ? "及错题记录" : "")
                            + " 引用,不能删除;如需停止投放请使用下架");
        }
        questionMapper.deleteById(id);
        log.info("管理端删除题目: id={}", id);
    }

    // ==================== 校验与规范化 ====================

    /**
     * 执行五道校验并返回可入库的 content 节点(其 id/type/source 已按权威值覆写)。
     *
     * @param type       题型
     * @param sourceType 来源类型
     * @param request    录入请求
     * @return 规范化后的 content
     */
    private ObjectNode validateAndNormalize(String type, String sourceType, QuestionUpsertRequest request) {
        JsonNode raw = request.getContent();
        if (raw == null || !raw.isObject()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "content 必须是 JSON 对象");
        }
        // 深拷贝后覆写权威字段,避免直接改动请求对象
        // 注:Jackson 3 的 JsonNode.deepCopy() 统一返回 JsonNode(不像 Jackson 2 在 ObjectNode 上有协变覆写),
        //     因此已判定 isObject() 后需要显式强转
        ObjectNode content = (ObjectNode) raw.deepCopy();

        // 校验 1:结构
        switch (type) {
            case "reading" -> {
                requireText(content, "passage", "content");
                ArrayNode questions = requireArray(content, "questions", 1, "content");
                for (int i = 0; i < questions.size(); i++) {
                    JsonNode q = questions.get(i);
                    String path = "content.questions[" + i + "]";
                    requireIntegerId(q, path);
                    requireText(q, "stem", path);
                    requireOptions(q.get("options"), path + ".options", 2);
                }
            }
            case "matching" -> {
                requireText(content, "passage", "content");
                requireArray(content, "blanks", 1, "content");
                requireOptions(content.get("options"), "content.options", 2);
            }
            case "cloze" -> {
                requireText(content, "passage", "content");
                ArrayNode blanks = requireArray(content, "blanks", 1, "content");
                for (int i = 0; i < blanks.size(); i++) {
                    JsonNode blank = blanks.get(i);
                    String path = "content.blanks[" + i + "]";
                    requireIntegerId(blank, path);
                    requireOptions(blank.get("options"), path + ".options", 2);
                }
            }
            case "grammar-fill" -> {
                requireText(content, "passage", "content");
                ArrayNode blanks = requireArray(content, "blanks", 1, "content");
                for (int i = 0; i < blanks.size(); i++) {
                    requireIntegerId(blanks.get(i), "content.blanks[" + i + "]");
                }
            }
            case "writing" -> {
                requireText(content, "prompt", "content");
                requireArray(content, "requirements", 1, "content");
                requireArray(content, "scoringDimensions", 1, "content");
                int wordLimit = content.path("wordLimit").asInt(0);
                if (wordLimit <= 0) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(), "content.wordLimit 必须为正整数");
                }
            }
            default -> throw new BizException(ResultCode.TYPE_INVALID);
        }

        // 校验 2:答案防呆(content 递归禁止出现答案类键)
        assertNoForbiddenKey(content, "content");

        // 校验 3~5:answer_key 与 content 的一致性
        if ("writing".equals(type)) {
            JsonNode key = request.getAnswerKey();
            if (key != null && !key.isNull()) {
                if (!key.isObject() || (key.has("sampleAnswer") && !key.get("sampleAnswer").isTextual())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(), "answer_key.sampleAnswer 必须是字符串");
                }
            }
        } else {
            validateAnswerKey(type, content, request.getAnswerKey());
        }

        // 覆写权威字段:id 由调用方设置,type/source 在此设置
        content.put("type", type);
        content.set("source", buildSourceMeta(sourceType, request.getSourceMeta()));
        return content;
    }

    /**
     * 校验 answer_key:结构、id 集合一致、答案合法、占位符与空位一致。
     *
     * @param type    题型
     * @param content 已校验的 content
     * @param key     答案节点
     */
    private void validateAnswerKey(String type, JsonNode content, JsonNode key) {
        if (key == null || !key.isObject()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "客观题必须提供 answer_key(对象)");
        }
        boolean reading = "reading".equals(type);
        String itemsField = reading ? "questions" : "blanks";
        JsonNode keyItems = key.get(itemsField);
        if (keyItems == null || !keyItems.isArray() || keyItems.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "answer_key." + itemsField + " 不能为空数组");
        }

        // id 集合一致(不多不少)
        List<Integer> contentIds = collectIds(reading ? content.get("questions") : content.get("blanks"),
                "content." + itemsField);
        List<Integer> keyIds = collectIds(keyItems, "answer_key." + itemsField);
        if (new LinkedHashSet<>(contentIds).size() != contentIds.size()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "content." + itemsField + " 存在重复编号");
        }
        if (!new HashSet<>(contentIds).equals(new HashSet<>(keyIds))) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "answer_key 的编号集合必须与 content 完全一致(题目:" + contentIds + ",答案:" + keyIds + ")");
        }

        // 答案合法:逐题比对候选 key
        Set<String> usedAnswers = new HashSet<>();
        for (int i = 0; i < keyItems.size(); i++) {
            JsonNode item = keyItems.get(i);
            String path = "answer_key." + itemsField + "[" + i + "]";
            int id = item.path("id").asInt();
            String answer = item.path("answer").asText("").trim();
            if (answer.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + ".answer 不能为空");
            }
            if (reading) {
                Set<String> candidates = optionKeys(findById(content.get("questions"), id).get("options"),
                        "content.questions");
                if (!candidates.contains(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            path + ".answer=" + answer + " 不在该题选项 " + candidates + " 中");
                }
            } else if ("cloze".equals(type)) {
                Set<String> candidates = optionKeys(findById(content.get("blanks"), id).get("options"),
                        "content.blanks");
                if (!candidates.contains(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            path + ".answer=" + answer + " 不在该空选项 " + candidates + " 中");
                }
            } else if ("matching".equals(type)) {
                Set<String> candidates = optionKeys(content.get("options"), "content.options");
                if (!candidates.contains(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            path + ".answer=" + answer + " 不在候选句 " + candidates + " 中");
                }
                // 五选五是一对一匹配,同一句不能被填进两个空
                if (!usedAnswers.add(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            "answer_key 五选五答案重复:" + answer);
                }
            }
            // grammar-fill:answer 为自由文本,只校验非空(可接受变体写在 acceptAlternatives)
        }

        // 占位符一致:passage 的 [[n]] 集合必须等于 blanks 的 id 集合
        if (!reading) {
            Set<Integer> marks = collectBlankMarks(content.path("passage").asText(""));
            if (!marks.equals(new HashSet<>(contentIds))) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "passage 中的 [[n]] 占位符集合 " + new ArrayList<>(marks)
                                + " 与 blanks 编号集合 " + contentIds + " 不一致");
            }
        }
    }

    /**
     * 递归禁止 content 出现答案类键。
     *
     * @param node 当前节点
     * @param path 路径(用于报错定位)
     */
    private void assertNoForbiddenKey(JsonNode node, String path) {
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                assertNoForbiddenKey(node.get(i), path + "[" + i + "]");
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        var fields = node.properties();
        for (var entry : fields) {
            String key = entry.getKey();
            if (FORBIDDEN_KEYS.contains(key)) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "content 禁止包含答案类字段 " + path + "." + key + "(答案只能写在 answer_key 里)");
            }
            assertNoForbiddenKey(entry.getValue(), path + "." + key);
        }
    }

    // ==================== 小工具 ====================

    /**
     * 组装 source 对象({type, meta}),与前端 QuestionSource 一致。
     *
     * @param sourceType 来源类型
     * @param meta       来源 meta(可空)
     * @return source 节点
     */
    private ObjectNode buildSourceMeta(String sourceType, JsonNode meta) {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("type", sourceType);
        source.set("meta", meta == null || !meta.isObject() ? objectMapper.createObjectNode() : meta.deepCopy());
        return source;
    }

    /**
     * 从已存的 source_meta 列取出 meta 子对象(更新时沿用原来源明细)。
     *
     * @param sourceMetaJson 已存的完整 source 对象 JSON
     * @return meta 节点,缺失或解析失败返回 null
     */
    private JsonNode extractMeta(String sourceMetaJson) {
        JsonNode stored = readTree(sourceMetaJson);
        return stored == null ? null : stored.get("meta");
    }

    /**
     * 统计一批题目被练习提交引用的次数。
     *
     * @param questionIds 题目 ID 集合
     * @return 题目 ID -> 引用数
     */
    private Map<String, Long> countSubmissionsByQuestion(List<String> questionIds) {
        Map<String, Long> result = new HashMap<>();
        if (questionIds.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> rows = submissionMapper.selectMaps(Wrappers.<PracticeSubmission>query()
                .select("question_id AS questionId", "COUNT(*) AS cnt")
                .in("question_id", questionIds)
                .groupBy("question_id"));
        for (Map<String, Object> row : rows) {
            Object qid = row.get("questionId");
            Object cnt = row.get("cnt");
            if (qid != null && cnt != null) {
                result.put(String.valueOf(qid), Long.parseLong(String.valueOf(cnt)));
            }
        }
        return result;
    }

    /**
     * 收集 questions/blanks 数组的 id 列表(保持声明顺序)。
     *
     * @param items 数组节点
     * @param path  路径(报错定位用)
     * @return id 列表
     */
    private List<Integer> collectIds(JsonNode items, String path) {
        if (items == null || !items.isArray() || items.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 必须是非空数组");
        }
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            requireIntegerId(items.get(i), path + "[" + i + "]");
            ids.add(items.get(i).path("id").asInt());
        }
        return ids;
    }

    /**
     * 提取 passage 中的 [[n]] 占位符编号集合。
     *
     * @param passage 文章正文
     * @return 编号集合
     */
    private Set<Integer> collectBlankMarks(String passage) {
        Set<Integer> marks = new LinkedHashSet<>();
        Matcher matcher = BLANK_MARK.matcher(passage);
        while (matcher.find()) {
            marks.add(Integer.parseInt(matcher.group(1)));
        }
        return marks;
    }

    /**
     * 取选项 key 集合(统一大写比较,容忍管理员写小写)。
     *
     * @param options 选项数组
     * @param path    路径
     * @return key 集合
     */
    private Set<String> optionKeys(JsonNode options, String path) {
        Set<String> keys = new LinkedHashSet<>();
        if (options == null || !options.isArray()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 必须是数组");
        }
        for (JsonNode option : options) {
            keys.add(option.path("key").asText("").toUpperCase());
        }
        return keys;
    }

    /**
     * 按 id 在数组中查找元素(校验答案候选集用)。
     *
     * @param items 数组节点
     * @param id    目标编号
     * @return 命中的元素
     */
    private JsonNode findById(JsonNode items, int id) {
        for (JsonNode item : items) {
            if (item.path("id").asInt() == id) {
                return item;
            }
        }
        throw new BizException(ResultCode.PARAM_ERROR.getCode(), "找不到编号为 " + id + " 的题目元素");
    }

    /**
     * 校验对象含整数 id 字段。
     *
     * @param node 目标节点
     * @param path 路径
     */
    private void requireIntegerId(JsonNode node, String path) {
        if (node == null || !node.isObject() || !node.has("id") || !node.get("id").isNumber()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 缺少数字字段 id(题号/空位编号)");
        }
    }

    /**
     * 校验非空文本字段。
     *
     * @param node  目标节点
     * @param field 字段名
     * @param path  路径
     */
    private void requireText(JsonNode node, String field, String path) {
        if (!node.hasNonNull(field) || node.get(field).asText("").isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + "." + field + " 不能为空");
        }
    }

    /**
     * 校验数组字段非空。
     *
     * @param node  目标节点
     * @param field 字段名
     * @param min   最少元素数
     * @param path  路径
     * @return 数组节点
     */
    private ArrayNode requireArray(JsonNode node, String field, int min, String path) {
        JsonNode array = node.get(field);
        if (array == null || !array.isArray() || array.size() < min) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    path + "." + field + " 至少需要 " + min + " 项");
        }
        return (ArrayNode) array;
    }

    /**
     * 校验选项数组:key 非空且互不重复。
     *
     * @param options 选项数组
     * @param path    路径
     * @param min     最少个数
     */
    private void requireOptions(JsonNode options, String path, int min) {
        if (options == null || !options.isArray() || options.size() < min) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 至少需要 " + min + " 个选项");
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            JsonNode option = options.get(i);
            String key = option.path("key").asText("").trim();
            if (key.isEmpty() || option.path("text").asText("").isBlank()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        path + "[" + i + "] 的 key 与 text 均不能为空");
            }
            if (!seen.add(key.toUpperCase())) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 存在重复选项 key:" + key);
            }
        }
    }

    /**
     * 校验并返回题型。
     *
     * @param type 题型
     * @return 题型
     */
    private String requireType(String type) {
        if (!PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        return type;
    }

    /**
     * 校验并返回来源类型。
     *
     * @param sourceType 来源类型
     * @return 来源类型
     */
    private String requireSource(String sourceType) {
        if (sourceType == null || !VALID_SOURCES.contains(sourceType)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "sourceType 仅接受 real/teacher/ai");
        }
        return sourceType;
    }

    /**
     * 难度收敛到 1~5,留空取 3。
     *
     * @param difficulty 请求难度
     * @return 合法难度
     */
    private Integer clampDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return 3;
        }
        return Math.max(1, Math.min(5, difficulty));
    }

    /**
     * 标题留空时生成默认标题。
     *
     * @param title 请求标题
     * @param type  题型
     * @return 标题
     */
    private String defaultTitle(String title, String type) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        return "后台录入·" + PracticeRules.typeName(type) + " · " + LocalDateTime.now().format(TITLE_STAMP);
    }

    /**
     * 按 ID 查找题目,不存在即 404。
     *
     * @param id 题目 ID
     * @return 题目实体
     */
    private Question findQuestion(String id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "题目不存在");
        }
        return question;
    }

    /**
     * 实体 -> 管理端详情(解析三个 JSON 列为节点)。
     *
     * @param question 题目实体
     * @return 详情响应
     */
    private QuestionAdminDetailDto toDetail(Question question) {
        return QuestionAdminDetailDto.builder()
                .id(question.getId())
                .type(question.getType())
                .title(question.getTitle())
                .sourceType(question.getSourceType())
                .sourceMeta(extractMeta(question.getSourceMeta()))
                .difficulty(question.getDifficulty())
                .tags(readStringList(question.getTags()))
                .status(question.getStatus())
                .hasAnswer(Boolean.TRUE.equals(question.getHasAnswer()))
                .totalScore(question.getTotalScore())
                .perQuestionScore(question.getPerQuestionScore())
                .content(readTree(question.getContent()))
                .answerKey(readTree(question.getAnswerKey()))
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    /**
     * 序列化为 JSON 字符串(null 归一化为空串占位,避免写入 SQL NULL 之外的脏值)。
     *
     * @param value 任意值
     * @return JSON 字符串
     */
    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "JSON 字段序列化失败:" + e.getMessage());
        }
    }

    /**
     * 解析 JSON 字符串为节点(容错返回 null)。
     *
     * @param json JSON 字符串
     * @return 节点或 null
     */
    private JsonNode readTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("管理端 JSON 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析字符串数组 JSON(标签列)。
     *
     * @param json JSON 字符串
     * @return 字符串列表(解析失败返回空列表)
     */
    private List<String> readStringList(String json) {
        JsonNode node = readTree(json);
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String value = item.asText("");
                if (!value.isBlank()) {
                    result.add(value);
                }
            }
        }
        return result;
    }
}
