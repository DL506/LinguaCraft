package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 模考作文人工批改请求(阶段 A4-②)。
 *
 * <p>只判写作部分:客观题在交卷时已算好并落库,这里补的是 AI 批改失败后留在
 * {@code partially_graded} 的那一段。JSON 形状由 {@code WritingGradeSupport} 统一产出,
 * 与练习作文的人工批改一致。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "模考作文人工批改请求")
public class ExamWritingGradeRequest {

    /** 评分维度(必填,通常取自试卷写作部分的 scoringDimensions) */
    @Valid
    @NotEmpty(message = "作文批改必须提交评分维度(dimensions)")
    @Schema(description = "作文评分维度")
    private List<GradeDimension> dimensions;

    /** 写作得分覆盖(可空,留空按维度合计) */
    @Schema(description = "写作得分覆盖(可空,封顶写作部分分值)", example = "11.0")
    private BigDecimal score;

    /** 参考范文 */
    @Schema(description = "参考范文(可空)")
    private String sampleAnswer;

    /** 总评 */
    @Schema(description = "作文总评")
    private String overallComment;
}
