package com.linguacraft.backend.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成绩报告中的单部分得分(对应前端 ExamReport.sections 元素)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamReportSectionDto {

    /** 部分序号 */
    @Schema(description = "部分序号:1-阅读,2-五选五,3-完形,4-语法填空,5-作文", example = "1")
    private Integer part;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 该部分得分(作文未批时为 0) */
    @Schema(description = "该部分得分(作文未批时为 0)")
    private BigDecimal score;

    /** 该部分总分 */
    @Schema(description = "该部分总分", example = "30.0")
    private BigDecimal total;
}