package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 题目内容校验器的五道校验单测(阶段 A2 录入与 AI 出题闸门共用)。
 *
 * <p>纯对象构造,不依赖 Spring 上下文与数据库;断言锁定错误文案的关键片段,
 * 这样从 AdminQuestionService 搬迁过来的行为一旦漂移就会失败。</p>
 *
 * <p>注:Jackson 3 的 {@code putArray/withArray/put} 返回泛型 {@code <T extends JsonNode>},
 * 链式调用会推断失败,因此本文件一律先赋值给显式类型的局部变量再操作。</p>
 */
class QuestionContentValidatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final QuestionContentValidator validator = new QuestionContentValidator();

    /** 完形填空合法内容:1 个空,占位符 [[1]] 与 blanks 编号一致,四个选项里含正确项。 */
    private ObjectNode clozeContent() {
        return (ObjectNode) mapper.readTree("""
                {"passage":"It [[1]] a fine day.",
                 "blanks":[{"id":1,"options":[{"key":"A","text":"is"},{"key":"B","text":"are"}]}]}
                """);
    }

    /** 与 {@link #clozeContent()} 对应的答案列。 */
    private ObjectNode clozeAnswerKey() {
        return (ObjectNode) mapper.readTree("""
                {"blanks":[{"id":1,"answer":"A","explanation":"主语单数"}]}
                """);
    }

    @Test
    @DisplayName("合法题目通过全部五道校验")
    void acceptsValidQuestion() {
        assertThatCode(() -> validator.validate("cloze", clozeContent(), clozeAnswerKey()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("校验 1:结构缺关键字段即拒绝")
    void rejectsMissingPassage() {
        ObjectNode content = clozeContent();
        content.remove("passage");
        assertThatThrownBy(() -> validator.validate("cloze", content, clozeAnswerKey()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("content.passage 不能为空");
    }

    @Test
    @DisplayName("校验 1:选项不足两个即拒绝,并给出定位路径")
    void rejectsTooFewOptions() {
        ObjectNode content = clozeContent();
        ArrayNode blanks = content.withArray("blanks");
        ObjectNode first = (ObjectNode) blanks.get(0);
        ArrayNode options = first.withArray("options");
        options.remove(1);
        assertThatThrownBy(() -> validator.validate("cloze", content, clozeAnswerKey()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("content.blanks[0].options 至少需要 2 个选项");
    }

    @Test
    @DisplayName("校验 2:答案混进 content 即拒绝(递归进入数组元素)")
    void rejectsAnswerInsideContent() {
        ObjectNode content = clozeContent();
        ArrayNode blanks = content.withArray("blanks");
        ObjectNode first = (ObjectNode) blanks.get(0);
        first.put("answer", "A");
        assertThatThrownBy(() -> validator.validate("cloze", content, clozeAnswerKey()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("content 禁止包含答案类字段");
    }

    @Test
    @DisplayName("校验 3:answer_key 编号集合与 content 不一致即拒绝")
    void rejectsIdSetMismatch() {
        ObjectNode content = clozeContent();
        content.put("passage", "It [[1]] a fine day and [[2]] too.");
        ArrayNode blanks = content.withArray("blanks");
        ObjectNode second = blanks.addObject();
        second.put("id", 2);
        ArrayNode secondOptions = second.putArray("options");
        ObjectNode optionA = secondOptions.addObject();
        optionA.put("key", "A").put("text", "is");
        ObjectNode optionB = secondOptions.addObject();
        optionB.put("key", "B").put("text", "are");
        assertThatThrownBy(() -> validator.validate("cloze", content, clozeAnswerKey()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key 的编号集合必须与 content 完全一致");
    }

    @Test
    @DisplayName("校验 4:答案不在该空候选 key 内即拒绝")
    void rejectsAnswerOutsideOptions() {
        ObjectNode answerKey = clozeAnswerKey();
        ArrayNode keyItems = answerKey.withArray("blanks");
        ObjectNode first = (ObjectNode) keyItems.get(0);
        first.put("answer", "D");
        assertThatThrownBy(() -> validator.validate("cloze", clozeContent(), answerKey))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不在该空选项");
    }

    @Test
    @DisplayName("校验 4:五选五答案重复即拒绝")
    void rejectsDuplicatedMatchingAnswers() {
        ObjectNode content = (ObjectNode) mapper.readTree("""
                {"passage":"One. [[1]] Two. [[2]]",
                 "blanks":[{"id":1},{"id":2}],
                 "options":[{"key":"A","text":"句一"},{"key":"B","text":"句二"}]}
                """);
        ObjectNode answerKey = (ObjectNode) mapper.readTree("""
                {"blanks":[{"id":1,"answer":"A"},{"id":2,"answer":"A"}]}
                """);
        assertThatThrownBy(() -> validator.validate("matching", content, answerKey))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("五选五答案重复");
    }

    @Test
    @DisplayName("校验 5:passage 占位符与空位编号不齐即拒绝")
    void rejectsBlankMarkMismatch() {
        ObjectNode content = clozeContent();
        content.put("passage", "It [[3]] a fine day.");
        assertThatThrownBy(() -> validator.validate("cloze", content, clozeAnswerKey()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("占位符集合");
    }

    @Test
    @DisplayName("作文允许无范文,但 sampleAnswer 一旦给出必须是字符串")
    void validatesWritingSampleAnswerType() {
        ObjectNode content = (ObjectNode) mapper.readTree("""
                {"prompt":"写一封申请邮件","requirements":["要点一"],
                 "scoringDimensions":["内容要点"],"wordLimit":120}
                """);
        assertThatCode(() -> validator.validate("writing", content, null)).doesNotThrowAnyException();

        ObjectNode badKey = mapper.createObjectNode();
        badKey.put("sampleAnswer", 123);
        assertThatThrownBy(() -> validator.validate("writing", content, badKey))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key.sampleAnswer 必须是字符串");
    }

    @Test
    @DisplayName("语法填空答案按自由文本处理,只要求非空")
    void validatesGrammarFillAnswer() {
        ObjectNode content = (ObjectNode) mapper.readTree("""
                {"passage":"He [[1]] (work) here.","blanks":[{"id":1,"hint":"(work)"}]}
                """);
        ObjectNode answerKey = (ObjectNode) mapper.readTree("""
                {"blanks":[{"id":1,"answer":"working","acceptAlternatives":["works"]}]}
                """);
        assertThatCode(() -> validator.validate("grammar-fill", content, answerKey)).doesNotThrowAnyException();

        ArrayNode keyItems = answerKey.withArray("blanks");
        ObjectNode first = (ObjectNode) keyItems.get(0);
        first.put("answer", "   ");
        assertThatThrownBy(() -> validator.validate("grammar-fill", content, answerKey))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key.blanks[0].answer 不能为空");
    }

    @Test
    @DisplayName("客观题不给 answer_key 即拒绝")
    void requiresAnswerKeyForObjective() {
        assertThatThrownBy(() -> validator.validate("cloze", clozeContent(), null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("客观题必须提供 answer_key");
    }
}
