package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 写作评分维度(对应前端 WritingSubmitResult.dimensions 元素)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WritingDimensionDto {

    /** 维度名称 */
    @Schema(description = "维度名称", example = "内容要点")
    private String name;

    /** 维度得分 */
    @Schema(description = "维度得分", example = "5")
    private BigDecimal score;

    /** 维度评语 */
    @Schema(description = "维度评语")
    private String comment;
}