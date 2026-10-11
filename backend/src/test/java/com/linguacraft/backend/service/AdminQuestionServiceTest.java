package com.linguacraft.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.QuestionUpsertRequest;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.QuestionMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理端题库录入测试(A8 引入)。
 *
 * <p>重点是<b>未知字段 passthrough 的服务端半边</b>:录入表单只建模它认识的键,
 * 前端会把库里原始的 content/answer_key 打底后再覆盖,因此服务端必须
 * <b>原样保留请求里那些它没建模的键</b>(例如将来 A10 写入的校核标记),
 * 同时仍然强制覆写三个权威字段(id/type/source)并沿用原有 source_meta。</p>
 *
 * <p>闸门本身(五道校验)已由 {@code QuestionContentValidatorTest} 覆盖,这里用真实校验器实例,
 * 目的是验证"录入服务没有在校验之后另建一套 content 对象把键丢掉"。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminQuestionServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private PracticeSubmissionMapper submissionMapper;

    @Mock
    private MistakeMapper mistakeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AdminQuestionService service;

    /** 被测服务:真实校验器 + 真 ObjectMapper,只有 mapper 是桩 */
    private AdminQuestionService service() {
        if (service == null) {
            service = new AdminQuestionService(questionMapper, submissionMapper, mistakeMapper,
                    objectMapper, new QuestionContentValidator());
        }
        return service;
    }

    // ==================== 夹具 ====================

    /** 一条合法完形填空的 content(含两个表单没建模的自定义键) */
    private ObjectNode clozeContent() {
        ObjectNode content = objectMapper.createObjectNode();
        content.put("passage", "老师 [[1]] 了她的高声。");
        // 自定义顶层键:录入表单永远不会产生它,保存后必须仍然存在
        ObjectNode note = content.putObject("customNote");
        note.put("checkedBy", "王老师");
        note.put("round", 2);
        ArrayNode blanks = content.putArray("blanks");
        ObjectNode blank = blanks.addObject();
        blank.put("id", 1);
        // 自定义 item 级键:同样要原样穿过
        blank.putObject("verifiedMeta").put("source", "省教研室审核");
        ArrayNode options = blank.putArray("options");
        options.addObject().put("key", "A").put("text", "raised");
        options.addObject().put("key", "B").put("text", "shouted");
        return content;
    }

    /** 与 {@link #clozeContent()} 配套的答案(也带一个自定义键) */
    private ObjectNode clozeAnswerKey() {
        ObjectNode key = objectMapper.createObjectNode();
        ArrayNode blanks = key.putArray("blanks");
        ObjectNode item = blanks.addObject();
        item.put("id", 1);
        item.put("answer", "A");
        item.put("explanation", "raise one's head 为固定搭配。");
        item.putObject("reviewMeta").put("by", "AI 校核");
        return key;
    }

    private QuestionUpsertRequest request(String type, JsonNode content, JsonNode answerKey) {
        QuestionUpsertRequest request = new QuestionUpsertRequest();
        request.setType(type);
        request.setSourceType("teacher");
        request.setTitle("完形 · 自定义键回归");
        request.setDifficulty(3);
        request.setStatus(1);
        request.setContent(content);
        request.setAnswerKey(answerKey);
        return request;
    }

    /** 库里已存在的一道完形题(update 路径的前置) */
    private Question existingCloze() {
        Question entity = new Question();
        entity.setId("cloze-teacher-9");
        entity.setType("cloze");
        entity.setSourceType("teacher");
        entity.setSourceMeta("{\"type\":\"teacher\",\"meta\":{\"teacherName\":\"李老师\",\"school\":\"启航班\"}}");
        entity.setTitle("完形 · 存量");
        entity.setDifficulty(3);
        entity.setTags("[\"固定搭配\"]");
        entity.setContent("{\"passage\":\"旧文章\",\"blanks\":[{\"id\":1,\"options\":[{\"key\":\"A\",\"text\":\"raised\"},{\"key\":\"B\",\"text\":\"shouted\"}]}]}");
        entity.setAnswerKey("{\"blanks\":[{\"id\":1,\"answer\":\"A\",\"explanation\":\"旧解析\"}]}");
        entity.setHasAnswer(true);
        entity.setTotalScore(new java.math.BigDecimal("10.0"));
        entity.setPerQuestionScore(new java.math.BigDecimal("10.0"));
        entity.setStatus(1);
        return entity;
    }

    // ==================== 新建 ====================

    @Test
    @DisplayName("新建:请求里表单没建模的键原样入库,三个权威字段仍由服务端覆写")
    void createKeepsUnknownKeysAndOverwritesAuthoritativeOnes() {
        when(questionMapper.selectById(any())).thenReturn(null);

        QuestionUpsertRequest request = request("cloze", clozeContent(), clozeAnswerKey());
        request.setId("cloze-teacher-new");
        service().create(request);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionMapper).insert(captor.capture());
        Question saved = captor.getValue();
        JsonNode content = parse(saved.getContent());

        // 未知键存活(顶层与 item 级各一个)
        assertThat(content.path("customNote").path("checkedBy").asText()).isEqualTo("王老师");
        assertThat(content.path("customNote").path("round").asInt()).isEqualTo(2);
        assertThat(content.path("blanks").get(0).path("verifiedMeta").path("source").asText()).isEqualTo("省教研室审核");
        // 权威字段以服务端为准:即使请求里没写也必须补齐
        assertThat(content.path("id").asText()).isEqualTo("cloze-teacher-new");
        assertThat(content.path("type").asText()).isEqualTo("cloze");
        assertThat(content.path("source").path("type").asText()).isEqualTo("teacher");
        // 答案列同样不洗键
        assertThat(parse(saved.getAnswerKey()).path("blanks").get(0).path("reviewMeta").path("by").asText())
                .isEqualTo("AI 校核");
    }

    // ==================== 更新 ====================

    @Test
    @DisplayName("更新:未知键存活,id/type 以路径变量与库存题型为准")
    void updateKeepsUnknownKeys() {
        Question existing = existingCloze();
        when(questionMapper.selectById("cloze-teacher-9")).thenReturn(existing);

        // 表单不提交 id/type/source:服务端要自己补;customNote 由前端 passthrough 带回
        QuestionUpsertRequest request = request("cloze", clozeContent(), clozeAnswerKey());
        service().update("cloze-teacher-9", request);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionMapper).updateById(captor.capture());
        JsonNode content = parse(captor.getValue().getContent());

        assertThat(content.path("customNote").path("checkedBy").asText()).isEqualTo("王老师");
        assertThat(content.path("blanks").get(0).path("verifiedMeta").path("source").asText()).isEqualTo("省教研室审核");
        assertThat(content.path("id").asText()).isEqualTo("cloze-teacher-9");
        assertThat(content.path("type").asText()).isEqualTo("cloze");
        assertThat(parse(captor.getValue().getAnswerKey()).path("blanks").get(0).path("reviewMeta").isMissingNode())
                .isFalse();
    }

    @Test
    @DisplayName("更新:录入表单不提交 sourceMeta,必须沿用库里原值而不是清空")
    void updateRetainsSourceMetaWhenRequestOmitsIt() {
        Question existing = existingCloze();
        when(questionMapper.selectById("cloze-teacher-9")).thenReturn(existing);

        service().update("cloze-teacher-9", request("cloze", clozeContent(), clozeAnswerKey()));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionMapper).updateById(captor.capture());
        JsonNode sourceMeta = parse(captor.getValue().getSourceMeta());
        assertThat(sourceMeta.path("meta").path("teacherName").asText()).isEqualTo("李老师");
        // content.source 与 source_meta 必须同源,否则学生端看到的来源与后台记录不一致
        assertThat(parse(captor.getValue().getContent()).path("source").path("meta").path("teacherName").asText())
                .isEqualTo("李老师");
    }

    // ==================== 既有护栏(回归) ====================

    @Test
    @DisplayName("更新:题型不可变更(历史提交与错题按原题型快照)")
    void updateRejectsTypeChange() {
        when(questionMapper.selectById("cloze-teacher-9")).thenReturn(existingCloze());

        QuestionUpsertRequest request = request("reading", clozeContent(), clozeAnswerKey());
        assertThatThrownBy(() -> service().update("cloze-teacher-9", request))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("题型不可变更");
    }

    @Test
    @DisplayName("更新:status 只接受 0 或 1")
    void updateRejectsIllegalStatus() {
        when(questionMapper.selectById("cloze-teacher-9")).thenReturn(existingCloze());

        QuestionUpsertRequest request = request("cloze", clozeContent(), clozeAnswerKey());
        request.setStatus(2);
        assertThatThrownBy(() -> service().update("cloze-teacher-9", request))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("status 仅接受 0 或 1");
    }

    private JsonNode parse(String json) {
        return objectMapper.readTree(json);
    }
}
