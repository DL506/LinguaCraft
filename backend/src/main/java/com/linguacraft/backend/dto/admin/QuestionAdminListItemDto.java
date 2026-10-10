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
 * 管理端题库列表项(GET /api/admin/questions)。
 *
 * <p>与 student 端的列表项区别:额外暴露 {@code status}/{@code hasAnswer}/{@code answerKey 摘要}
 * 等治理字段,并带 {@code refSubmissionCount} 用于判断能否安全删除。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端题库列表项")
public class QuestionAdminListItemDto {

    /** 题目 ID */
    @Schema(description = "题目 ID", example = "cloze-real-2023-1")
    private String id;

    /** 题型 */
    @Schema(description = "题型", example = "cloze")
    private String type;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 来源类型:real/teacher/ai */
    @Schema(description = "来源类型:real/teacher/ai", example = "real")
    private String sourceType;

    /** 难度 1~5 */
    @Schema(description = "难度 1~5", example = "3")
    private Integer difficulty;

    /** 上架状态:1 上架,0 下架 */
    @Schema(description = "上架状态:1 上架,0 下架", example = "1")
    private Integer status;

    /** 是否具备可比对答案 */
    @Schema(description = "是否具备可比对答案(写作恒为 false)")
    private Boolean hasAnswer;

    /** 练习场景总分 */
    @Schema(description = "练习场景总分", example = "20.0")
    private BigDecimal totalScore;

    /** 单题分值 */
    @Schema(description = "单题分值", example = "2.0")
    private BigDecimal perQuestionScore;

    /** 被练习提交引用的次数(>0 时不允许物理删除) */
    @Schema(description = "被练习提交引用次数(>0 不可删)", example = "3")
    private Long refSubmissionCount;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "更新时间(ISO 本地日期时间)")
    private LocalDateTime updatedAt;
}
