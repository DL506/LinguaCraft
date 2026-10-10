package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端模考批改队列列表项(阶段 A4-②)。
 *
 * <p>{@code writingGraded=false} 即"作文还欠着",是这批记录里唯一需要人工看的东西 ——
 * 客观分在交卷那一刻就已算好。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端模考批改队列列表项")
public class AdminExamSubmissionListItemDto {

    /** 提交 ID */
    @Schema(description = "提交 ID")
    private Long id;

    /** 用户 ID */
    @Schema(description = "提交用户 ID")
    private Long userId;

    /** 用户昵称 */
    @Schema(description = "提交用户昵称")
    private String nickname;

    /** 试卷 ID */
    @Schema(description = "试卷 ID", example = "exam-2026")
    private String examId;

    /** 试卷标题 */
    @Schema(description = "试卷标题")
    private String paperTitle;

    /** 试卷年份 */
    @Schema(description = "试卷年份", example = "2026")
    private Integer year;

    /** 状态 */
    @Schema(description = "状态:graded/partially_graded/pending")
    private String status;

    /** 批改方式 */
    @Schema(description = "批改方式:ai/auto/manual")
    private String gradingMethod;

    /** 客观题得分(交卷即算) */
    @Schema(description = "客观题得分")
    private BigDecimal objectiveScore;

    /** 写作得分(未批改为空) */
    @Schema(description = "写作得分(未批改为空)")
    private BigDecimal writingScore;

    /** 卷面总分快照 */
    @Schema(description = "卷面总分快照", example = "100.0")
    private BigDecimal totalScore;

    /** 作文是否已出分 */
    @Schema(description = "作文是否已出分")
    private Boolean writingGraded;

    /** 学生是否写了作文 */
    @Schema(description = "学生是否写了作文(空白正文不可判)")
    private Boolean hasWritingContent;

    /** 等待时长(分钟) */
    @Schema(description = "等待时长(分钟,自交卷起算)")
    private Long waitingMinutes;

    /** 交卷时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "交卷时间")
    private LocalDateTime submittedAt;

    /** 作文出分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "作文出分时间(可空)")
    private LocalDateTime writingGradedAt;
}
