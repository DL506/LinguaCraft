package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单题批改明细(对应前端 SubmitDetail;答案与解析只在此结构下发)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitDetailDto {

    /** 题号/空位编号 */
    @Schema(description = "题号/空位编号", example = "1")
    private Integer id;

    /** 用户作答 */
    @Schema(description = "用户作答", example = "B")
    private String userAnswer;

    /** 正确答案 */
    @Schema(description = "正确答案", example = "B")
    private String correctAnswer;

    /** 是否答对 */
    @Schema(description = "是否答对", example = "true")
    private Boolean isCorrect;

    /** 解析 */
    @Schema(description = "解析")
    private String explanation;
}