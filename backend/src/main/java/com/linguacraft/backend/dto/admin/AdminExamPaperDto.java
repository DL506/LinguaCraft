package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端真题卷行(阶段 A6-③)。
 *
 * <p>列表与详情共用同一结构:列表里 {@code content}/{@code answerKey} 为 null(整卷 JSON 很大,
 * 列表不必下发),详情才带。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端真题卷")
public class AdminExamPaperDto {

    /** 试卷 ID */
    @Schema(description = "试卷 ID", example = "exam-2026")
    private String id;

    /** 年份 */
    @Schema(description = "年份")
    private Integer year;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 卷面总分 */
    @Schema(description = "卷面总分")
    private BigDecimal totalScore;

    /** 各部分分值之和(与 totalScore 不等即为脏数据) */
    @Schema(description = "各部分分值之和")
    private BigDecimal sectionScoreSum;

    /** 考试时长(分钟) */
    @Schema(description = "考试时长(分钟)")
    private Integer duration;

    /** 上下架:1 上架,0 下架 */
    @Schema(description = "上下架:1 上架,0 下架")
    private Integer status;

    /** 部分数(含写作) */
    @Schema(description = "部分数")
    private int sectionCount;

    /** 客观题总卡数(答案 items 数,不含写作) */
    @Schema(description = "客观题卡数")
    private int objectiveItemCount;

    /** 已被交卷次数(>0 不可删除) */
    @Schema(description = "已被交卷次数")
    private int submissionCount;

    /** 卷面 JSON(仅详情下发) */
    @Schema(description = "卷面 JSON(仅详情下发)")
    private JsonNode content;

    /** 答案 JSON(仅详情下发) */
    @Schema(description = "答案 JSON(仅详情下发)")
    private JsonNode answerKey;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
