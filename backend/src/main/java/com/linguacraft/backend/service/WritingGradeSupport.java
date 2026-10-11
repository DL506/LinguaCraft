package com.linguacraft.backend.service;

import com.linguacraft.backend.ai.GradedWriting;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.GradeDimension;
import com.linguacraft.backend.dto.admin.WritingPreviewDto;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 作文判分结果写手(唯一产出 {@code writing_result} JSON 的地方)。
 *
 * <p>练习人工批改(A4)与模考作文人工批改(A4-②)落的是<b>不同表</b>、用<b>不同 CAS 条件</b>,
 * 但写进 JSON 列的形状必须一模一样 —— 学生端两套渲染器都按 {@code dimensions[{name,score,comment}]}
 * + {@code sampleAnswer} + {@code overallComment} 读取。因此分值校验与拼装集中在这里,
 * 两个服务各自只负责"取数 + 落库"。</p>
 */
@Component
public class WritingGradeSupport {

    private final ObjectMapper objectMapper;

    public WritingGradeSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 校验维度并拼装 {@code writing_result} 节点。
     *
     * @param dimensions     评分维度(非空)
     * @param sampleAnswer   参考范文(可空,空则不写该键)
     * @param overallComment 总评(可空,空则不写该键)
     * @param cap            该项满分(练习=卷面总分,模考=写作部分分值)
     * @param capLabel       满分叫法(用于报错文案,如"卷面总分"/"作文满分")
     * @return writing_result 节点
     */
    public ObjectNode buildResult(List<GradeDimension> dimensions, String sampleAnswer, String overallComment,
                                  BigDecimal cap, String capLabel) {
        if (dimensions == null || dimensions.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "作文题必须提交至少一个评分维度(dimensions)");
        }
        ObjectNode root = objectMapper.createObjectNode();
        var array = root.putArray("dimensions");
        for (GradeDimension dimension : dimensions) {
            if (dimension.getScore() == null || dimension.getScore().signum() < 0) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "维度「" + dimension.getName() + "」得分不能为空或为负");
            }
            ObjectNode node = array.addObject();
            node.put("name", dimension.getName());
            node.put("score", dimension.getScore().setScale(1, RoundingMode.HALF_UP));
            node.put("comment", dimension.getComment() == null ? "" : dimension.getComment());
        }
        if (sampleAnswer != null && !sampleAnswer.isBlank()) {
            root.put("sampleAnswer", sampleAnswer);
        }
        if (overallComment != null && !overallComment.isBlank()) {
            root.put("overallComment", overallComment);
        }
        BigDecimal sum = dimensionSum(dimensions);
        if (cap != null && sum.compareTo(cap) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "维度分之和 " + sum + " 超过" + capLabel + " " + cap + ",请调整各维度得分");
        }
        return root;
    }

    /**
     * 维度分之和(保留 1 位小数)。
     *
     * @param dimensions 维度列表
     * @return 合计
     */
    public BigDecimal dimensionSum(List<GradeDimension> dimensions) {
        return dimensions.stream()
                .map(GradeDimension::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 把 AI 作文评分结果转成预评草稿(练习与模考两条预评路径共用同一个映射,形状保持一致)。
     *
     * <p>建议总分取<b>维度之和</b>而不是 AI 自报的 {@code graded.score()} —— 抽屉里管理员看到的是一格
     * 一格的维度分,若顶部总分与它们加不起来,就会被当成 bug。AI 自报的合计只在
     * {@code AiGradingService} 内部做封顶用,到这里以维度实算为准。</p>
     *
     * @param graded AI 评分结果
     * @param cap    该项满分(题面口径,不采信 AI 返回值)
     * @return 预评草稿
     */
    public WritingPreviewDto toPreview(GradedWriting graded, BigDecimal cap) {
        List<GradeDimension> dimensions = graded.dimensions().stream()
                .map(item -> GradeDimension.builder()
                        .name(item.getName())
                        .score(item.getScore() == null ? BigDecimal.ZERO
                                : item.getScore().setScale(1, RoundingMode.HALF_UP))
                        .comment(item.getComment())
                        .build())
                .toList();
        BigDecimal sum = dimensionSum(dimensions);
        return WritingPreviewDto.builder()
                .score(cap != null && sum.compareTo(cap) > 0 ? cap : sum)
                .cap(cap)
                .dimensions(dimensions)
                .sampleAnswer(graded.sampleAnswer())
                .overallComment(graded.overallComment())
                .build();
    }
}
