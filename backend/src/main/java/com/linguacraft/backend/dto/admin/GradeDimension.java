package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 作文评分维度(人工与模考两条判分路径共用)。
 *
 * <p>JSON 形状 {@code {name, score, comment}} 与学生端渲染器(练习 SubmitResult 与模考报告)
 * 的隐式契约一致,因此由唯一写手 {@code WritingGradeSupport} 产出,避免两处各自拼装漂移。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "作文评分维度")
public class GradeDimension {

    /** 维度名称(取自题目的 scoringDimensions) */
    @NotEmpty(message = "维度名称不能为空")
    @Schema(description = "维度名称", example = "内容要点")
    private String name;

    /** 该维度得分 */
    @NotNull(message = "维度得分不能为空")
    @Schema(description = "该维度得分", example = "4.0")
    private BigDecimal score;

    /** 该维度评语 */
    @Schema(description = "该维度评语(可空)")
    private String comment;
}
