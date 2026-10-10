package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人工批改请求(阶段 A4):管理员对一条待批改提交给出判分结果。
 *
 * <p>按提交题型二选一,服务端会校验:</p>
 * <ul>
 *   <li>客观题(reading/matching/cloze/grammar-fill):提交 {@code items},题号集合必须与学生实际作答一致;</li>
 *   <li>作文(writing):提交 {@code dimensions}(+ 可选 {@code sampleAnswer}/{@code overallComment})。</li>
 * </ul>
 *
 * <p>分值口径与自动判分一致:客观题得分 = 单题分值 × 正确数(可用 {@code score} 覆盖,封顶卷面总分);
 * 作文得分 = 各维度分之和(封顶卷面总分)。落库后学生端「我的提交」无需改动即可显示结果。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "人工批改请求")
public class ManualGradeRequest {

    /** 客观题逐题判定 */
    @Valid
    @Schema(description = "客观题逐题判定(非写作题必填)")
    private List<Item> items;

    /** 得分覆盖(可空;留空则由服务端按正确数累计) */
    @Schema(description = "得分覆盖(可空,封顶卷面总分)", example = "12.0")
    private BigDecimal score;

    /** 作文评分维度(写作题必填) */
    @Valid
    @Schema(description = "作文评分维度(写作题必填)")
    private List<GradeDimension> dimensions;

    /** 作文参考范文 */
    @Schema(description = "作文参考范文(可空)")
    private String sampleAnswer;

    /** 作文总评 */
    @Schema(description = "作文总评")
    private String overallComment;

    /**
     * 客观题单题判定项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "客观题单题判定")
    public static class Item {

        /** 题号/空位编号(与学生作答一致) */
        @NotNull(message = "题号 id 不能为空")
        @Schema(description = "题号/空位编号", example = "1")
        private Integer id;

        /** 正确答案(自由文本或选项 key) */
        @Schema(description = "正确答案(选项 key 或自由文本)", example = "B")
        private String correctAnswer;

        /** 是否答对 */
        @NotNull(message = "isCorrect 不能为空")
        @Schema(description = "是否答对", example = "true")
        private Boolean isCorrect;

        /** 评语/解析 */
        @Schema(description = "评语或解析(可空)")
        private String explanation;
    }
}
