package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.linguacraft.backend.ai.AiGradingService;
import com.linguacraft.backend.ai.GradedWriting;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.AdminSubmissionDetailDto;
import com.linguacraft.backend.dto.admin.GradeDimension;
import com.linguacraft.backend.dto.admin.ManualGradeRequest;
import com.linguacraft.backend.dto.admin.WritingPreviewDto;
import com.linguacraft.backend.dto.practice.WritingDimensionDto;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 人工判分落库口径测试(阶段 A4)。
 *
 * <p>覆盖四件关键事:非待批改拒绝、题号集合必须与学生作答一致、CAS 冲突不覆盖已有判分、
 * 作文维度分之和不得超过卷面总分。mapper 全部桩化,不连库。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminSubmissionServiceTest {

    @Mock
    private PracticeSubmissionMapper submissionMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AiGradingService aiGradingService;

    private AdminSubmissionService newService() {
        ObjectMapper mapper = new ObjectMapper();
        return new AdminSubmissionService(submissionMapper, questionMapper, userMapper, mapper,
                new WritingGradeSupport(mapper), aiGradingService);
    }

    /** 一条待批改的客观题提交(学生作答了题号 1 与 2) */
    private PracticeSubmission pendingObjective() {
        PracticeSubmission submission = new PracticeSubmission();
        submission.setId(17L);
        submission.setUserId(1L);
        submission.setQuestionId("cloze-teacher-1");
        submission.setType("cloze");
        submission.setTitle("完形填空 · 演示");
        submission.setStatus("pending");
        submission.setGradingMethod("manual");
        submission.setTotalScore(new BigDecimal("20.0"));
        submission.setAnswers("{\"1\":\"A\",\"2\":\"C\"}");
        submission.setEstimatedTime("预计 24 小时内出结果");
        submission.setSubmittedAt(LocalDateTime.now().minusHours(3));
        return submission;
    }

    private Question clozeQuestion() {
        Question question = new Question();
        question.setId("cloze-teacher-1");
        question.setType("cloze");
        question.setContent("{\"passage\":\"It [[1]] a fine day.\",\"blanks\":[{\"id\":1},{\"id\":2}]}");
        question.setAnswerKey("{\"blanks\":[{\"id\":1,\"answer\":\"A\"},{\"id\":2,\"answer\":\"B\"}]}");
        question.setHasAnswer(true);
        question.setPerQuestionScore(new BigDecimal("2.0"));
        question.setTotalScore(new BigDecimal("20.0"));
        return question;
    }

    private ManualGradeRequest.Item item(int id, String correctAnswer, boolean isCorrect) {
        return ManualGradeRequest.Item.builder().id(id).correctAnswer(correctAnswer).isCorrect(isCorrect)
                .explanation("演示解析").build();
    }

    @Test
    @DisplayName("已批改的提交不能被再次人工判分")
    void rejectsNonPendingSubmission() {
        PracticeSubmission graded = pendingObjective();
        graded.setStatus("graded");
        when(submissionMapper.selectById(17L)).thenReturn(graded);

        assertThatThrownBy(() -> newService().grade(17L, ManualGradeRequest.builder()
                .items(List.of(item(1, "A", true))).build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有待批改(pending)可人工判分");

        verify(submissionMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("判定题号包含学生未作答的题号即拒绝")
    void rejectsUnknownItemIds() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingObjective());

        assertThatThrownBy(() -> newService().grade(17L, ManualGradeRequest.builder()
                .items(List.of(item(1, "A", true), item(9, "Z", false))).build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("学生并未作答该题");
    }

    @Test
    @DisplayName("判定题号少于学生作答即拒绝(不允许漏判)")
    void rejectsMissingItemIds() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingObjective());

        assertThatThrownBy(() -> newService().grade(17L, ManualGradeRequest.builder()
                .items(List.of(item(1, "A", true))).build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("items 题号集合必须与学生作答一致");
    }

    @Test
    @DisplayName("合格判分走 CAS 更新:得分=单题分值×正确数")
    void gradesWithCasUpdate() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingObjective());
        when(questionMapper.selectById("cloze-teacher-1")).thenReturn(clozeQuestion());
        when(submissionMapper.update(isNull(), any())).thenReturn(1);

        AdminSubmissionDetailDto detail = newService().grade(17L, ManualGradeRequest.builder()
                .items(List.of(item(1, "A", true), item(2, "B", false))).build());

        verify(submissionMapper).update(isNull(), any(Wrapper.class));
        // 判分材料应一并下发给判分页:参考答案、题目内容、学生作答
        assertThat(detail.getQuestionHasAnswer()).isTrue();
        assertThat(detail.getAnswerKey()).isNotNull();
        assertThat(detail.getAnswers()).isNotNull();
    }

    @Test
    @DisplayName("CAS 未命中(已被定时任务或他人处理)即拒绝且不落库")
    void rejectsWhenCasMisses() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingObjective());
        when(questionMapper.selectById("cloze-teacher-1")).thenReturn(clozeQuestion());
        when(submissionMapper.update(isNull(), any())).thenReturn(0);

        assertThatThrownBy(() -> newService().grade(17L, ManualGradeRequest.builder()
                .items(List.of(item(1, "A", true), item(2, "B", false))).build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("该提交已被处理");
    }

    @Test
    @DisplayName("作文维度分之和超过卷面总分即拒绝")
    void rejectsWritingDimensionSumOverCap() {
        PracticeSubmission writing = pendingObjective();
        writing.setType("writing");
        writing.setTotalScore(new BigDecimal("15.0"));
        writing.setAnswers("{\"content\":\"My email text.\"}");
        when(submissionMapper.selectById(17L)).thenReturn(writing);

        assertThatThrownBy(() -> newService().grade(17L, ManualGradeRequest.builder()
                .dimensions(List.of(
                        GradeDimension.builder().name("内容要点").score(new BigDecimal("8")).build(),
                        GradeDimension.builder().name("语言表达").score(new BigDecimal("8")).build()))
                .build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("超过卷面总分");
    }

    @Test
    @DisplayName("作文必须提交评分维度")
    void requiresWritingDimensions() {
        PracticeSubmission writing = pendingObjective();
        writing.setType("writing");
        when(submissionMapper.selectById(17L)).thenReturn(writing);

        assertThatThrownBy(() -> newService().grade(17L, ManualGradeRequest.builder().overallComment("总评").build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("作文题必须提交至少一个评分维度");
    }

    // ==================== AI 预评(阶段 A7) ====================

    /** 一条待批改的写作提交(有正文,满分 15) */
    private PracticeSubmission pendingWriting() {
        PracticeSubmission submission = pendingObjective();
        submission.setType("writing");
        submission.setQuestionId("writing-teacher-1");
        submission.setAnswers("{\"content\":\"Dear Mr. Smith, I am writing to apply for the volunteer job.\"}");
        submission.setTotalScore(new BigDecimal("15.0"));
        return submission;
    }

    private Question writingQuestion() {
        Question question = new Question();
        question.setId("writing-teacher-1");
        question.setType("writing");
        question.setContent("{\"prompt\":\"申请英语角志愿者\",\"scoringDimensions\":[\"内容要点\",\"语言表达\"]}");
        question.setAnswerKey("{\"sampleAnswer\":\"范文\"}");
        question.setHasAnswer(false);
        question.setTotalScore(new BigDecimal("15.0"));
        return question;
    }

    @Test
    @DisplayName("客观题不该被送来 AI 预评(它们由答案键比对出分)")
    void previewRejectsObjectiveType() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingObjective());

        assertThatThrownBy(() -> newService().aiPreview(17L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有写作题需要 AI 预评");
        verify(aiGradingService, never()).gradeWriting(any(), any());
    }

    @Test
    @DisplayName("已批改的写作件不预评")
    void previewRejectsGradedSubmission() {
        PracticeSubmission graded = pendingWriting();
        graded.setStatus("graded");
        when(submissionMapper.selectById(17L)).thenReturn(graded);

        assertThatThrownBy(() -> newService().aiPreview(17L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI 预评只对待批改(pending)的件有效");
    }

    @Test
    @DisplayName("写作题本体缺失时拒绝,而不是拿空题干去问 AI")
    void previewRejectsMissingQuestion() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingWriting());
        when(questionMapper.selectById("writing-teacher-1")).thenReturn(null);

        assertThatThrownBy(() -> newService().aiPreview(17L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("写作题不存在");
        verify(aiGradingService, never()).gradeWriting(any(), any());
    }

    @Test
    @DisplayName("预评返回草稿、总分按维度实算并截到题面满分,且不写库")
    void previewReturnsDraftWithoutWriting() {
        when(submissionMapper.selectById(17L)).thenReturn(pendingWriting());
        when(questionMapper.selectById("writing-teacher-1")).thenReturn(writingQuestion());
        when(aiGradingService.gradeWriting(any(Question.class), any(String.class))).thenReturn(new GradedWriting(
                List.of(WritingDimensionDto.builder().name("内容要点").score(new BigDecimal("8.0")).comment("还行").build(),
                        WritingDimensionDto.builder().name("语言表达").score(new BigDecimal("8.0")).comment("还行").build()),
                "AI 范文", "AI 总评", new BigDecimal("16.0")));

        WritingPreviewDto preview = newService().aiPreview(17L);

        assertThat(preview.getCap()).isEqualByComparingTo("15.0");
        // 总分取维度实算(16.0)再截到满分,而不是 AI 自报的那个数
        assertThat(preview.getScore()).isEqualByComparingTo("15.0");
        assertThat(preview.getDimensions()).hasSize(2);
        assertThat(preview.getDimensions().get(0).getName()).isEqualTo("内容要点");
        assertThat(preview.getSampleAnswer()).isEqualTo("AI 范文");
        // 预评不许碰库:写了 writing_result 或 status 学生就会看到"已批改"
        verify(submissionMapper, never()).update(isNull(), any());
        verify(submissionMapper, never()).updateById(any(PracticeSubmission.class));
    }
}
