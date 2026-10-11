package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 真题卷 JSON 校验器测试(阶段 A6-③)。
 *
 * <p>这里守的是<b>静默错判</b>:卷面题号与答案题号一旦错位,交卷时不报错,只是分数错。
 * 因此每条拒绝路径都断言 message 指明是哪一部分。</p>
 */
class ExamPaperValidatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final ExamPaperValidator validator = new ExamPaperValidator();

    /** 一份最小合法卷:part1 阅读 2 分 + part5 写作 15 分,总分 17 */
    private JsonNode validContent() {
        return mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,
                   "content":[{"id":"p1","passage":"text","questions":[
                     {"id":1,"stem":"q1","options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]},
                     {"id":2,"stem":"q2","options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}]},
                  {"part":5,"type":"writing","score":15,
                   "content":{"id":"w1","prompt":"写一封申请信","scoringDimensions":["内容要点","语言表达"]}}
                ]}
                """);
    }

    /** 与 {@link #validContent()} 匹配的答案(写作只给范文) */
    private JsonNode validAnswerKey() {
        return mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"items":[
                    {"id":1,"answer":"A","explanation":"解析"},
                    {"id":2,"answer":"B","explanation":"解析"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);
    }

    @Test
    @DisplayName("卷面与答案一致的整卷通过全部校验")
    void acceptsConsistentPaper() {
        assertThatCode(() -> validator.validate(validContent(), validAnswerKey(), new BigDecimal("17")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("答案题号与卷面不一致即拒绝(否则会静默判错)")
    void rejectsIdMismatch() {
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"items":[
                    {"id":1,"answer":"A"},{"id":7,"answer":"B"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(validContent(), answerKey, new BigDecimal("17")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("第 1 部分的答案编号与题目编号不一致");
    }

    @Test
    @DisplayName("同一卡号跨部分重复即拒绝")
    void rejectsDuplicatedCardId() {
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"items":[
                    {"id":1,"answer":"A"},{"id":2,"answer":"B"}]},
                  {"part":2,"type":"matching","score":15,"items":[
                    {"id":1,"answer":"C"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);
        JsonNode content = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"content":[{"id":"p1","questions":[
                    {"id":1,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]},
                    {"id":2,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}]},
                  {"part":2,"type":"matching","score":15,"content":{"passage":"[[1]]","blanks":[{"id":1}]}},
                  {"part":5,"type":"writing","score":15,"content":{"prompt":"题","scoringDimensions":["内容要点"]}}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(content, answerKey, new BigDecimal("32")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("答题卡编号 1 在全卷重复出现");
    }

    @Test
    @DisplayName("各部分分值之和与卷面总分不符即拒绝")
    void rejectsScoreSumMismatch() {
        assertThatThrownBy(() -> validator.validate(validContent(), validAnswerKey(), new BigDecimal("100")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("各部分分值之和 17 与卷面总分 100 不一致");
    }

    @Test
    @DisplayName("缺少写作部分即拒绝(异步批改与人工判分都依赖它)")
    void requiresWritingSection() {
        JsonNode content = mapper.readTree("""
                {"sections":[{"part":1,"type":"reading","score":2,"content":[{"id":"p1","questions":[
                  {"id":1,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}]}]}
                """);
        JsonNode answerKey = mapper.readTree("""
                {"sections":[{"part":1,"type":"reading","score":2,"items":[{"id":1,"answer":"A"}]}]}
                """);

        assertThatThrownBy(() -> validator.validate(content, answerKey, new BigDecimal("2")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("必须恰好包含一个写作部分");
    }

    @Test
    @DisplayName("写作部分分值不是 15 即拒绝(与异步批改器的封顶口径冲突)")
    void rejectsNonFifteenWriting() {
        JsonNode content = mapper.readTree("""
                {"sections":[
                  {"part":5,"type":"writing","score":20,"content":{"prompt":"题","scoringDimensions":["内容要点"]}}
                ]}
                """);
        JsonNode answerKey = mapper.readTree("""
                {"sections":[{"part":5,"type":"writing","score":20,"sampleAnswer":"范文"}]}
                """);

        assertThatThrownBy(() -> validator.validate(content, answerKey, new BigDecimal("20")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("写作部分分值必须为 15");
    }

    @Test
    @DisplayName("answer_key 多出卷面没有的部分即拒绝")
    void rejectsExtraAnswerSection() {
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"items":[{"id":1,"answer":"A"},{"id":2,"answer":"B"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"},
                  {"part":9,"type":"cloze","score":0,"items":[{"id":99,"answer":"A"}]}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(validContent(), answerKey, new BigDecimal("17")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key 多出第 9 部分");
    }

    @Test
    @DisplayName("部分号重复即拒绝")
    void rejectsDuplicatedPart() {
        // 答案只给 part1 的题号 1:第一个 section 能过,循环到第二个 part=1 时才暴露重复
        JsonNode content = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"content":[{"id":"a","questions":[{"id":1,"options":[{"key":"A","text":"x"},{"key":"B","text":"y"}]}]}]},
                  {"part":1,"type":"reading","score":0,"content":[{"id":"b","questions":[]}]},
                  {"part":5,"type":"writing","score":15,"content":{"prompt":"题","scoringDimensions":["内容要点"]}}
                ]}
                """);
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"items":[{"id":1,"answer":"A"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(content, answerKey, new BigDecimal("17")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("content 存在重复的 part:1");
    }

    @Test
    @DisplayName("答案项缺少答案文本即拒绝")
    void rejectsBlankAnswer() {
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":2,"items":[
                    {"id":1,"answer":""},{"id":2,"answer":"B"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(validContent(), answerKey, new BigDecimal("17")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("题号 1 的答案为空");
    }

    @Test
    @DisplayName("sections 缺失或非数组即拒绝")
    void rejectsMissingSections() {
        assertThatThrownBy(() -> validator.validate(mapper.readTree("{}"), validAnswerKey(), new BigDecimal("17")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("content.sections 必须是非空数组");

        assertThatThrownBy(() -> validator.validate(validContent(), mapper.readTree("{}"), new BigDecimal("17")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("answer_key.sections 必须是非空数组");
    }
}
