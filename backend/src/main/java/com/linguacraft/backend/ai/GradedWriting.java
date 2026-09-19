package com.linguacraft.backend.ai;

import java.math.BigDecimal;
import java.util.List;

import com.linguacraft.backend.dto.practice.WritingDimensionDto;

/**
 * AI 作文评分结果(内部值对象)。
 *
 * @param dimensions     评分维度(名称/得分/评语)
 * @param sampleAnswer   范文
 * @param overallComment 总评
 * @param score          作文总分(维度分合计,封顶卷面总分)
 */
public record GradedWriting(List<WritingDimensionDto> dimensions, String sampleAnswer, String overallComment,
                            BigDecimal score) {
}