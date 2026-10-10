package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.linguacraft.backend.dto.admin.AdminDashboardDto;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.entity.UserWordProgress;
import com.linguacraft.backend.entity.UserWordReview;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import com.linguacraft.backend.mapper.GrammarSectionMapper;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import com.linguacraft.backend.mapper.UserWordReviewMapper;
import com.linguacraft.backend.mapper.VocabBookMapper;
import com.linguacraft.backend.mapper.VocabWordMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运营看板服务(阶段 A5):把后台各处的计数汇总成一张概览。
 *
 * <p>口径纪律(看板上每个数字都必须能在学生端复现,所以一律沿用既有口径,不自立一套):</p>
 * <ul>
 *   <li>"今日" = {@code created_at >= 今日零点},与首页今日任务和学生端统计同口径
 *       (见 {@code HomeService.countTodayByType} 与 {@code UserService.stats};两者都用 created_at 而非 submitted_at);</li>
 *   <li>{@code practicePending} 只统计练习提交,与学生端 {@code stats.pendingGradingCount} 一致;
 *       模考的两类欠账单列 {@code examPending} 与 {@code examWritingUngraded},不混进同一个数;</li>
 *   <li>背单词相关计数镜像 {@code VocabService.todayTask()} 的字段语义,但<b>不调用它</b> ——
 *       那个方法按当前登录用户取数,看板要全站数字,而它位于学生热路径,不值得为后台改造。</li>
 * </ul>
 */
@Service
public class AdminDashboardService {

    private final QuestionMapper questionMapper;
    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final UserMapper userMapper;
    private final UserWordProgressMapper wordProgressMapper;
    private final UserWordReviewMapper wordReviewMapper;
    private final VocabWordMapper vocabWordMapper;
    private final VocabBookMapper vocabBookMapper;
    private final GrammarSectionMapper grammarSectionMapper;
    private final MistakeMapper mistakeMapper;

    public AdminDashboardService(QuestionMapper questionMapper,
                                 PracticeSubmissionMapper practiceSubmissionMapper,
                                 ExamSubmissionMapper examSubmissionMapper,
                                 ExamPaperMapper examPaperMapper,
                                 UserMapper userMapper,
                                 UserWordProgressMapper wordProgressMapper,
                                 UserWordReviewMapper wordReviewMapper,
                                 VocabWordMapper vocabWordMapper,
                                 VocabBookMapper vocabBookMapper,
                                 GrammarSectionMapper grammarSectionMapper,
                                 MistakeMapper mistakeMapper) {
        this.questionMapper = questionMapper;
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.examSubmissionMapper = examSubmissionMapper;
        this.examPaperMapper = examPaperMapper;
        this.userMapper = userMapper;
        this.wordProgressMapper = wordProgressMapper;
        this.wordReviewMapper = wordReviewMapper;
        this.vocabWordMapper = vocabWordMapper;
        this.vocabBookMapper = vocabBookMapper;
        this.grammarSectionMapper = grammarSectionMapper;
        this.mistakeMapper = mistakeMapper;
    }

    /**
     * 汇总看板数字(GET /api/admin/dashboard)。
     *
     * @return 概览计数
     */
    public AdminDashboardDto summary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return AdminDashboardDto.builder()
                .questionOnline(count(questionMapper.selectCount(
                        Wrappers.<Question>lambdaQuery().eq(Question::getStatus, 1))))
                .questionTotal(count(questionMapper.selectCount(null)))
                .questionCreatedToday(count(questionMapper.selectCount(
                        Wrappers.<Question>lambdaQuery().ge(Question::getCreatedAt, todayStart))))
                .questionAiTotal(count(questionMapper.selectCount(
                        Wrappers.<Question>lambdaQuery().eq(Question::getSourceType, "ai"))))
                .practicePending(count(practiceSubmissionMapper.selectCount(
                        Wrappers.<PracticeSubmission>lambdaQuery().eq(PracticeSubmission::getStatus, "pending"))))
                .examPending(count(examSubmissionMapper.selectCount(
                        Wrappers.<ExamSubmission>lambdaQuery().eq(ExamSubmission::getStatus, "pending"))))
                // 客观分已出但写作未出分:AI 作文评分失败留下的死档,由 A4-② 人工判
                .examWritingUngraded(count(examSubmissionMapper.selectCount(
                        Wrappers.<ExamSubmission>lambdaQuery()
                                .eq(ExamSubmission::getStatus, "partially_graded")
                                .isNull(ExamSubmission::getWritingGradedAt))))
                .practiceOldestPendingMinutes(oldestPracticePendingMinutes())
                .userTotal(count(userMapper.selectCount(null)))
                .userDisabled(count(userMapper.selectCount(
                        Wrappers.<User>lambdaQuery().eq(User::getEnabled, false))))
                .userAdmin(count(userMapper.selectCount(
                        Wrappers.<User>lambdaQuery().eq(User::getRole, "admin"))))
                .submissionPracticeToday(count(practiceSubmissionMapper.selectCount(
                        Wrappers.<PracticeSubmission>lambdaQuery().ge(PracticeSubmission::getCreatedAt, todayStart))))
                .submissionExamToday(count(examSubmissionMapper.selectCount(
                        Wrappers.<ExamSubmission>lambdaQuery().ge(ExamSubmission::getCreatedAt, todayStart))))
                .wordLearnedTotal(count(wordProgressMapper.selectCount(null)))
                .wordLearnedToday(count(wordProgressMapper.selectCount(
                        Wrappers.<UserWordProgress>lambdaQuery().ge(UserWordProgress::getFirstLearnedAt, todayStart))))
                .wordReviewedToday(count(wordReviewMapper.selectCount(
                        Wrappers.<UserWordReview>lambdaQuery().ge(UserWordReview::getReviewedAt, todayStart))))
                .vocabWordTotal(count(vocabWordMapper.selectCount(null)))
                .vocabBookTotal(count(vocabBookMapper.selectCount(null)))
                .grammarSectionTotal(count(grammarSectionMapper.selectCount(null)))
                .examPaperTotal(count(examPaperMapper.selectCount(null)))
                .mistakeTotal(count(mistakeMapper.selectCount(null)))
                .build();
    }

    /**
     * 练习待批改里等得最久的分钟数。
     *
     * @return 分钟数;没有待批改记录时为 0
     */
    private long oldestPracticePendingMinutes() {
        List<PracticeSubmission> oldest = practiceSubmissionMapper.selectList(
                Wrappers.<PracticeSubmission>lambdaQuery()
                        .eq(PracticeSubmission::getStatus, "pending")
                        .orderByAsc(PracticeSubmission::getSubmittedAt)
                        .last("LIMIT 1"));
        if (oldest.isEmpty() || oldest.get(0).getSubmittedAt() == null) {
            return 0L;
        }
        return Duration.between(oldest.get(0).getSubmittedAt(), LocalDateTime.now()).toMinutes();
    }

    /**
     * MyBatis-Plus 的 selectCount 返回 Long(可能为 null),统一收成 int。
     *
     * @param value 计数结果
     * @return 计数
     */
    private int count(Long value) {
        return value == null ? 0 : value.intValue();
    }
}
