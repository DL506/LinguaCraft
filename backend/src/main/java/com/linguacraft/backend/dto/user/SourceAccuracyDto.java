package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 各来源正确率(对应前端 UserStats.sourceAccuracy;-1 表示该来源暂无数据)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceAccuracyDto {

    /** 真题来源正确率(0-100;-1 无数据) */
    @Schema(description = "真题来源正确率(0-100;-1 无数据)", example = "70")
    private Integer real;

    /** 模拟题来源正确率(0-100;-1 无数据) */
    @Schema(description = "模拟题来源正确率(0-100;-1 无数据)", example = "75")
    private Integer teacher;

    /** AI 出题来源正确率(0-100;-1 无数据) */
    @Schema(description = "AI 出题来源正确率(0-100;-1 无数据)", example = "-1")
    private Integer ai;
}