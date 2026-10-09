package com.linguacraft.backend.dto.home;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考试倒计时响应(GET /api/home/exam-countdown,对应前端 ExamCountdown)。
 *
 * <p>日期与备考进度取自配置项 {@code lc.exam.date} / {@code lc.exam.progress};
 * 剩余天数按考试当天 09:00 与当前时刻之差向上取整,最小为 0(与前端 mock 算法一致)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考试倒计时响应")
public class ExamCountdownResponse {

    /** 考试日期 yyyy-MM-dd */
    @Schema(description = "考试日期", example = "2027-03-20")
    private String examDate;

    /** 剩余天数(最小 0) */
    @Schema(description = "剩余天数", example = "163")
    private Integer daysLeft;

    /** 备考进度百分比(配置值,后续可改为按学习数据计算) */
    @Schema(description = "备考进度百分比", example = "42")
    private Integer progress;
}
