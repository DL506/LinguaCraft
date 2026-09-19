package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交结果响应(对应前端 SubmitResult / WritingSubmitResult;所有 submit 接口统一返回)。
 *
 * <p>null 字段不序列化(与前端 mock 的 undefined 语义一致):
 * details 仅在 status=graded 时出现;pending 时出现 estimatedTime;
 * 写作扩展字段(dimensions/sampleAnswer/overallComment)仅写作题批改后出现。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitResultResponse {

    /** 提交记录 ID(对外为字符串) */
    @Schema(description = "提交记录 ID(字符串)")
    private Long submissionId;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 批改状态 */
    @Schema(description = "批改状态:graded/partially_graded/pending", example = "graded")
    private String status;

    /** 实际采用的批改方式 */
    @Schema(description = "批改方式:ai/auto/manual", example = "ai")
    private String gradingMethod;

    /** 客观题是否已批改(整卷模考使用) */
    @Schema(description = "客观题是否已批改(整卷模考使用)")
    private Boolean objectiveGraded;

    /** 写作是否已批改(整卷模考使用) */
    @Schema(description = "写作是否已批改(整卷模考使用)")
    private Boolean writingGraded;

    /** 得分 */
    @Schema(description = "得分")
    private BigDecimal score;

    /** 总分 */
    @Schema(description = "总分")
    private BigDecimal totalScore;

    /** 正确数 */
    @Schema(description = "正确数")
    private Integer correctCount;

    /** 总题数 */
    @Schema(description = "总题数")
    private Integer totalCount;

    /** 逐题批改明细(仅批改完成后下发,含答案与解析) */
    @Schema(description = "逐题批改明细(仅批改完成后下发)")
    private List<SubmitDetailDto> details;

    /** 预计出结果说明(待批改时返回) */
    @Schema(description = "预计出结果说明(待批改时返回)", example = "预计 24 小时内出结果")
    private String estimatedTime;

    /** 写作:评分维度(写作题批改后) */
    @Schema(description = "写作:评分维度(写作题批改后)")
    private List<WritingDimensionDto> dimensions;

    /** 写作:范文(写作题批改后) */
    @Schema(description = "写作:范文(写作题批改后)")
    private String sampleAnswer;

    /** 写作:总评(写作题批改后) */
    @Schema(description = "写作:总评(写作题批改后)")
    private String overallComment;
}