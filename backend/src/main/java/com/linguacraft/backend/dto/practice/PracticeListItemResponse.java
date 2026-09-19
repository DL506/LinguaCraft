package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题型来源列表项(对应前端 PracticeListItem;含最近一次提交状态)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeListItemResponse {

    /** 题目 ID */
    @Schema(description = "题目 ID", example = "reading-real-2023-1")
    private String id;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 来源信息 */
    @Schema(description = "来源信息")
    private SourceDto source;

    /** 标题 */
    @Schema(description = "标题", example = "2023年真题·阅读理解 Passage 1")
    private String title;

    /** 难度 1-5 */
    @Schema(description = "难度 1-5", example = "2")
    private Integer difficulty;

    /** 标签数组 */
    @Schema(description = "标签数组")
    private List<String> tags;

    /** 是否已有提交记录 */
    @Schema(description = "是否已有提交记录", example = "false")
    private boolean submitted;

    /** 最近一次提交 ID(已提交时返回) */
    @Schema(description = "最近一次提交 ID(已提交时返回)")
    private Long submissionId;

    /** 最近一次提交状态(已提交时返回) */
    @Schema(description = "最近一次提交状态:graded/partially_graded/pending")
    private String submissionStatus;

    /** 最近一次得分(已批改时返回) */
    @Schema(description = "最近一次得分(已批改时返回)")
    private BigDecimal score;

    /** 总分 */
    @Schema(description = "总分")
    private BigDecimal totalScore;

    /** 最近一次提交时间(已提交时返回;ISO-8601) */
    @Schema(description = "最近一次提交时间(ISO-8601)")
    private LocalDateTime lastSubmittedAt;
}