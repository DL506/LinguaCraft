package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 得分趋势点(对应前端 UserStats.scoreTrend 元素;日期格式 MM-dd)。
 *
 * <p>口径:值为"截至当日"的累计得分率(0-100),无数据的日期沿用前一有效值,便于趋势展示。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreTrendDto {

    /** 日期(MM-dd) */
    @Schema(description = "日期(MM-dd)", example = "09-27")
    private String date;

    /** 阅读理解累计得分率(0-100) */
    @Schema(description = "阅读理解累计得分率(0-100)")
    private Integer reading;

    /** 五选五累计得分率(0-100) */
    @Schema(description = "五选五累计得分率(0-100)")
    private Integer matching;

    /** 完形填空累计得分率(0-100) */
    @Schema(description = "完形填空累计得分率(0-100)")
    private Integer cloze;

    /** 语法填空累计得分率(0-100) */
    @Schema(description = "语法填空累计得分率(0-100)")
    private Integer grammar;

    /** 作文累计得分率(0-100) */
    @Schema(description = "作文累计得分率(0-100)")
    private Integer writing;
}