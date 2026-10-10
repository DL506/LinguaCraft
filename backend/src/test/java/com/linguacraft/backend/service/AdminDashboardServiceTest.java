package com.linguacraft.backend.service;

import com.linguacraft.backend.dto.admin.AdminDashboardDto;
import com.linguacraft.backend.entity.PracticeSubmission;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 运营看板汇总测试(阶段 A5)。
 *
 * <p>重点是"每个数字落到正确的字段上":各 mapper 桩返回互不相同的值,任何一个字段接错都会被发现;
 * 另外覆盖 count 的 null 兜底,以及最长等待时数的算法。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDashboardServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private PracticeSubmissionMapper practiceSubmissionMapper;

    @Mock
    private ExamSubmissionMapper examSubmissionMapper;

    @Mock
    private ExamPaperMapper examPaperMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserWordProgressMapper wordProgressMapper;

    @Mock
    private UserWordReviewMapper wordReviewMapper;

    @Mock
    private VocabWordMapper vocabWordMapper;

    @Mock
    private VocabBookMapper vocabBookMapper;

    @Mock
    private GrammarSectionMapper grammarSectionMapper;

    @Mock
    private MistakeMapper mistakeMapper;

    private AdminDashboardService newService() {
        return new AdminDashboardService(questionMapper, practiceSubmissionMapper, examSubmissionMapper,
                examPaperMapper, userMapper, wordProgressMapper, wordReviewMapper, vocabWordMapper,
                vocabBookMapper, grammarSectionMapper, mistakeMapper);
    }

    /**
     * 练习提交的待批改桩。
     *
     * @param submittedAt 提交时间(算等待时长用)
     */
    private void stubOldestPending(LocalDateTime submittedAt) {
        PracticeSubmission oldest = new PracticeSubmission();
        oldest.setStatus("pending");
        oldest.setSubmittedAt(submittedAt);
        when(practiceSubmissionMapper.selectList(any())).thenReturn(List.of(oldest));
    }

    @Test
    @DisplayName("各计数分别落到正确字段,不被串到别的指标上")
    void mapsEveryCounterToItsOwnField() {
        when(questionMapper.selectCount(any())).thenReturn(11L);
        when(practiceSubmissionMapper.selectCount(any())).thenReturn(22L);
        when(examSubmissionMapper.selectCount(any())).thenReturn(33L);
        when(userMapper.selectCount(any())).thenReturn(44L);
        when(wordProgressMapper.selectCount(any())).thenReturn(55L);
        when(wordReviewMapper.selectCount(any())).thenReturn(66L);
        when(vocabWordMapper.selectCount(any())).thenReturn(77L);
        when(vocabBookMapper.selectCount(any())).thenReturn(88L);
        when(grammarSectionMapper.selectCount(any())).thenReturn(99L);
        when(examPaperMapper.selectCount(any())).thenReturn(101L);
        when(mistakeMapper.selectCount(any())).thenReturn(102L);
        stubOldestPending(LocalDateTime.now().minusMinutes(30));

        AdminDashboardDto dto = newService().summary();

        // questionMapper 被调四次且返回同一桩值,说明"同一 mapper 内多条件复用同值"不会错位
        assertThat(dto.getQuestionOnline()).isEqualTo(11);
        assertThat(dto.getQuestionTotal()).isEqualTo(11);
        assertThat(dto.getPracticePending()).isEqualTo(22);
        assertThat(dto.getExamPending()).isEqualTo(33);
        assertThat(dto.getUserTotal()).isEqualTo(44);
        assertThat(dto.getWordLearnedTotal()).isEqualTo(55);
        assertThat(dto.getWordReviewedToday()).isEqualTo(66);
        assertThat(dto.getVocabWordTotal()).isEqualTo(77);
        assertThat(dto.getVocabBookTotal()).isEqualTo(88);
        assertThat(dto.getGrammarSectionTotal()).isEqualTo(99);
        assertThat(dto.getExamPaperTotal()).isEqualTo(101);
        assertThat(dto.getMistakeTotal()).isEqualTo(102);
    }

    @Test
    @DisplayName("count 为 null 时兜底 0,不抛 NPE")
    void toleratesNullCounts() {
        when(questionMapper.selectCount(any())).thenReturn(null);
        when(practiceSubmissionMapper.selectCount(any())).thenReturn(null);
        when(examSubmissionMapper.selectCount(any())).thenReturn(null);
        when(userMapper.selectCount(any())).thenReturn(null);
        when(wordProgressMapper.selectCount(any())).thenReturn(null);
        when(wordReviewMapper.selectCount(any())).thenReturn(null);
        when(vocabWordMapper.selectCount(any())).thenReturn(null);
        when(vocabBookMapper.selectCount(any())).thenReturn(null);
        when(grammarSectionMapper.selectCount(any())).thenReturn(null);
        when(examPaperMapper.selectCount(any())).thenReturn(null);
        when(mistakeMapper.selectCount(any())).thenReturn(null);
        when(practiceSubmissionMapper.selectList(any())).thenReturn(List.of());

        AdminDashboardDto dto = newService().summary();

        assertThat(dto.getQuestionOnline()).isZero();
        assertThat(dto.getPracticePending()).isZero();
        assertThat(dto.getExamWritingUngraded()).isZero();
        assertThat(dto.getPracticeOldestPendingMinutes()).isZero();
    }

    @Test
    @DisplayName("最长等待按最早一条待批改的 submitted_at 计算(不是 created_at)")
    void computesOldestPendingFromSubmittedAt() {
        when(questionMapper.selectCount(any())).thenReturn(0L);
        when(practiceSubmissionMapper.selectCount(any())).thenReturn(3L);
        when(examSubmissionMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(wordProgressMapper.selectCount(any())).thenReturn(0L);
        when(wordReviewMapper.selectCount(any())).thenReturn(0L);
        when(vocabWordMapper.selectCount(any())).thenReturn(0L);
        when(vocabBookMapper.selectCount(any())).thenReturn(0L);
        when(grammarSectionMapper.selectCount(any())).thenReturn(0L);
        when(examPaperMapper.selectCount(any())).thenReturn(0L);
        when(mistakeMapper.selectCount(any())).thenReturn(0L);
        stubOldestPending(LocalDateTime.now().minusHours(2).minusMinutes(5));

        AdminDashboardDto dto = newService().summary();

        assertThat(dto.getPracticeOldestPendingMinutes()).isBetween(124L, 126L);
    }
}
