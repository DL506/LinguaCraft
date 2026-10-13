package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 真题卷 JSON 校验器测试(阶段 A6-③,编号规则在 A11 修正)。
 *
 * <p>这里守的是<b>静默错判</b>:卷面题号与答案题号一旦错位,交卷时不报错,只是分数错。
 * 因此每条拒绝路径都断言 message 指明是哪一部分。</p>
 *
 * <p>编号契约(任务书 11.7):卷面 {@code id} 是<b>本节内局部题序</b> 1~n,
 * 答案 {@code items[].id} 是<b>答题卡全局编号</b> 前序累加+1 ~ 前序累加+n。
 * 旧规则要求两个集合相等,于是把种子卷(阅读 1-15 / 五选五卷面 1-5、答案 16-20)判成非法,
 * 后台连原值保存都会 400 —— {@link #acceptsSeedShapedPaper()} 就是那份被误拒的卷的阳性对照。</p>
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
                   "content":{"id":"w1","prompt":"写一封申请信","scoringDimensions":["内容要点","语言表达","篇章结构","格式规范"]}}
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

    /** 四个客观题部分各 2 题 + 写作,形状同种子卷:卷面局部 1-2,答案全局 1-2/3-4/5-6/7-8 */
    private JsonNode seedShapedContent() {
        return mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":10,
                   "content":[{"id":"p1","passage":"t","questions":[
                     {"id":1,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]},
                     {"id":2,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}]},
                  {"part":2,"type":"matching","score":10,
                   "content":{"passage":"[[1]] [[2]]","blanks":[{"id":1},{"id":2}],
                     "options":[{"key":"A","text":"s1"},{"key":"B","text":"s2"}]}},
                  {"part":3,"type":"cloze","score":10,
                   "content":{"passage":"[[1]] [[2]]","blanks":[
                     {"id":1,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]},
                     {"id":2,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}},
                  {"part":4,"type":"grammar-fill","score":10,
                   "content":{"passage":"[[1]] [[2]]","blanks":[{"id":1,"hint":"(work)"},{"id":2}]}},
                  {"part":5,"type":"writing","score":15,
                   "content":{"prompt":"题","scoringDimensions":["内容要点","语言表达","篇章结构","格式规范"]}}
                ]}
                """);
    }

    /** {@link #seedShapedContent()} 的答案:全局编号 1-2 / 3-4 / 5-6 / 7-8 */
    private JsonNode seedShapedAnswerKey() {
        return mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":10,"items":[{"id":1,"answer":"A"},{"id":2,"answer":"B"}]},
                  {"part":2,"type":"matching","score":10,"items":[{"id":3,"answer":"A"},{"id":4,"answer":"B"}]},
                  {"part":3,"type":"cloze","score":10,"items":[{"id":5,"answer":"A"},{"id":6,"answer":"B"}]},
                  {"part":4,"type":"grammar-fill","score":10,"items":[{"id":7,"answer":"working"},{"id":8,"answer":"the"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);
    }

    /** 把顶层 sections 数组倒序(只动数组顺序,不改 part) */
    private JsonNode reverseSections(JsonNode root) {
        List<JsonNode> sections = new ArrayList<>();
        root.path("sections").forEach(sections::add);
        ArrayNode reversed = mapper.createArrayNode();
        for (int i = sections.size() - 1; i >= 0; i--) {
            reversed.add(mapper.readTree(sections.get(i).toString()));
        }
        ObjectNode copy = mapper.createObjectNode();
        copy.set("sections", reversed);
        return copy;
    }

    @Test
    @DisplayName("卷面与答案一致的整卷通过全部校验")
    void acceptsConsistentPaper() {
        assertThatCode(() -> validator.validate(validContent(), validAnswerKey(), new BigDecimal("17")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("多部分卷:卷面局部题序 + 答案全局编号即合法(种子卷形状,旧规则会误拒)")
    void acceptsSeedShapedPaper() {
        assertThatCode(() -> validator.validate(seedShapedContent(), seedShapedAnswerKey(), new BigDecimal("55")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sections 数组顺序不影响偏移量:按 part 升序累加")
    void acceptsSectionsInAnyArrayOrder() {
        // 卷面与答案都把 sections 倒过来写:只要 part 升序累加仍得到 1-2/3-4/5-6/7-8 就应通过
        assertThatCode(() -> validator.validate(reverseSections(seedShapedContent()),
                reverseSections(seedShapedAnswerKey()), new BigDecimal("55")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("答案编号不从本节全局起点起即拒绝(否则会静默判错)")
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
                .hasMessageContaining("第 1 部分(reading)的答案编号必须是答题卡全局编号 1~2");
    }

    @Test
    @DisplayName("跨部分答案块重叠即拒绝")
    void rejectsOverlappingAnswerBlocks() {
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":10,"items":[{"id":1,"answer":"A"},{"id":2,"answer":"B"}]},
                  {"part":2,"type":"matching","score":10,"items":[{"id":2,"answer":"A"},{"id":3,"answer":"B"}]},
                  {"part":3,"type":"cloze","score":10,"items":[{"id":5,"answer":"A"},{"id":6,"answer":"B"}]},
                  {"part":4,"type":"grammar-fill","score":10,"items":[{"id":7,"answer":"working"},{"id":8,"answer":"the"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(seedShapedContent(), answerKey, new BigDecimal("55")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("第 2 部分(matching)的答案编号必须是答题卡全局编号 3~4(前序共 2 题)");
    }

    @Test
    @DisplayName("卷面题号误用全局编号即拒绝(局部题序必须从 1 起)")
    void rejectsGloballyNumberedContent() {
        JsonNode content = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":10,
                   "content":[{"id":"p1","passage":"t","questions":[
                     {"id":1,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]},
                     {"id":2,"options":[{"key":"A","text":"a"},{"key":"B","text":"b"}]}]}]},
                  {"part":2,"type":"matching","score":10,
                   "content":{"passage":"[[3]] [[4]]","blanks":[{"id":3},{"id":4}],
                     "options":[{"key":"A","text":"s1"},{"key":"B","text":"s2"}]}},
                  {"part":5,"type":"writing","score":15,
                   "content":{"prompt":"题","scoringDimensions":["内容要点"]}}
                ]}
                """);
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":10,"items":[{"id":1,"answer":"A"},{"id":2,"answer":"B"}]},
                  {"part":2,"type":"matching","score":10,"items":[{"id":3,"answer":"A"},{"id":4,"answer":"B"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);

        assertThatThrownBy(() -> validator.validate(content, answerKey, new BigDecimal("35")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("第 2 部分(matching)的卷面题号必须是本节内连续的 1~2");
    }

    @Test
    @DisplayName("题量与答案条数不等即拒绝")
    void rejectsItemCountMismatch() {
        JsonNode answerKey = mapper.readTree("""
                {"sections":[
                  {"part":1,"type":"reading","score":10,"items":[{"id":1,"answer":"A"}]},
                  {"part":2,"type":"matching","score":10,"items":[{"id":3,"answer":"A"},{"id":4,"answer":"B"}]},
                  {"part":3,"type":"cloze","score":10,"items":[{"id":5,"answer":"A"},{"id":6,"answer":"B"}]},
                  {"part":4,"type":"grammar-fill","score":10,"items":[{"id":7,"answer":"working"},{"id":8,"answer":"the"}]},
                  {"part":5,"type":"writing","score":15,"sampleAnswer":"范文"}
                ]}
                """);

        // part1 只 1 题 → 后续部分的全局起点整体前移,第 2 部分不再落在 3~4
        assertThatThrownBy(() -> validator.validate(seedShapedContent(), answerKey, new BigDecimal("55")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("第 1 部分(reading)的答案编号必须是答题卡全局编号 1~2");
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
