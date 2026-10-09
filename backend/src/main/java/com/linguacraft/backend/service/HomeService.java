package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.linguacraft.backend.dto.home.ExamCountdownResponse;
import com.linguacraft.backend.dto.home.TodayTaskItemDto;
import com.linguacraft.backend.dto.home.TodayTasksResponse;
import com.linguacraft.backend.dto.vocab.VocabTodayTaskDto;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页服务:今日任务完成度与考试倒计时(前端契约 12.2)。
 *
 * <p>口径约定:</p>
 * <ul>
 *   <li>练习类任务(阅读/完形/语法填空)的 done 按"当日提交数"统计,时间字段用 created_at,
 *       与 /api/user/stats 的学习趋势保持同一口径,避免首页与统计页数字打架;</li>
 *   <li>背单词任务的 done 与 target 直接复用 {@link VocabService#todayTask()},不重复实现一套计数,
 *       防止两处算法漂移;</li>
 *   <li>完形与语法填空的目标固定为 1(任务书 11.2),阅读与单词的目标取用户设置。</li>
 * </ul>
 */
@Service
public class HomeService {

    /** 完形填空/语法填空的每日固定目标 */
    private static final int FIXED_PRACTICE_TARGET = 1;

    /** 每日阅读目标缺省值(用户设置缺失时) */
    private static final int DEFAULT_DAILY_READING_GOAL = 1;

    private final UserSettingMapper userSettingMapper;
    private final PracticeSubmissionMapper submissionMapper;
    private final VocabService vocabService;

    /** 考试日期(配置项 lc.exam.date) */
    private final LocalDate examDate;

    /** 备考进度百分比(配置项 lc.exam.progress) */
    private final int examProgress;

    public HomeService(UserSettingMapper userSettingMapper,
                       PracticeSubmissionMapper submissionMapper,
                       VocabService vocabService,
                       @Value("${lc.exam.date}") String examDateConfig,
                       @Value("${lc.exam.progress}") int examProgressConfig) {
        this.userSettingMapper = userSettingMapper;
        this.submissionMapper = submissionMapper;
        this.vocabService = vocabService;
        this.examDate = LocalDate.parse(examDateConfig);
        this.examProgress = examProgressConfig;
    }

    /**
     * 今日任务(GET /api/home/today-tasks):固定四项,顺序与前端展示一致。
     *
     * @return 今日任务(reading/cloze/grammar-fill/vocab)
     */
    public TodayTasksResponse todayTasks() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSetting setting = userSettingMapper.selectById(userId);
        int readingTarget = setting != null && setting.getDailyReadingGoal() != null
                ? setting.getDailyReadingGoal() : DEFAULT_DAILY_READING_GOAL;

        // 背单词的完成数与目标直接复用背单词模块口径,避免两处实现漂移
        VocabTodayTaskDto vocabTask = vocabService.todayTask();

        List<TodayTaskItemDto> items = new ArrayList<>();
        items.add(new TodayTaskItemDto("reading", readingTarget, countTodayByType(userId, "reading")));
        items.add(new TodayTaskItemDto("cloze", FIXED_PRACTICE_TARGET, countTodayByType(userId, "cloze")));
        items.add(new TodayTaskItemDto("grammar-fill", FIXED_PRACTICE_TARGET,
                countTodayByType(userId, "grammar-fill")));
        items.add(new TodayTaskItemDto("vocab", vocabTask.getTarget(), vocabTask.getDoneCount()));
        return TodayTasksResponse.builder().items(items).build();
    }

    /**
     * 考试倒计时(GET /api/home/exam-countdown)。
     *
     * <p>剩余天数按考试当天 09:00 与当前时刻之差向上取整,并限制最小为 0
     * (与前端 mock 的算法逐位一致)。</p>
     *
     * @return 考试日期、剩余天数与备考进度
     */
    public ExamCountdownResponse examCountdown() {
        LocalDateTime examMoment = examDate.atTime(9, 0);
        long remainingMillis = Duration.between(LocalDateTime.now(), examMoment).toMillis();
        int daysLeft = (int) Math.max(0, Math.ceil(remainingMillis / 86_400_000.0));
        return ExamCountdownResponse.builder()
                .examDate(examDate.toString())
                .daysLeft(daysLeft)
                .progress(examProgress)
                .build();
    }

    /**
     * 统计当前用户当日某种题型的提交数。
     *
     * @param userId 用户 ID
     * @param type   题型
     * @return 当日提交数
     */
    private int countTodayByType(Long userId, String type) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return Math.toIntExact(submissionMapper.selectCount(
                Wrappers.<PracticeSubmission>lambdaQuery()
                        .eq(PracticeSubmission::getUserId, userId)
                        .eq(PracticeSubmission::getType, type)
                        .ge(PracticeSubmission::getCreatedAt, startOfDay)));
    }
}
