package com.linguacraft.backend.ai;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.AiDraftDto;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.service.QuestionContentValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linguacraft.backend.entity.Question;

/**
 * AI 草稿出口测试(阶段 A9)。
 *
 * <p>只盯一件事:<b>生成草稿这条路径一次都不碰题库</b>,且草稿与入库题目过同一个闸门。
 * 落库分支的行为已由 {@code AiGenerateServiceGateTest} 覆盖,这里不重复。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiGenerateServiceDraftTest {

    @Mock
    private AiChatService aiChatService;

    @Mock
    private QuestionMapper questionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 被测服务:真实校验器 + 真 ObjectMapper;模型名用测试桩值(字段注入在纯单测里不会发生) */
    private AiGenerateService newService() {
        AiGenerateService service = new AiGenerateService(aiChatService, questionMapper, objectMapper,
                new QuestionContentValidator());
        ReflectionTestUtils.setField(service, "modelName", "deepseek-flash");
        return service;
    }

    private ObjectNode read(String json) {
        return (ObjectNode) objectMapper.readTree(json);
    }

    /** 一道能通过五道校验的完形草稿 */
    private ObjectNode validCloze() {
        return read("""
                {"title":"AI 完形草稿","difficulty":4,"tags":["动词时态"],
                 "passage":"She [[1]] to the market yesterday.",
                 "blanks":[{"id":1,"options":[{"key":"A","text":"went"},{"key":"B","text":"goes"}],
                            "answer":"A","explanation":"yesterday 提示一般过去时。"}]}
                """);
    }

    @Test
    @DisplayName("生成草稿:不写题库,不编 id,内容与答案分离返回")
    void generateDraftNeverPersists() {
        when(aiChatService.chatForJson(anyString())).thenReturn(validCloze());

        AiDraftDto draft = newService().generateDraft("cloze");

        verify(questionMapper, never()).insert(any(Question.class));
        assertThat(draft.getType()).isEqualTo("cloze");
        assertThat(draft.getTitle()).isEqualTo("AI 完形草稿");
        assertThat(draft.getDifficulty()).isEqualTo(4);
        assertThat(draft.getTags()).containsExactly("动词时态");

        JsonNode content = draft.getContent();
        // 权威字段留给保存时的服务端覆写:草稿阶段有 id 只会误导管理员去改一个马上被覆盖的值
        assertThat(content.has("id")).isFalse();
        assertThat(content.has("type")).isFalse();
        assertThat(content.has("source")).isFalse();
        // content 里绝不带答案类字段:候选选项文本是要下发给学生的,答案与解析只在 answerKey
        assertThat(content.toString()).doesNotContain("\"answer\"").doesNotContain("\"explanation\"");
        assertThat(content.path("blanks").get(0).has("answer")).isFalse();
        assertThat(content.path("blanks").get(0).path("options").get(0).path("text").asText()).isEqualTo("went");
        assertThat(draft.getAnswerKey().path("blanks").get(0).path("answer").asText()).isEqualTo("A");
        assertThat(draft.getAnswerKey().path("blanks").get(0).path("explanation").asText()).isNotBlank();
    }

    @Test
    @DisplayName("生成草稿:sourceMeta 记录模型与生成时刻(形状同题目详情,可直接回填)")
    void generateDraftCarriesSourceMeta() {
        when(aiChatService.chatForJson(anyString())).thenReturn(validCloze());

        AiDraftDto draft = newService().generateDraft("cloze");

        // 是 meta 子对象本身,不带外层 type —— 与 AdminQuestionDetailDto.sourceMeta / 录入请求同形
        assertThat(draft.getSourceMeta().has("type")).isFalse();
        assertThat(draft.getSourceMeta().path("model").asText()).isEqualTo("deepseek-flash");
        assertThat(draft.getSourceMeta().path("generatedAt").asText()).isNotBlank();
    }

    @Test
    @DisplayName("草稿与入库同闸门:答案不在候选项内时 500 拒绝,且不写库")
    void draftGoesThroughSameValidator() {
        ObjectNode bad = read("""
                {"title":"坏草稿","difficulty":3,"passage":"It [[1]] a fine day.",
                 "blanks":[{"id":1,"options":[{"key":"A","text":"is"},{"key":"B","text":"are"}],
                            "answer":"D","explanation":"解析"}]}
                """);
        when(aiChatService.chatForJson(anyString())).thenReturn(bad);

        assertThatThrownBy(() -> newService().generateDraft("cloze"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI 出题结果不合格:");
        verify(questionMapper, never()).insert(any(Question.class));
    }

    @Test
    @DisplayName("非法题型:1004 拒绝,且完全不调用模型")
    void rejectsUnknownTypeBeforeCallingModel() {
        assertThatThrownBy(() -> newService().generateDraft("speaking"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("题型");
        verify(aiChatService, never()).chatForJson(anyString());
        verify(questionMapper, never()).insert(any(Question.class));
    }
}
