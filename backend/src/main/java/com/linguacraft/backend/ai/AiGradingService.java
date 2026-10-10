package com.linguacraft.backend.ai;

import tools.jackson.databind.JsonNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.practice.SubmitDetailDto;
import com.linguacraft.backend.dto.practice.WritingDimensionDto;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.service.GradedObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 批改服务(Spring AI):题库无答案的客观题判分、作文全权评分。
 *
 * <p>口径(已确认裁决):</p>
 * <ul>
 *   <li>客观题:优先数据库答案比对(ObjectiveGradingService);库中无答案时才走本服务的 AI 判分;</li>
 *   <li>作文:AI 模式下由本服务全权评分(维度/范文/总评);失败由上层降级为 pending 转人工。</li>
 * </ul>
 */
@Service
public class AiGradingService {

    private static final Logger log = LoggerFactory.getLogger(AiGradingService.class);

    private final AiChatService aiChatService;

    public AiGradingService(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * AI 判分客观题(题库无答案场景)。
     *
     * <p>明细以<b>学生实际作答的题号</b>为准:模型漏答的题号补成"未判定(isCorrect=false)",
     * 多答的题号忽略。否则学生端逐题表会少行,且 correct_count/total_count 口径错位。</p>
     *
     * @param question 题目(下发内容作为判分依据)
     * @param answers  用户作答(题号 -> 作答)
     * @return 判分结果(明细含 AI 给出的正确答案与解析;得分按题型单题分值计算并封顶卷面总分)
     * @throws BizException AI 返回结构异常(code 500,由上层降级转人工)
     */
    public GradedObjective gradeObjective(Question question, Map<Integer, String> answers) {
        JsonNode json = aiChatService.chatForJson(PromptTemplates.objectiveGradingPrompt(question, answers));
        JsonNode detailsNode = json.get("details");
        if (detailsNode == null || !detailsNode.isArray() || detailsNode.isEmpty()) {
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 判分结果结构异常");
        }

        // 先把模型给的判定按题号索引,再以学生作答的题号升序逐项落地
        Map<Integer, JsonNode> gradedById = new HashMap<>();
        for (JsonNode item : detailsNode) {
            gradedById.putIfAbsent(item.path("id").asInt(), item);
        }
        List<Integer> askedIds = new ArrayList<>(answers.keySet());
        askedIds.sort(Comparator.naturalOrder());

        List<SubmitDetailDto> details = new ArrayList<>();
        int correctCount = 0;
        for (Integer id : askedIds) {
            JsonNode item = gradedById.get(id);
            if (item == null) {
                log.warn("AI 判分缺失题号 {},按未正确计入", id);
            }
            SubmitDetailDto detail = SubmitDetailDto.builder()
                    .id(id)
                    // 用户作答以请求原文为准(AI 不参与回填)
                    .userAnswer(answers.getOrDefault(id, ""))
                    .correctAnswer(item == null ? "" : item.path("correctAnswer").asText(""))
                    .isCorrect(item != null && item.path("isCorrect").asBoolean(false))
                    .explanation(item == null ? "AI 未返回该题判定,请转人工复核" : item.path("explanation").asText(""))
                    .build();
            if (Boolean.TRUE.equals(detail.getIsCorrect())) {
                correctCount++;
            }
            details.add(detail);
        }

        BigDecimal perQuestion = question.getPerQuestionScore() == null
                ? BigDecimal.ONE : question.getPerQuestionScore();
        BigDecimal score = perQuestion.multiply(BigDecimal.valueOf(correctCount)).setScale(1, RoundingMode.HALF_UP);
        BigDecimal cap = question.getTotalScore();
        if (cap != null && score.compareTo(cap) > 0) {
            score = cap.setScale(1, RoundingMode.HALF_UP);
        }
        return new GradedObjective(details, score, correctCount, details.size());
    }

    /**
     * AI 作文评分(全权处理):评分维度 / 范文 / 总评 / 总分。
     *
     * @param question 写作题(题目与评分维度来自下发内容)
     * @param content  学生作文正文
     * @return 评分结果(总分封顶卷面满分 15)
     * @throws BizException AI 返回结构异常(code 500,由上层降级 pending 转人工)
     */
    public GradedWriting gradeWriting(Question question, String content) {
        JsonNode json = aiChatService.chatForJson(PromptTemplates.writingGradingPrompt(question, content));

        List<WritingDimensionDto> dimensions = new ArrayList<>();
        JsonNode dimensionNode = json.get("dimensions");
        if (dimensionNode != null && dimensionNode.isArray()) {
            for (JsonNode item : dimensionNode) {
                dimensions.add(WritingDimensionDto.builder()
                        .name(item.path("name").asText(""))
                        .score(item.hasNonNull("score") ? item.get("score").decimalValue() : BigDecimal.ZERO)
                        .comment(item.path("comment").asText(""))
                        .build());
            }
        }
        if (dimensions.isEmpty()) {
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 作文评分结果结构异常");
        }

        BigDecimal total = dimensions.stream()
                .map(WritingDimensionDto::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
        BigDecimal cap = question.getTotalScore() == null ? new BigDecimal("15") : question.getTotalScore();
        if (total.compareTo(cap) > 0) {
            total = cap;
        }
        return new GradedWriting(dimensions,
                json.path("sampleAnswer").asText(null),
                json.path("overallComment").asText(null),
                total);
    }
}