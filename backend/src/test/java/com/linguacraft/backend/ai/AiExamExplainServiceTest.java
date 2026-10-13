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
import com.linguacraft.backend.dto.admin.ExamExplainDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 真题卷「AI 补解析」的清洗与分批测试(阶段 A11)。
 *
 * <p>提示词写了"只处理本批编号""不要改答案",但<b>服务端不指望模型兑现</b>:
 * 真题卷的编号是全卷累加的卡号,模型一旦自己编号,采纳后解析就会写到<b>别的部分的题</b>上,
 * 而学生看到的解析与正确答案对不上 —— 这类污染比没解析更糟,所以清洗比提示词重要。</p>
 *
 * <p>分批沿用 A10 的实测结论(5 项一批能活着回来,10 项一次必定卡在 60 秒)。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiExamExplainServiceTest {

    @Mock
    private AiChatService aiChatService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiExamExplainService service() {
        return new AiExamExplainService(aiChatService, objectMapper);
    }

    private JsonNode read(String json) {
        return objectMapper.readTree(json);
    }

    /** 完形部分的卷面(局部题序 1~3;卡号由答案侧决定) */
    private JsonNode clozeContent() {
        return read("""
                {"passage":"He [[1]] home, then [[2]] TV and [[3]] out.","blanks":[
                  {"id":1,"options":[{"key":"A","text":"went"},{"key":"B","text":"go"}]},
                  {"id":2,"options":[{"key":"A","text":"watched"},{"key":"B","text":"watch"}]},
                  {"id":3,"options":[{"key":"A","text":"went"},{"key":"B","text":"goes"}]}]}
                """);
    }

    /** n 个答案项,卡号从 firstCard 连续排(真题卷形状:五选五之后是 21 起) */
    private JsonNode itemsOf(int count, int firstCard) {
        ArrayNode items = objectMapper.createArrayNode();
        for (int i = 0; i < count; i++) {
            ObjectNode item = items.addObject();
            item.put("id", firstCard + i);
            item.put("answer", "A");
            item.put("explanation", "已有解析" + (firstCard + i));
        }
        return items;
    }

    /** 单个答案项(可指定要不要标准答案) */
    private ObjectNode itemOf(int card, String answer) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("id", card);
        if (answer != null) {
            item.put("answer", answer);
        }
        item.put("explanation", "");
        return item;
    }

    @Test
    @DisplayName("超过 5 题分批请求,跨批同卡号只保留先到的一条")
    void splitsIntoBatchesOfFiveAndDedupesByCard() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":21,"explanation":"第一题依据原文 went。"}]}
                """));

        ExamExplainDto result = service().explain("cloze", clozeContent(), itemsOf(7, 21));

        // 7 题 → 2 批;两批都报卡号 21 → 第二条计入丢弃,前端不会出现两行
        verify(aiChatService, times(2)).chatForJson(anyString(), anyLong());
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(21);
        assertThat(result.getDroppedCount()).isEqualTo(1);
        assertThat(result.getBatchCount()).isEqualTo(2);
        assertThat(result.getRequestedCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("每批只声明本批卡号(整卷卷面都发,靠这句防串题)")
    void eachBatchDeclaresOnlyItsOwnCardNumbers() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("{\"items\":[]}"));

        service().explain("cloze", clozeContent(), itemsOf(6, 16));

        ArgumentCaptor<String> prompts = ArgumentCaptor.forClass(String.class);
        verify(aiChatService, times(2)).chatForJson(prompts.capture(), anyLong());
        assertThat(prompts.getAllValues().get(0)).contains("本批只处理这些答题卡编号:[16, 17, 18, 19, 20]");
        assertThat(prompts.getAllValues().get(1)).contains("本批只处理这些答题卡编号:[21]");
        // 第二批的答案项里不该出现第一批的卡号
        assertThat(prompts.getAllValues().get(1)).doesNotContain("\"id\":16");
    }

    @Test
    @DisplayName("卡号不属于本部分即丢弃并计数(模型自己编号时不能污染别的部分)")
    void dropsUnknownCardNumbers() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":21,"explanation":"正经解析。"},{"id":3,"explanation":"按本部分局部题序编号,越界。"},
                          {"id":999,"explanation":"整卷没有这个卡号。"}]}
                """));

        ExamExplainDto result = service().explain("cloze", clozeContent(), itemsOf(3, 21));

        assertThat(result.getItems()).extracting(ExamExplainDto.Item::getId).containsExactly(21);
        assertThat(result.getDroppedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("空解析丢弃:否则采纳等于把已有解析抹掉")
    void dropsBlankExplanation() {
        when(aiChatService.chatForJson(anyString(), anyLong())).thenReturn(read("""
                {"items":[{"id":21,"explanation":"   "},{"id":22,"explanation":"有内容。"}]}
                """));

        ExamExplainDto result = service().explain("cloze", clozeContent(), itemsOf(2, 21));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getExplanation()).isEqualTo("有内容。");
        assertThat(result.getDroppedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("超长解析截断:模型跑飞时不能把上千字塞进 answer_key")
    void truncatesOverlongExplanation() {
        String longText = "解".repeat(700);
        when(aiChatService.chatForJson(anyString(), anyLong()))
                .thenReturn(read("{\"items\":[{\"id\":21,\"explanation\":\"" + longText + "\"}]}"));

        ExamExplainDto result = service().explain("cloze", clozeContent(), itemsOf(1, 21));

        assertThat(result.getItems().get(0).getExplanation()).hasSize(600);
    }

    @Test
    @DisplayName("没有标准答案的题不发往模型:让 AI 解释它等于让它自己定答案")
    void skipsItemsWithoutStandardAnswer() {
        ArrayNode items = objectMapper.createArrayNode();
        items.add(itemOf(16, "A"));
        items.add(itemOf(17, null));
        when(aiChatService.chatForJson(anyString(), anyLong()))
                .thenReturn(read("{\"items\":[{\"id\":16,\"explanation\":\"依据原文。\"}]}"));

        ExamExplainDto result = service().explain("cloze", clozeContent(), items);

        verify(aiChatService, times(1)).chatForJson(anyString(), anyLong());
        assertThat(result.getRequestedCount()).isEqualTo(1);
        assertThat(result.getSkippedNoAnswer()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("本部分全是没答案的题即拒绝(不花额度)")
    void rejectsSectionWithoutAnyAnswer() {
        ArrayNode items = objectMapper.createArrayNode();
        items.add(itemOf(16, null));

        assertThatThrownBy(() -> service().explain("cloze", clozeContent(), items))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("先补齐答案再让 AI 写解析");
        verifyNoInteractions(aiChatService);
    }

    @Test
    @DisplayName("写作部分拒绝:客观题解析与范文是两回事")
    void declinesWritingSection() {
        assertThatThrownBy(() -> service().explain("writing", clozeContent(), itemsOf(1, 45)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("写作部分没有客观题解析可生成");
        verify(aiChatService, never()).chatForJson(anyString(), anyLong());
    }

    @Test
    @DisplayName("非法题型直接拒绝(1004),不触模型")
    void rejectsUnknownType() {
        assertThatThrownBy(() -> service().explain("listening", clozeContent(), itemsOf(1, 1)))
                .isInstanceOf(BizException.class);
        verifyNoInteractions(aiChatService);
    }

    @Test
    @DisplayName("items 为空或 id 不是数字即拒绝")
    void rejectsMalformedItems() {
        assertThatThrownBy(() -> service().explain("cloze", clozeContent(), objectMapper.createArrayNode()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("本部分没有答案项");

        assertThatThrownBy(() -> service().explain("cloze", clozeContent(),
                read("[{\"id\":\"21\",\"answer\":\"A\"}]")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("items[].id 必须是答题卡编号");
        verifyNoInteractions(aiChatService);
    }
}
