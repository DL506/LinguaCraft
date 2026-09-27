package com.linguacraft.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学习统计响应(对应前端 UserStats;口径详见各字段注释)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatsDto {

    /** 累计学习时长(分钟)= 练习提交 duration 之和 / 60 */
    @Schema(description = "累计学习时长(分钟)", example = "1250")
    private Integer totalStudyTime;

    /** 累计背单词数(已有学习进度的单词数,distinct) */
    @Schema(description = "累计背单词数(distinct)", example = "128")
    private Integer totalWords;

    /** 累计阅读篇数(阅读类型提交数) */
    @Schema(description = "累计阅读篇数", example = "12")
    private Integer totalReading;

    /** 总正确率(0-100;客观题正确数之和 / 题数之和) */
    @Schema(description = "总正确率(0-100)", example = "72")
    private Integer accuracy;

    /** 待批改数量(练习 pending;首页提示用) */
    @Schema(description = "待批改数量(练习 pending)", example = "2")
    private Integer pendingGradingCount;

    /** 各题型得分率(grammar 键映射语法填空) */
    @Schema(description = "各题型得分率")
    private TypeAccuracyDto typeAccuracy;

    /** 得分趋势(近 7 天,截至当日累计得分率) */
    @Schema(description = "得分趋势(近 7 天)")
    private List<ScoreTrendDto> scoreTrend;

    /** 各来源正确率(-1 表示无数据) */
    @Schema(description = "各来源正确率(-1 无数据)")
    private SourceAccuracyDto sourceAccuracy;
}