package com.linguacraft.backend.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.AiReviewDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 校核的清洗与分批测试(阶段 A10)。
 *
 * <p>提示词写了"不得新增编号""非语法填空不要给变体",但<b>服务端不指望模型兑现</b>:
 * 这些清洗规则都在 {@link AiReviewService} 里做,本测试逐条验证。
 * 判分是纯字符串比对,一条被误采纳的变体会直接放过学生的错答,所以清洗比提示词重要。</p>
 *
 * <p>分批也是行为契约:实测 10 空一次问完会在 60 秒被对端切断,所以按 5 项分片并跨批按编号去重。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiReviewServiceTest {

    @Mock
    private AiChatService aiChatService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiReviewService service() {
        return new AiReviewService(aiChatService, objectMapper);
    }

    private JsonNode read(String json) {
        return objectMapper.readTree(json);
    }

    /** 一道两个空的语法填空题面与答案 */
    private JsonNode grammarContent() {
        return read("""
                {"passage":"He [[1]] home and [[2]] TV.","blanks":[{"id":1,"hint":"go"},{"id":2}]}
                """);
    }

    private JsonNode grammarKey() {
        return read("""
                {"blanks":[{"id":1,"answer":"went","explanation":"过去时。"},
                           {"id":2,"answer":"watched","explanation":"解析"}]}
                """);
    }

    /** n 个空的语法填空答案(测分片用) */
    private JsonNode grammarKeyOf(int count) {
        ObjectNode key = objectMapper.createObjectNode();
        ArrayNode blanks = key.putArray("blanks");
        for (int i = 1; i <= count; i++) {
            ObjectNode item = blanks.addObject();
            item.put("id", i);
            item.put("answer", "ans" + i);
            item.put("explanation", "解析" + i);
        }
        return key;
    }

    @Test
    @DisplayName("超过 5 个答案项时分批请求(实测 10 空一次问不完),跨批按编号去重")
    void splitsReviewIntoBatchesOfFiveAndDedupesById() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"summary":"本批无问题","items":[{"id":1,"alternatives":[],"answerRisk":"ok","explanationRisk":"ok"}]}
                """));

        AiReviewDto result = service().review("grammar-fill", grammarContent(), grammarKeyOf(7));

        // 7 项 → 2 批;两批都报 id=1 → 只留先到的一条,重复那条计入丢弃
        verify(aiChatService, times(2)).chatForJson(anyString(), anyLong());
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(1);
        assertThat(result.getDroppedCount()).isEqualTo(1);
        assertThat(result.getSummary()).isEqualTo("本批无问题；本批无问题");
    }

    @Test
    @DisplayName("编号对得上才保留:模型新增的编号被丢弃并计数,已知编号全部可用")
    void dropsItemsWithUnknownIds() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"summary":"两处建议",
                 "items":[{"id":1,"alternatives":["had gone"],"answerRisk":"notUnique","explanationRisk":"ok"},
                          {"id":99,"alternatives":["whatever"],"answerRisk":"mayBeWrong","explanationRisk":"mismatch"},
                          {"id":2,"alternatives":[],"answerRisk":"ok","explanationRisk":"tooThin","explanation":"补一句为什么"}]}
                """));

        AiReviewDto result = service().review("grammar-fill", grammarContent(), grammarKey());

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).extracting(AiReviewDto.Item::getId).containsExactly(1, 2);
        assertThat(result.getDroppedCount()).isEqualTo(1);
        assertThat(result.getSummary()).isEqualTo("两处建议");
    }

    @Test
    @DisplayName("非语法填空:模型给来的变体一律抹掉(判分不读它,给采纳按钮就是假承诺)")
    void stripsAlternativesForNonGrammarTypes() {
        JsonNode readingContent = read("""
                {"passage":"A text.","questions":[{"id":1,"stem":"Q?","options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}
                """);
        JsonNode readingKey = read("""
                {"questions":[{"id":1,"answer":"A","explanation":"因为 A。"}]}
                """);
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":1,"alternatives":["B","b"],"answerRisk":"notUnique","explanationRisk":"ok"}]}
                """));

        AiReviewDto result = service().review("reading", readingContent, readingKey);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getAlternatives()).isEmpty();
        // 风险提醒仍然保留(它不改数据)
        assertThat(result.getItems().get(0).getAnswerRisk()).isEqualTo("notUnique");
    }

    @Test
    @DisplayName("风险值归一:模型造出的未知状态按 ok 处理,前端不会渲染出未定义状态")
    void normalizesUnknownRiskValues() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":1,"alternatives":[],"answerRisk":"totally-broken","explanationRisk":null},
                          {"id":2,"alternatives":[],"answerRisk":"mayBeWrong","explanationRisk":"mismatch"}]}
                """));

        AiReviewDto result = service().review("grammar-fill", grammarContent(), grammarKey());

        assertThat(result.getItems().get(0).getAnswerRisk()).isEqualTo("ok");
        assertThat(result.getItems().get(0).getExplanationRisk()).isEqualTo("ok");
        assertThat(result.getItems().get(1).getAnswerRisk()).isEqualTo("mayBeWrong");
        assertThat(result.getItems().get(1).getExplanationRisk()).isEqualTo("mismatch");
    }

    @Test
    @DisplayName("语法填空变体清洗:去空白、去重,并剔除与标准答案相同的那一项")
    void cleansAlternativeValues() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":1,"alternatives":["went","  had gone ","had gone",""," "],"answerRisk":"ok","explanationRisk":"ok"}]}
                """));

        AiReviewDto result = service().review("grammar-fill", grammarContent(), grammarKey());

        // "went" 就是标准答案,不该出现在可采纳的变体里;空串与重复项也不该留
        assertThat(result.getItems().get(0).getAlternatives()).containsExactly("had gone");
    }

    @Test
    @DisplayName("写作题(无答案项)拒绝校核并给出可懂文案,而不是返回空建议")
    void rejectsQuestionWithoutAnswerItems() {
        JsonNode writingKey = read("{\"sampleAnswer\":\"Dear Peter, ...\"}");

        assertThatThrownBy(() -> service().review("writing", read("{\"prompt\":\"写信\"}"), writingKey))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有可校核的答案项");
    }

    @Test
    @DisplayName("非法题型与缺答案对象:在进入模型之前就被拦下")
    void rejectsBadInputBeforeCallingModel() {
        assertThatThrownBy(() -> service().review("speaking", grammarContent(), grammarKey()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("题型");

        assertThatThrownBy(() -> service().review("cloze", grammarContent(), null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key");
    }

    @Test
    @DisplayName("提示词必须写明本批编号:否则模型会拿完整题面对比半批答案,回报\"答案键缺 1-5\"这种假问题")
    void promptDeclaresBatchScopeToStopFalseMissingAnswerReports() {
        // 实测踩过:第一批(1-5)发完整题面 + 只含 1-5 的答案,模型回"答案未覆盖题面 6-10 空"
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":1,"alternatives":[],"answerRisk":"ok","explanationRisk":"ok"}]}
                """));

        service().review("grammar-fill", grammarContent(), grammarKeyOf(7));

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(aiChatService, times(2)).chatForJson(prompt.capture(), anyLong());
        // 第一批只报 1~5,第二批只报 6~7 —— 各批都不该以为自己缺了别人的编号
        assertThat(prompt.getAllValues().get(0)).contains("本批只校核这些编号").contains("1, 2, 3, 4, 5");
        assertThat(prompt.getAllValues().get(1)).contains("6, 7");
    }

    @Test
    @DisplayName("answer_key 用 items 形状(真题卷那套)也能定位编号")
    void supportsItemsShape() {
        JsonNode paperKey = read("{\"items\":[{\"id\":36,\"answer\":\"have become\",\"explanation\":\"时态\"}]}");
        JsonNode paperContent = read("{\"passage\":\"...[[36]]...\",\"blanks\":[{\"id\":36}]}");
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":36,"alternatives":["has become"],"answerRisk":"ok","explanationRisk":"ok"}]}
                """));

        AiReviewDto result = service().review("grammar-fill", paperContent, paperKey);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(36);
        assertThat(result.getItems().get(0).getAlternatives()).containsExactly("has become");
    }
}
