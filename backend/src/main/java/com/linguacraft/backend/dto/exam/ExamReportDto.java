package com.linguacraft.backend.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linguacraft.backend.dto.practice.WritingDimensionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 模考成绩报告(对应前端 ExamReport)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamReportDto {

    /** 总分(作文未出分时=客观题得分) */
    @Schema(description = "总分(作文未出分时=客观题得分)")
    private BigDecimal score;

    /** 卷面总分 */
    @Schema(description = "卷面总分", example = "100.0")
    private BigDecimal totalScore;

    /** 各部分得分 */
    @Schema(description = "各部分得分")
    private List<ExamReportSectionDto> sections;

    /** 薄弱项描述(得分率 <60% 的部分;无则默认鼓励语) */
    @Schema(description = "薄弱项描述")
    private List<String> weakPoints;

    /** 历年成绩对比(该用户已出分的模考,按年份升序) */
    @Schema(description = "历年成绩对比")
    private List<HistoryScoreDto> compareWithHistory;

    /** 客观题是否已批改 */
    @Schema(description = "客观题是否已批改")
    private Boolean objectiveGraded;

    /** 写作是否已批改 */
    @Schema(description = "写作是否已批改")
    private Boolean writingGraded;

    /** 写作得分(写作已批改时返回) */
    @Schema(description = "写作得分(写作已批改时返回)")
    private BigDecimal writingScore;

    /** 写作逐维评分(作文已出分时返回;与练习批改 SubmitResultResponse.dimensions 同形状) */
    @Schema(description = "写作逐维评分(作文已出分时返回)")
    private List<WritingDimensionDto> writingDimensions;

    /** 写作参考范文 */
    @Schema(description = "写作参考范文(可空)")
    private String writingSampleAnswer;

    /** 写作总评 */
    @Schema(description = "写作总评(可空)")
    private String writingOverallComment;
}