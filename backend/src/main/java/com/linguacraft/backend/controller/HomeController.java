package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.home.ExamCountdownResponse;
import com.linguacraft.backend.dto.home.TodayTasksResponse;
import com.linguacraft.backend.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页接口:今日任务完成度与考试倒计时(前端契约 12.2)。
 *
 * <p>两接口均需登录,数据全部由既有业务表实时派生,不新增冗余字段。</p>
 */
@Tag(name = "首页", description = "今日任务与考试倒计时(首页概览卡片)")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 今日任务。
     *
     * @return 统一响应:data 为固定四项(reading/cloze/grammar-fill/vocab)的目标与完成数
     */
    @Operation(summary = "今日任务", description = "阅读与单词目标取用户设置,完形/语法填空固定 1;完成数按当日提交与当日复习记录实时统计")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/today-tasks")
    public R<TodayTasksResponse> todayTasks() {
        return R.ok(homeService.todayTasks());
    }

    /**
     * 考试倒计时。
     *
     * @return 统一响应:data 为考试日期、剩余天数与备考进度
     */
    @Operation(summary = "考试倒计时", description = "日期与进度取自配置 lc.exam.date / lc.exam.progress;剩余天数按当天 09:00 向上取整,最小 0")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/exam-countdown")
    public R<ExamCountdownResponse> examCountdown() {
        return R.ok(homeService.examCountdown());
    }
}
