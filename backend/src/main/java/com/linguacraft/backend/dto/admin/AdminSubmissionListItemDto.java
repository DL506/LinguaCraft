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
 * 管理端批改队列列表项(阶段 A4)。
 *
 * <p>{@code waitingMinutes} 让管理员先处理等得最久的提交;{@code gradingMethod} 用来区分
 * {@code auto}(定时任务到期会自动比对出分,人工不必插手)与 {@code manual}(真正的断链)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端批改队列列表项")
public class AdminSubmissionListItemDto {

    /** 提交 ID */
    @Schema(description = "提交 ID", example = "17")
    private Long id;

    /** 提交用户 ID */
    @Schema(description = "提交用户 ID")
    private Long userId;

    /** 提交用户昵称 */
    @Schema(description = "提交用户昵称")
    private String nickname;

    /** 题型 */
    @Schema(description = "题型", example = "writing")
    private String type;

    /** 标题快照 */
    @Schema(description = "标题快照")
    private String title;

    /** 题目 ID(可跳转后台编辑) */
    @Schema(description = "题目 ID")
    private String questionId;

    /** 批改状态 */
    @Schema(description = "批改状态:graded/partially_graded/pending")
    private String status;

    /** 批改方式:auto 会被定时任务自动出分,manual 需人工处理 */
    @Schema(description = "批改方式:ai/auto/manual")
    private String gradingMethod;

    /** 得分(未批改为空) */
    @Schema(description = "得分(未批改为空)")
    private BigDecimal score;

    /** 总分快照 */
    @Schema(description = "总分快照", example = "15.0")
    private BigDecimal totalScore;

    /** 已作答的题数(客观题;写作为 1) */
    @Schema(description = "已作答题数")
    private Integer answeredCount;

    /** 等待时长(分钟,自提交起算) */
    @Schema(description = "等待时长(分钟)")
    private Long waitingMinutes;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    /** 批改完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "批改完成时间")
    private LocalDateTime gradedAt;
}
