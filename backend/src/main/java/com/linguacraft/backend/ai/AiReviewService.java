package com.linguacraft.backend.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AiReviewDto;
import com.linguacraft.backend.util.PracticeRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * AI 题目校核服务(阶段 A10)。
 *
 * <p>职责边界:读< b>当前表单内容</b>(不读库)、调一次模型、把返回结果<b>清洗成可安全采纳的建议</b>,
 * 全程不写库。写 {@code question} 内容的 AI 一律要过人 —— 采纳由管理员逐项点,落库仍由保存触发。</p>
 *
 * <p>对模型返回的四道清洗(都在服务端做,不指望提示词兑现):
 * ① 编号必须存在于 answer_key,否则整条丢弃并计数;
 * ② 非语法填空题型一律抹掉 {@code alternatives}(判分不读它,给界面采纳就是假承诺);
 * ③ 风险取值必须落在白名单,未知值归一为 {@code ok},避免前端渲染出没定义的状态;
 * ④ 变体去空白、去重、去掉与标准答案相同的那一项(否则"采纳"会让可接受变体里混进标准答案自己)。</p>
 */
@Service
public class AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);

    /** 标准答案风险的合法取值 */
    private static final Set<String> ANSWER_RISKS = Set.of("ok", "mayBeWrong", "notUnique");

    /** 解析风险的合法取值 */
    private static final Set<String> EXPLANATION_RISKS = Set.of("ok", "mismatch", "tooThin");

    /**
     * 单批校核的答案项数上限。
     *
     * <p>不是拍脑袋:实测 5 项的题 16 秒返回,而 10 空的语法填空<b>必然</b>卡在 60 秒
     * (报 {@code OpenAIInvalidDataException: Error reading response},是响应被对端中途关闭,
     * 抬高我们这边的 Future 超时与 SDK timeout 都越不过去 —— 试过 180s/200s,失败点仍在 60.5s)。
     * 因此把一次校核拆成 ≤5 项的小请求,每批都留足余量,代价只是多一次模型调用。</p>
     */
    private static final int REVIEW_CHUNK_SIZE = 5;

    private final AiChatService aiChatService;

    private final ObjectMapper objectMapper;

    /**
     * 单批校核的应用层等待上限(秒)。
     *
     * <p>实测一道 10 空语法填空:一次问完 5 批以内仍需 60~90 秒,60 秒会在我们自己这里断掉
     * (日志 {@code AI 调用超时(>60 秒)}),所以与 HTTP 层一起放宽到 180 秒。
     * 字段留初值是为了纯单测(无 Spring 注入)时不会退化成 0 秒。</p>
     */
    @Value("${lc.ai.review-timeout-seconds:180}")
    private long reviewTimeoutSeconds = 180L;

    public AiReviewService(AiChatService aiChatService, ObjectMapper objectMapper) {
        this.aiChatService = aiChatService;
        this.objectMapper = objectMapper;
    }

    /**
     * 校核一份题面 + 答案(分批调模型,全程不写库)。
     *
     * @param type      题型(五种之一)
     * @param content   当前题面
     * @param answerKey 当前答案与解析
     * @return 建议清单(未写库);模型一条都没返回时 items 为空
     * @throws BizException 题型非法(1004)、无可校核项(400)、AI 不可用或返回非法 JSON(500)
     */
    public AiReviewDto review(String type, JsonNode content, JsonNode answerKey) {
        if (!PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        if (answerKey == null || !answerKey.isObject()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "answer_key 必须是 JSON 对象");
        }
        // 写作题没有可比对的答案项,校核无对象(与"客观题不进人工队列"同一条现实)
        String itemsField = itemsFieldOf(answerKey);
        List<JsonNode> stored = keyItems(answerKey);
        if (stored.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该题没有可校核的答案项(写作题请改用 AI 预评;客观题需先填答案)");
        }
        Set<Integer> knownIds = new LinkedHashSet<>();
        for (JsonNode node : stored) {
            knownIds.add(node.path("id").asInt());
        }

        List<AiReviewDto.Item> items = new ArrayList<>();
        List<String> summaries = new ArrayList<>();
        Set<Integer> collected = new LinkedHashSet<>();
        int dropped = 0;
        for (int from = 0; from < stored.size(); from += REVIEW_CHUNK_SIZE) {
            List<JsonNode> chunk = stored.subList(from, Math.min(from + REVIEW_CHUNK_SIZE, stored.size()));
            List<Integer> batchIds = chunk.stream().map(node -> node.path("id").asInt()).toList();
            JsonNode json = aiChatService.chatForJson(
                    PromptTemplates.reviewPrompt(type, content, subsetKey(itemsField, chunk), batchIds),
                    reviewTimeoutSeconds);
            JsonNode rawItems = json.get("items");
            if (rawItems != null && rawItems.isArray()) {
                for (JsonNode raw : rawItems) {
                    int id = raw.path("id").asInt(Integer.MIN_VALUE);
                    // 分批后同一编号可能被不同批次各说一次:以先到的为准,避免前端出现两行一样的建议
                    if (!knownIds.contains(id) || !collected.add(id)) {
                        dropped++;
                        continue;
                    }
                    items.add(toItem(type, raw, id, answerOf(stored, id)));
                }
            }
            String summary = text(json.get("summary"));
            if (!summary.isEmpty()) {
                summaries.add(summary);
            }
        }
        AiReviewDto dto = AiReviewDto.builder()
                .type(type)
                .summary(String.join("；", summaries))
                .items(items)
                .droppedCount(dropped)
                .build();
        log.info("AI 校核完成(未写库): type={} 答案项={} 批数={} 建议={} 丢弃={}",
                type, stored.size(), (stored.size() + REVIEW_CHUNK_SIZE - 1) / REVIEW_CHUNK_SIZE,
                items.size(), dropped);
        return dto;
    }

    /**
     * 取答案项所在的字段名(阅读用 questions,其余用 blanks,真题卷沿用 items)。
     *
     * @param answerKey 答案对象
     * @return 字段名,都不存在时返回 blanks
     */
    private String itemsFieldOf(JsonNode answerKey) {
        for (String field : List.of("blanks", "questions", "items")) {
            JsonNode node = answerKey.get(field);
            if (node != null && node.isArray() && !node.isEmpty()) {
                return field;
            }
        }
        return "blanks";
    }

    /**
     * 用同一字段名装出一批答案项,让提示词看到的仍是"完整形状",只是编号少一批。
     *
     * @param field  答案项字段名
     * @param chunk  本批答案项
     * @return 只含本批的 answer_key 副本
     */
    private ObjectNode subsetKey(String field, List<JsonNode> chunk) {
        ObjectNode subset = objectMapper.createObjectNode();
        ArrayNode array = subset.putArray(field);
        chunk.forEach(array::add);
        return subset;
    }

    /**
     * 清洗单条建议。
     *
     * @param type         题型(决定要不要保留变体)
     * @param raw          模型返回的一项
     * @param id           已确认存在的编号
     * @param standardAnswer 该编号的标准答案(用于把"变体里混进标准答案"剔除)
     * @return 归一后的建议
     */
    private AiReviewDto.Item toItem(String type, JsonNode raw, int id, String standardAnswer) {
        boolean grammar = "grammar-fill".equals(type);
        List<String> alternatives = grammar ? cleanAlternatives(raw.get("alternatives"), standardAnswer) : List.of();
        return AiReviewDto.Item.builder()
                .id(id)
                .alternatives(alternatives)
                .answerRisk(normalize(raw.path("answerRisk").asText(""), ANSWER_RISKS))
                .explanationRisk(normalize(raw.path("explanationRisk").asText(""), EXPLANATION_RISKS))
                .explanation(text(raw.get("explanation")))
                .note(text(raw.get("note")))
                .build();
    }

    /**
     * 变体清洗:去空白、去重、去掉与标准答案相同项(大小写敏感地比,因为大小写另有开关兜住)。
     *
     * @param node             模型返回的 alternatives
     * @param standardAnswer   该空标准答案
     * @return 可直接采纳的写法列表
     */
    private List<String> cleanAlternatives(JsonNode node, String standardAnswer) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        Set<String> cleaned = new LinkedHashSet<>();
        for (JsonNode one : node) {
            String value = one.asText("").trim();
            if (value.isEmpty() || value.equals(standardAnswer)) {
                continue;
            }
            cleaned.add(value);
        }
        return List.copyOf(cleaned);
    }

    /**
     * 取 answer_key 的答案项数组(阅读用 questions,其余用 blanks,真题卷沿用 items)。
     *
     * @param answerKey 答案对象
     * @return 答案项列表(可能为空)
     */
    private List<JsonNode> keyItems(JsonNode answerKey) {
        List<JsonNode> out = new ArrayList<>();
        for (String field : List.of("blanks", "questions", "items")) {
            JsonNode node = answerKey.get(field);
            if (node != null && node.isArray()) {
                node.forEach(out::add);
            }
        }
        return out;
    }

    /**
     * 某编号的标准答案。
     *
     * @param stored 答案项列表
     * @param id     编号
     * @return 标准答案(缺失返回空串)
     */
    private String answerOf(List<JsonNode> stored, int id) {
        for (JsonNode node : stored) {
            if (node.path("id").asInt() == id) {
                return node.path("answer").asText("");
            }
        }
        return "";
    }

    /**
     * 取值归一到白名单。
     *
     * @param value    模型给的值
     * @param allowed  合法集合
     * @return 合法值,未知一律按 ok
     */
    private String normalize(String value, Set<String> allowed) {
        return value != null && allowed.contains(value) ? value : "ok";
    }

    /**
     * 文本节点取值(空白归一为空串,避免前端渲染出 "null")。
     *
     * @param node 节点
     * @return 文本
     */
    private String text(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText("").trim();
    }
}
