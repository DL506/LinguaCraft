package com.linguacraft.backend.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模考列表项(对应前端 ExamListItem)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamListItemDto {

    /** 试卷 ID */
    @Schema(description = "试卷 ID", example = "exam-2026")
    private String id;

    /** 年份 */
    @Schema(description = "年份", example = "2026")
    private Integer year;

    /** 试卷标题 */
    @Schema(description = "试卷标题")
    private String title;

    /** 卷面总分 */
    @Schema(description = "卷面总分", example = "100.0")
    private BigDecimal totalScore;

    /** 考试时长(分钟) */
    @Schema(description = "考试时长(分钟)", example = "120")
    private Integer duration;

    /** 最近一次得分(无提交或未出分时为空) */
    @Schema(description = "最近一次得分(无提交或待批改时为空)")
    private BigDecimal lastScore;

    /** 最近一次交卷时间(ISO-8601) */
    @Schema(description = "最近一次交卷时间(ISO-8601)")
    private LocalDateTime lastSubmittedAt;
}