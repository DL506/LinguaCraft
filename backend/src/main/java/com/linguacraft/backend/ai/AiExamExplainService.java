package com.linguacraft.backend.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.ExamExplainDto;
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
 * 真题卷「AI 补解析」服务(阶段 A11)。
 *
 * <p>职责边界:读<b>表单此刻的某个部分</b>(不读库)、分批调模型、把返回清洗成
 * <b>只有卡号与中文解析</b>的建议清单,全程不写库。管理员逐条采纳进表单,落库仍由「校验并保存」触发。</p>
 *
 * <p>为什么按部分而不是整卷:整卷 45 题要 9 批,每批实测 15~40 秒(A10 数据),同步等一次要三五分钟;
 * 一个部分是 1~3 批,等得起。</p>
 *
 * <p>服务端清洗(不指望提示词兑现):</p>
 * <ol>
 *   <li>卡号必须属于本批发出去的那批编号,否则整条丢弃并计数 —— 真题卷的卡号是全卷累加值,
 *       模型若自己编号,采纳后会把解析写到<b>别的部分的题</b>上;</li>
 *   <li>解析去空白,空的一律丢弃(空建议进表单只会覆盖掉已有解析);</li>
 *   <li>没有标准答案的项<b>不发往模型</b>:否则只能让 AI 自己猜答案再"解释"它,那是编造;</li>
 *   <li>超长截断,避免模型跑飞把上千字塞进 {@code answer_key};</li>
 *   <li>跨批同卡号以先到的为准,前端不会出现两行一样的建议。</li>
 * </ol>
 */
@Service
public class AiExamExplainService {

    private static final Logger log = LoggerFactory.getLogger(AiExamExplainService.class);

    /**
     * 单批的答案项数上限。
     *
     * <p>沿用 A10 的实测结论:5 项一批 15~40 秒能活着回来,10 项一次必定卡在 60 秒
     * (对端中途关闭响应)。这里批大小与超时都跟着校核那条路径走。</p>
     */
    private static final int EXPLAIN_CHUNK_SIZE = 5;

    /** 单条解析的字符上限(超出即截断) */
    private static final int MAX_EXPLANATION_LENGTH = 600;

    private final AiChatService aiChatService;

    private final ObjectMapper objectMapper;

    /**
     * 单批等待上限(秒),与 A10 校核共用一档配置。
     *
     * <p>字段留初值是为了纯单测(无 Spring 注入)时不会退化成 0 秒。</p>
     */
    @Value("${lc.ai.review-timeout-seconds:180}")
    private long batchTimeoutSeconds = 180L;

    public AiExamExplainService(AiChatService aiChatService, ObjectMapper objectMapper) {
        this.aiChatService = aiChatService;
        this.objectMapper = objectMapper;
    }

    /**
     * 为真题卷某个部分补写中文解析(分批调模型,全程不写库)。
     *
     * @param type      本部分题型(写作无客观题解析,直接拒绝)
     * @param content   本部分卷面(阅读为数组,其余为带 blanks 的对象)
     * @param itemsNode 本部分答案项数组 {@code [{id,answer,explanation?}]}
     * @return 解析建议清单(未写库)
     * @throws BizException 题型非法(1004)、无可处理项(400)、AI 不可用或返回非法 JSON(500)
     */
    public ExamExplainDto explain(String type, JsonNode content, JsonNode itemsNode) {
        if (!PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        if ("writing".equals(type)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "写作部分没有客观题解析可生成(请改用批改工作台的 AI 评分与范文)");
        }
        if (content == null || !(content.isObject() || content.isArray())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "content 必须是本部分的卷面节点");
        }
        if (itemsNode == null || !itemsNode.isArray() || itemsNode.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "本部分没有答案项(items)");
        }

        List<JsonNode> requestable = new ArrayList<>();
        int skippedNoAnswer = 0;
        for (JsonNode item : itemsNode) {
            if (!item.path("id").isNumber()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "items[].id 必须是答题卡编号(数字)");
            }
            // 没有标准答案就不该让模型"解释"它:那等于让 AI 自己定答案
            if (item.path("answer").asText("").isBlank()) {
                skippedNoAnswer++;
                continue;
            }
            requestable.add(item);
        }
        if (requestable.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "本部分 " + skippedNoAnswer + " 道题都还没有标准答案,先补齐答案再让 AI 写解析");
        }

        Set<Integer> knownIds = new LinkedHashSet<>();
        for (JsonNode item : requestable) {
            knownIds.add(item.path("id").asInt());
        }

        List<ExamExplainDto.Item> items = new ArrayList<>();
        Set<Integer> collected = new LinkedHashSet<>();
        int dropped = 0;
        int batches = 0;
        for (int from = 0; from < requestable.size(); from += EXPLAIN_CHUNK_SIZE) {
            List<JsonNode> chunk = requestable.subList(from, Math.min(from + EXPLAIN_CHUNK_SIZE, requestable.size()));
            List<Integer> batchIds = chunk.stream().map(node -> node.path("id").asInt()).toList();
            JsonNode json = aiChatService.chatForJson(
                    PromptTemplates.examExplainPrompt(type, content, subset(chunk), batchIds), batchTimeoutSeconds);
            batches++;
            JsonNode rawItems = json.get("items");
            if (rawItems == null || !rawItems.isArray()) {
                continue;
            }
            for (JsonNode raw : rawItems) {
                int id = raw.path("id").asInt(Integer.MIN_VALUE);
                String explanation = raw.path("explanation").asText("").trim();
                if (!knownIds.contains(id) || explanation.isEmpty()) {
                    dropped++;
                    continue;
                }
                // 分批之间同一编号被说两次:以先到的为准
                if (!collected.add(id)) {
                    dropped++;
                    continue;
                }
                items.add(ExamExplainDto.Item.builder()
                        .id(id)
                        .explanation(explanation.length() > MAX_EXPLANATION_LENGTH
                                ? explanation.substring(0, MAX_EXPLANATION_LENGTH) : explanation)
                        .build());
            }
        }

        ExamExplainDto dto = ExamExplainDto.builder()
                .type(type)
                .items(items)
                .requestedCount(requestable.size())
                .skippedNoAnswer(skippedNoAnswer)
                .droppedCount(dropped)
                .batchCount(batches)
                .build();
        log.info("AI 补解析完成(未写库): type={} 送处理={} 跳过无答案={} 批数={} 采纳建议={} 丢弃={}",
                type, requestable.size(), skippedNoAnswer, batches, items.size(), dropped);
        return dto;
    }

    /**
     * 装出本批答案项数组(提示词只看到本批编号,避免它顺手把别的部分也写了)。
     *
     * @param chunk 本批答案项
     * @return 只含本批的数组节点
     */
    private ArrayNode subset(List<JsonNode> chunk) {
        ArrayNode subset = objectMapper.createArrayNode();
        chunk.forEach(subset::add);
        return subset;
    }
}
