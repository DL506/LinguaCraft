package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.AdminExamSubmissionDetailDto;
import com.linguacraft.backend.dto.admin.ExamWritingGradeRequest;
import com.linguacraft.backend.dto.admin.GradeDimension;
import com.linguacraft.backend.entity.ExamPaper;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
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
 * 模考作文人工判分测试(阶段 A4-②)。
 *
 * <p>重点是"什么时候不许判":作文已出分、整卷已批改、缺客观分、没写正文、分值越界、
 * 以及与 AI 异步批改器的 CAS 竞争。判分成功路径只断言走了 CAS 更新并回出材料。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminExamSubmissionServiceTest {

    @Mock
    private ExamSubmissionMapper submissionMapper;

    @Mock
    private ExamPaperMapper paperMapper;

    @Mock
    private UserMapper userMapper;

    private AdminExamSubmissionService newService() {
        ObjectMapper mapper = new ObjectMapper();
        return new AdminExamSubmissionService(submissionMapper, paperMapper, userMapper, mapper,
                new WritingGradeSupport(mapper));
    }

    /** 一份待判作文的模考提交:客观 48 分已算好,写作未出分 */
    private ExamSubmission awaitingWriting() {
        ExamSubmission submission = new ExamSubmission();
        submission.setId(1L);
        submission.setUserId(1L);
        submission.setExamId("exam-2026");
        submission.setStatus("partially_graded");
        submission.setGradingMethod("ai");
        submission.setObjectiveScore(new BigDecimal("48.0"));
        submission.setTotalScore(new BigDecimal("100.0"));
        submission.setWriting("{\"content\":\"Dear Mr. Smith, I am writing to apply...\"}");
        submission.setSectionScores("{\"1\":22,\"2\":2,\"3\":24,\"4\":0}");
        submission.setSubmittedAt(LocalDateTime.now().minusHours(5));
        return submission;
    }

    /** 写作部分满分 15 的试卷 content */
    private ExamPaper paper() {
        ExamPaper paper = new ExamPaper();
        paper.setId("exam-2026");
        paper.setYear(2026);
        paper.setTitle("2026 年广东专插本英语模拟卷");
        paper.setTotalScore(new BigDecimal("100.0"));
        paper.setContent("""
                {"sections":[
                  {"part":1,"type":"reading","score":30,"content":{"passage":"..."}},
                  {"part":5,"type":"writing","score":15,"content":{"prompt":"申请英语角志愿者",
                    "requirements":["要点一"],"wordLimit":120,"scoringDimensions":["内容要点","语言表达","篇章结构","格式规范"]}}
                ]}
                """);
        paper.setAnswerKey("{\"sections\":[{\"part\":5,\"type\":\"writing\",\"score\":15}]}");
        return paper;
    }

    private GradeDimension dimension(String name, int score) {
        return GradeDimension.builder().name(name).score(new BigDecimal(String.valueOf(score))).comment("评语").build();
    }

    private ExamWritingGradeRequest request(GradeDimension... dimensions) {
        return ExamWritingGradeRequest.builder().dimensions(List.of(dimensions)).build();
    }

    @Test
    @DisplayName("写作部分满分取自试卷的 15 分")
    void readsWritingCapFromPaper() {
        when(submissionMapper.selectById(1L)).thenReturn(awaitingWriting());
        when(paperMapper.selectById("exam-2026")).thenReturn(paper());

        AdminExamSubmissionDetailDto detail = newService().detail(1L);

        assertThat(detail.getWritingFullScore()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(detail.getWritingContent()).contains("Dear Mr. Smith");
        assertThat(detail.getWritingSection().path("prompt").asText("")).contains("英语角");
        assertThat(detail.getWritingGraded()).isFalse();
    }

    @Test
    @DisplayName("试卷缺失时写作满分兜底 15,材料接口不炸")
    void toleratesMissingPaper() {
        when(submissionMapper.selectById(1L)).thenReturn(awaitingWriting());
        when(paperMapper.selectById("exam-2026")).thenReturn(null);

        AdminExamSubmissionDetailDto detail = newService().detail(1L);

        assertThat(detail.getWritingFullScore()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(detail.getPaperTitle()).isEqualTo("exam-2026");
    }

    @Test
    @DisplayName("作文已出分时不许重复判分")
    void rejectsAlreadyGradedWriting() {
        ExamSubmission graded = awaitingWriting();
        graded.setWritingGradedAt(LocalDateTime.now());
        graded.setWritingScore(new BigDecimal("7.0"));
        graded.setStatus("graded");
        when(submissionMapper.selectById(1L)).thenReturn(graded);

        assertThatThrownBy(() -> newService().gradeWriting(1L,
                request(dimension("内容要点", 8))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能重复判分");
    }

    @Test
    @DisplayName("没写正文的提交不可判分")
    void rejectsEmptyWritingContent() {
        ExamSubmission blank = awaitingWriting();
        blank.setWriting("{\"content\":\"   \"}");
        when(submissionMapper.selectById(1L)).thenReturn(blank);

        assertThatThrownBy(() -> newService().gradeWriting(1L, request(dimension("内容要点", 8))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("该提交没有作文作答,无需人工批改");
    }

    @Test
    @DisplayName("客观分缺失即拒绝(否则会拼出错误总分)")
    void rejectsMissingObjectiveScore() {
        ExamSubmission broken = awaitingWriting();
        broken.setObjectiveScore(null);
        when(submissionMapper.selectById(1L)).thenReturn(broken);

        assertThatThrownBy(() -> newService().gradeWriting(1L, request(dimension("内容要点", 8))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("还没有客观题得分");
    }

    @Test
    @DisplayName("维度分之和超过写作满分即拒绝")
    void rejectsDimensionSumOverCap() {
        when(submissionMapper.selectById(1L)).thenReturn(awaitingWriting());
        when(paperMapper.selectById("exam-2026")).thenReturn(paper());

        assertThatThrownBy(() -> newService().gradeWriting(1L,
                request(dimension("内容要点", 8), dimension("语言表达", 9))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("超过作文满分");

        verify(submissionMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("得分覆盖超过写作满分即拒绝")
    void rejectsScoreOverrideOverCap() {
        when(submissionMapper.selectById(1L)).thenReturn(awaitingWriting());
        when(paperMapper.selectById("exam-2026")).thenReturn(paper());

        ExamWritingGradeRequest tooHigh = ExamWritingGradeRequest.builder()
                .dimensions(List.of(dimension("内容要点", 5)))
                .score(new BigDecimal("16"))
                .build();

        assertThatThrownBy(() -> newService().gradeWriting(1L, tooHigh))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("作文得分须在 0~15 之间");
    }

    @Test
    @DisplayName("CAS 未命中(AI 异步批改抢先出分)即拒绝")
    void rejectsWhenCasMisses() {
        when(submissionMapper.selectById(1L)).thenReturn(awaitingWriting());
        when(paperMapper.selectById("exam-2026")).thenReturn(paper());
        when(submissionMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);

        assertThatThrownBy(() -> newService().gradeWriting(1L, request(dimension("内容要点", 7))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("该提交已被批改或作文已出分");
    }

    @Test
    @DisplayName("合格判分走 CAS 更新并回出判分材料")
    void gradesWithCasUpdate() {
        when(submissionMapper.selectById(1L)).thenReturn(awaitingWriting());
        when(paperMapper.selectById("exam-2026")).thenReturn(paper());
        when(submissionMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        ExamWritingGradeRequest body = ExamWritingGradeRequest.builder()
                .dimensions(List.of(dimension("内容要点", 4), dimension("语言表达", 3)))
                .overallComment("总评:审题准确,语言通顺。")
                .build();
        AdminExamSubmissionDetailDto detail = newService().gradeWriting(1L, body);

        verify(submissionMapper).update(isNull(), any(Wrapper.class));
        assertThat(detail.getObjectiveScore()).isEqualByComparingTo(new BigDecimal("48.0"));
        assertThat(detail.getWritingSection().path("scoringDimensions").isArray()).isTrue();
    }

    @Test
    @DisplayName("队列状态筛选拒绝非法值")
    void rejectsUnknownStatus() {
        assertThatThrownBy(() -> newService().list("finished", null, 1, 20))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("status 仅接受 pending/graded/partially_graded");
    }

    @Test
    @DisplayName("提交不存在时 404")
    void throwsNotFoundForMissingSubmission() {
        when(submissionMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> newService().detail(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("模考提交不存在");
    }
}
