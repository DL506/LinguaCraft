package com.linguacraft.backend.ai;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.service.QuestionContentValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linguacraft.backend.entity.Question;

/**
 * AI 出题闸门口径测试(阶段 A2 校验复用到 AI 链路)。
 *
 * <p>关注点只有一个:模型返回的内容若未通过五道入库校验,<b>不得落库</b>,并且会重试一次。
 * 用 Mockito 桩替掉模型调用,因此本测试不需要真实 API key。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiGenerateServiceGateTest {

    @Mock
    private AiChatService aiChatService;

    @Mock
    private QuestionMapper questionMapper;

    /** 构建被测服务:校验器与 ObjectMapper 都用真实实例(被测正是这条判定链) */
    private AiGenerateService newService() {
        return new AiGenerateService(aiChatService, questionMapper, new ObjectMapper(),
                new QuestionContentValidator());
    }

    private ObjectNode read(String json) {
        return (ObjectNode) new ObjectMapper().readTree(json);
    }

    /** 通过浅层字段校验、但答案不在候选项内的坏题(校验 4 应拦下) */
    private ObjectNode answerOutsideOptions() {
        return read("""
                {"title":"坏题","difficulty":3,"passage":"It [[1]] a fine day.",
                 "blanks":[{"id":1,"options":[{"key":"A","text":"is"},{"key":"B","text":"are"}],
                            "answer":"D","explanation":"解析"}]}
                """);
    }

    /** 占位符与空位编号不齐的坏题(校验 5 应拦下) */
    private ObjectNode blankMarkMismatch() {
        return read("""
                {"title":"坏题","difficulty":3,"passage":"It [[3]] a fine day.",
                 "blanks":[{"id":1,"options":[{"key":"A","text":"is"},{"key":"B","text":"are"}],
                            "answer":"A","explanation":"解析"}]}
                """);
    }

    private ObjectNode validCloze() {
        return read("""
                {"title":"好题","difficulty":3,"tags":["词义辨析"],
                 "passage":"It [[1]] a fine day.",
                 "blanks":[{"id":1,"options":[{"key":"A","text":"is"},{"key":"B","text":"are"}],
                            "answer":"A","explanation":"主语单数"}]}
                """);
    }

    @Test
    @DisplayName("答案不在候选项内:不落库,重试一次后报可重试错误")
    void rejectsAnswerOutsideOptions() {
        when(aiChatService.chatForJson(anyString())).thenReturn(answerOutsideOptions());

        assertThatThrownBy(() -> newService().generate("cloze"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI 出题结果不合格");

        verify(aiChatService, times(2)).chatForJson(anyString());
        verify(questionMapper, never()).insert(any(Question.class));
    }

    @Test
    @DisplayName("占位符与空位编号不齐:同样拦在库外")
    void rejectsBlankMarkMismatch() {
        when(aiChatService.chatForJson(anyString())).thenReturn(blankMarkMismatch());

        assertThatThrownBy(() -> newService().generate("cloze"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("占位符集合");

        verify(questionMapper, never()).insert(any(Question.class));
    }

    @Test
    @DisplayName("合格结果落库,且答案没有混进下发列 content")
    void persistsValidQuestion() {
        when(aiChatService.chatForJson(anyString())).thenReturn(validCloze());

        newService().generate("cloze");

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionMapper, times(1)).insert(captor.capture());
        Question saved = captor.getValue();
        assertThat(saved.getContent()).contains("\"passage\"").doesNotContain("answer");
        assertThat(saved.getAnswerKey()).contains("\"answer\":\"A\"");
        assertThat(saved.getSourceType()).isEqualTo("ai");
        assertThat(saved.getStatus()).isEqualTo(1);
    }
}
