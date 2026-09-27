package com.linguacraft.backend.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 历年成绩对比项(对应前端 ExamReport.compareWithHistory 元素)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryScoreDto {

    /** 年份 */
    @Schema(description = "年份", example = "2026")
    private Integer year;

    /** 该年得分(取该年最近一次已出分的模考) */
    @Schema(description = "该年得分")
    private BigDecimal score;
}