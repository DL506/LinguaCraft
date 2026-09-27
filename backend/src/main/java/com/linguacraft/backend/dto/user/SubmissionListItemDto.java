package com.linguacraft.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linguacraft.backend.dto.practice.SourceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提交记录列表项(对应前端 SubmissionListItem;仅练习提交,模考不入本列表)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionListItemDto {

    /** 提交记录 ID(对外为字符串) */
    @Schema(description = "提交记录 ID(字符串)")
    private Long id;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 来源信息 */
    @Schema(description = "来源信息")
    private SourceDto source;

    /** 批改状态:graded/partially_graded/pending */
    @Schema(description = "批改状态:graded/partially_graded/pending")
    private String status;

    /** 得分(未批改时为空) */
    @Schema(description = "得分(未批改时为空)")
    private BigDecimal score;

    /** 总分 */
    @Schema(description = "总分")
    private BigDecimal totalScore;

    /** 提交时间(ISO-8601) */
    @Schema(description = "提交时间(ISO-8601)")
    private LocalDateTime submittedAt;

    /** 预计出结果说明(待批改时返回) */
    @Schema(description = "预计出结果说明(待批改时返回)")
    private String estimatedTime;
}