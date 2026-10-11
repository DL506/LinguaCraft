package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 预评结果(阶段 A7):把一份待批改的写作件交给 AI 起草,回填到管理端判分抽屉。
 *
 * <p><b>这个对象只代表建议,不代表结果。</b>两条预评接口都不写数据库、不改 {@code status},
 * 学生看到的仍是「待批改」;只有管理员在抽屉里改完并调用既有的判分接口落库后,才对学生生效。
 * 满分 {@code cap} 一律取自题面(练习=题目总分,模考=试卷写作部分 score),
 * <b>不采信 AI 自报的满分</b>,AI 合计超上限时按 cap 截断。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "AI 预评草稿(未落库,仅供管理员审阅后采纳)")
public class WritingPreviewDto {

    /** AI 建议总分(已按 cap 截断) */
    @Schema(description = "AI 建议总分", example = "7.5")
    private BigDecimal score;

    /** 该件的作文满分(取自题面,不是 AI 返回的值) */
    @Schema(description = "作文满分(题面口径)", example = "15")
    private BigDecimal cap;

    /** AI 给出的逐维度得分与评语,形状与人工判分请求一致,可直接回填 */
    @Schema(description = "评分维度(名称/得分/评语)")
    private List<GradeDimension> dimensions;

    /** AI 给出的参考范文(可空) */
    @Schema(description = "参考范文(可空)")
    private String sampleAnswer;

    /** AI 给出的总评(可空) */
    @Schema(description = "总评(可空)")
    private String overallComment;
}
