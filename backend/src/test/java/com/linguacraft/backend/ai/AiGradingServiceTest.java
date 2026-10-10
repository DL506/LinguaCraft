package com.linguacraft.backend.ai;

import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.service.GradedObjective;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.linguacraft.backend.dto.practice.SubmitDetailDto;

/**
 * AI 判分结果落地口径测试。
 *
 * <p>核心断言:明细行数与"学生作答的题号集合"一致(模型漏答补行、多答忽略),
 * 得分封顶卷面总分。桩替模型调用,不需要真实 API key。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiGradingServiceTest {

    @Mock
    private AiChatService aiChatService;

    private ObjectNode read(String json) {
        return (ObjectNode) new ObjectMapper().readTree(json);
    }

    /** 构造一道客观题:单题 2 分、卷面总分 5 分(用来验证封顶) */
    private Question objectiveQuestion() {
        Question question = new Question();
        question.setId("cloze-ai-test");
        question.setType("cloze");
        question.setContent("{\"passage\":\"It [[1]] a fine day.\",\"blanks\":[{\"id\":1}]}");
        question.setPerQuestionScore(new BigDecimal("2.0"));
        question.setTotalScore(new BigDecimal("5.0"));
        return question;
    }

    private Map<Integer, String> threeAnswers() {
        Map<Integer, String> answers = new LinkedHashMap<>();
        answers.put(1, "A");
        answers.put(2, "B");
        answers.put(3, "C");
        return answers;
    }

    @Test
    @DisplayName("模型漏答某个题号时补成一行未正确,行数与总题数保持对齐")
    void fillsMissingDetailRows() {
        when(aiChatService.chatForJson(anyString())).thenReturn(read("""
                {"details":[{"id":1,"correctAnswer":"A","isCorrect":true,"explanation":"正确"},
                             {"id":3,"correctAnswer":"C","isCorrect":true,"explanation":"正确"}]}
                """));

        GradedObjective graded = new AiGradingService(aiChatService)
                .gradeObjective(objectiveQuestion(), threeAnswers());

        assertThat(graded.details()).hasSize(3);
        assertThat(graded.totalCount()).isEqualTo(3);
        assertThat(graded.correctCount()).isEqualTo(2);
        SubmitDetailDto second = graded.details().get(1);
        assertThat(second.getId()).isEqualTo(2);
        assertThat(second.getUserAnswer()).isEqualTo("B");
        assertThat(second.getIsCorrect()).isFalse();
        assertThat(second.getExplanation()).contains("AI 未返回该题判定");
    }

    @Test
    @DisplayName("模型多给的未知题号被忽略,不抬高总题数")
    void ignoresExtraDetailRows() {
        when(aiChatService.chatForJson(anyString())).thenReturn(read("""
                {"details":[{"id":1,"correctAnswer":"A","isCorrect":true},
                             {"id":2,"correctAnswer":"B","isCorrect":true},
                             {"id":3,"correctAnswer":"C","isCorrect":true},
                             {"id":9,"correctAnswer":"Z","isCorrect":true}]}
                """));

        GradedObjective graded = new AiGradingService(aiChatService)
                .gradeObjective(objectiveQuestion(), threeAnswers());

        assertThat(graded.details()).hasSize(3);
        assertThat(graded.totalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("得分按单题分值累计但不超过卷面总分")
    void clampsScoreToTotalScore() {
        when(aiChatService.chatForJson(anyString())).thenReturn(read("""
                {"details":[{"id":1,"isCorrect":true},{"id":2,"isCorrect":true},{"id":3,"isCorrect":true}]}
                """));

        GradedObjective graded = new AiGradingService(aiChatService)
                .gradeObjective(objectiveQuestion(), threeAnswers());

        // 2.0 x 3 = 6.0 > 总分 5.0,应封顶
        assertThat(graded.score()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("作文维度分之和超过满分时封顶")
    void clampsWritingScore() {
        when(aiChatService.chatForJson(anyString())).thenReturn(read("""
                {"dimensions":[{"name":"内容要点","score":8,"comment":"要点齐全"},
                               {"name":"语言表达","score":8,"comment":"表达清楚"}],
                 "totalScore":16,"sampleAnswer":"范文","overallComment":"总评"}
                """));

        Question writing = new Question();
        writing.setId("writing-ai-test");
        writing.setType("writing");
        writing.setContent("{\"prompt\":\"写一封申请邮件\",\"scoringDimensions\":[\"内容要点\",\"语言表达\"]}");
        writing.setTotalScore(new BigDecimal("15.0"));

        GradedWriting graded = new AiGradingService(aiChatService).gradeWriting(writing, "My email text.");

        // 8 + 8 = 16 > 满分 15,应封顶到 15.0(而不是模型自报的 totalScore)
        assertThat(graded.score()).isEqualByComparingTo(new BigDecimal("15.0"));
        assertThat(graded.dimensions()).hasSize(2);
        assertThat(graded.overallComment()).isEqualTo("总评");
    }
}
