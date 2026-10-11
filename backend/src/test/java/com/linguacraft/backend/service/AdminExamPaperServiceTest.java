package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.AdminExamPaperDto;
import com.linguacraft.backend.dto.admin.ExamPaperUpsertRequest;
import com.linguacraft.backend.entity.ExamPaper;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 真题卷管理服务测试(阶段 A6-③)。
 *
 * <p>锁三件事:任何写入都必须先过 JSON 校验器、上下架只接受 0/1、被交卷引用的卷不能删。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminExamPaperServiceTest {

    @Mock
    private ExamPaperMapper paperMapper;

    @Mock
    private ExamSubmissionMapper submissionMapper;

    private AdminExamPaperService newService() {
        return new AdminExamPaperService(paperMapper, submissionMapper, new ObjectMapper(),
                new ExamPaperValidator());
    }

    private ExamPaper paper(String id, int status) {
        ExamPaper paper = new ExamPaper();
        paper.setId(id);
        paper.setYear(2026);
        paper.setTitle("2026 年真题");
        paper.setTotalScore(new BigDecimal("100.0"));
        paper.setDuration(120);
        paper.setStatus(status);
        paper.setContent("""
                {"sections":[{"part":5,"type":"writing","score":15,
                 "content":{"prompt":"题","scoringDimensions":["内容要点"]}}]}
                """);
        paper.setAnswerKey("""
                {"sections":[{"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}]}
                """);
        return paper;
    }

    private ExamPaperUpsertRequest.ExamPaperUpsertRequestBuilder validRequest() {
        ObjectMapper mapper = new ObjectMapper();
        return ExamPaperUpsertRequest.builder()
                .year(2026)
                .title("2026 年真题")
                .totalScore(new BigDecimal("15"))
                .content(mapper.readTree("""
                        {"sections":[{"part":5,"type":"writing","score":15,
                         "content":{"prompt":"题","scoringDimensions":["内容要点"]}}]}
                        """))
                .answerKey(mapper.readTree("""
                        {"sections":[{"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}]}
                        """));
    }

    @Test
    @DisplayName("新建卷默认上架并生成 ID")
    void createsPaperWithDefaultStatus() {
        when(paperMapper.selectById(any())).thenReturn(null);
        when(paperMapper.insert(any(ExamPaper.class))).thenReturn(1);
        when(submissionMapper.selectCount(any())).thenReturn(0L);

        AdminExamPaperDto saved = newService().save(null, validRequest().build());

        assertThat(saved.getId()).startsWith("2026-");
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getSectionScoreSum()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(saved.getSectionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("JSON 校验不过时完全不写库")
    void rejectsBeforeAnyWrite() {
        ExamPaperUpsertRequest broken = validRequest()
                .answerKey(new ObjectMapper().readTree(
                        "{\"sections\":[{\"part\":5,\"type\":\"writing\",\"score\":15,\"sampleAnswer\":\"范文\"},"
                                + "{\"part\":9,\"type\":\"cloze\",\"score\":0,\"items\":[{\"id\":9,\"answer\":\"A\"}]}]}"))
                .build();

        assertThatThrownBy(() -> newService().save(null, broken))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key 多出第 9 部分");

        verify(paperMapper, never()).insert(any(ExamPaper.class));
        verify(paperMapper, never()).updateById(any(ExamPaper.class));
    }

    @Test
    @DisplayName("更新时保留原状态,除非请求显式给出")
    void keepsStatusWhenUnspecified() {
        ExamPaper existing = paper("exam-2026", 0);
        when(paperMapper.selectById("exam-2026")).thenReturn(existing);
        when(paperMapper.updateById(any(ExamPaper.class))).thenReturn(1);
        when(submissionMapper.selectCount(any())).thenReturn(3L);

        AdminExamPaperDto saved = newService().save("exam-2026", validRequest().build());

        assertThat(saved.getStatus()).isZero();
        assertThat(saved.getSubmissionCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("上下架只接受 0/1")
    void validatesStatusValue() {
        assertThatThrownBy(() -> newService().changeStatus("exam-2026", 5))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("status 仅接受 1(上架)或 0(下架)");

        when(paperMapper.selectById("exam-2026")).thenReturn(paper("exam-2026", 1));
        when(paperMapper.updateById(any(ExamPaper.class))).thenReturn(1);

        newService().changeStatus("exam-2026", 0);

        ArgumentCaptor<ExamPaper> captor = ArgumentCaptor.forClass(ExamPaper.class);
        verify(paperMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isZero();
    }

    @Test
    @DisplayName("列表状态筛选非法值即拒绝")
    void rejectsBadListStatus() {
        assertThatThrownBy(() -> newService().list(null, 2, null, 1, 20))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("status 仅接受 1 或 0");
    }

    @Test
    @DisplayName("已被交卷引用的卷不能删除")
    void protectsPaperWithSubmissions() {
        when(paperMapper.selectById("exam-2026")).thenReturn(paper("exam-2026", 1));
        when(submissionMapper.selectCount(any())).thenReturn(2L);

        assertThatThrownBy(() -> newService().delete("exam-2026"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能删除;如需停止投放请使用下架");

        verify(paperMapper, never()).deleteById(any(String.class));
    }

    @Test
    @DisplayName("无引用的卷可删除")
    void deletesUnreferencedPaper() {
        when(paperMapper.selectById("exam-old")).thenReturn(paper("exam-old", 0));
        when(submissionMapper.selectCount(any())).thenReturn(0L);

        newService().delete("exam-old");

        verify(paperMapper).deleteById("exam-old");
    }

    @Test
    @DisplayName("试卷不存在时 404")
    void throwsNotFoundForMissingPaper() {
        when(paperMapper.selectById("nope")).thenReturn(null);

        assertThatThrownBy(() -> newService().detail("nope"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("试卷不存在");
    }
}
