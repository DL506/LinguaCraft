package com.linguacraft.backend.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 整卷作答部分(对应前端 ExamSection;content 原样透传)。
 *
 * <p>阅读部分的 content 为 3 篇数组(ReadingPaperContent[]),其余题型为单对象。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamSectionDto {

    /** 部分序号:1-阅读,2-五选五,3-完形,4-语法填空,5-作文 */
    @Schema(description = "部分序号:1-阅读,2-五选五,3-完形,4-语法填空,5-作文", example = "1")
    private Integer part;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 该部分卷面分值 */
    @Schema(description = "该部分卷面分值", example = "30.0")
    private BigDecimal score;

    /** 下发内容(ForAnswer JSON;阅读为 3 篇数组) */
    @Schema(description = "下发内容(ForAnswer JSON;阅读为 3 篇数组)")
    private JsonNode content;
}