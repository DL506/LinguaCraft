package com.linguacraft.backend.service;

import com.linguacraft.backend.dto.practice.AnswerItemDto;
import com.linguacraft.backend.dto.practice.AnswerKeyDto;
import com.linguacraft.backend.dto.practice.SubmitDetailDto;
import com.linguacraft.backend.entity.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客观题判分口径测试(为阶段 A10 铺安全网)。
 *
 * <p>判分主链路此前<b>零测试覆盖</b>:A10 要批量补 {@code acceptAlternatives}(直接改变判分结果),
 * 所以先把"什么算答对"钉住。重点是语法填空的三件事:trim、大小写策略、替代答案,
 * 以及替代答案的<b>三种存量形状</b>(数组 / null / 缺键)都不能误判。</p>
 */
class ObjectiveGradingServiceTest {

    private final ObjectiveGradingService service = new ObjectiveGradingService();

    /** 答案项构造 */
    private AnswerItemDto item(int id, String answer, List<String> alternatives) {
        AnswerItemDto item = new AnswerItemDto();
        item.setId(id);
        item.setAnswer(answer);
        item.setAcceptAlternatives(alternatives);
        item.setExplanation("解析 " + id);
        return item;
    }

    private AnswerKeyDto blanks(AnswerItemDto... items) {
        AnswerKeyDto key = new AnswerKeyDto();
        key.setBlanks(List.of(items));
        return key;
    }

    private Question question(String type, String perQuestionScore) {
        Question question = new Question();
        question.setType(type);
        question.setPerQuestionScore(new BigDecimal(perQuestionScore));
        question.setTotalScore(new BigDecimal(perQuestionScore));
        return question;
    }

    // ==================== judgeGrammarBlank(练习与模考共用) ====================

    @Test
    @DisplayName("语法填空:先 trim,首尾空白不影响判定")
    void trimsBeforeCompare() {
        assertThat(service.judgeGrammarBlank("  working ", "working", null, true)).isTrue();
        assertThat(service.judgeGrammarBlank("work ing", "working", null, true)).isFalse();
    }

    @Test
    @DisplayName("语法填空:区分大小写时大小写不同即判错")
    void caseSensitiveRejectsDifferentCase() {
        assertThat(service.judgeGrammarBlank("Faced", "faced", null, true)).isFalse();
        assertThat(service.judgeGrammarBlank("faced", "faced", null, true)).isTrue();
    }

    @Test
    @DisplayName("语法填空:不区分大小写时大小写差异放行(这正是设置项的兜底范围)")
    void caseInsensitiveAcceptsCaseVariants() {
        assertThat(service.judgeGrammarBlank("FACED", "faced", null, false)).isTrue();
        assertThat(service.judgeGrammarBlank("faced", "Faced", null, false)).isTrue();
    }

    @Test
    @DisplayName("语法填空:替代答案数组里的写法都算对")
    void acceptsAlternatives() {
        assertThat(service.judgeGrammarBlank("after", "when", List.of("after", "before"), true)).isTrue();
        assertThat(service.judgeGrammarBlank("which", "that", List.of("which"), true)).isTrue();
        assertThat(service.judgeGrammarBlank("whom", "that", List.of("which"), true)).isFalse();
    }

    @Test
    @DisplayName("语法填空:替代答案的三种存量形状(null / 空数组 / 缺键)都退化为只比标准答案,不得误判或抛错")
    void toleratesAllStoredAlternativeShapes() {
        // null:种子题的形状
        assertThat(service.judgeGrammarBlank("popular", "popular", null, true)).isTrue();
        assertThat(service.judgeGrammarBlank("Popularity", "popular", null, true)).isFalse();
        // 空数组:AI 生成题的形状
        assertThat(service.judgeGrammarBlank("more convenient", "more convenient", List.of(), true)).isTrue();
        assertThat(service.judgeGrammarBlank("convenient", "more convenient", List.of(), true)).isFalse();
        // 数组内含 null 元素(模型可能产出)也不能抛 NPE
        assertThat(service.judgeGrammarBlank("by", "by", Arrays.asList("to", null), true)).isTrue();
    }

    @Test
    @DisplayName("语法填空:未作答(空串/null)在正常题面上判错,不会被空替代答案放行")
    void emptyAnswerIsWrong() {
        assertThat(service.judgeGrammarBlank("", "working", List.of("worked"), true)).isFalse();
        assertThat(service.judgeGrammarBlank(null, "working", List.of("worked"), true)).isFalse();
    }

    @Test
    @DisplayName("边界:标准答案为空串时空白作答会判对 —— 依赖闸门拦下空答案,不是判分兜底")
    void blankStandardAnswerMatchesBlankInput() {
        // 如实钉住当前行为:judgeGrammarBlank 只做字符串比对,空答案与空作答相等即算命中。
        // 它不构成漏洞,因为 QuestionContentValidator 的「answer 不能为空」让这种题面根本进不了库;
        // 若哪天有人放宽那道校验,这条测试就是提醒:兜底不在判分侧。
        assertThat(service.judgeGrammarBlank("", "", List.of(), true)).isTrue();
    }

    // ==================== grade(整题判定) ====================

    @Test
    @DisplayName("阅读/完形/五选五:选项 key 精确相等才算对,语法填空才走变体逻辑")
    void objectiveTypesUseExactKeyCompare() {
        AnswerKeyDto key = blanks(item(1, "B", List.of("C")), item(2, "A", null));

        GradedObjective cloze = service.grade(question("cloze", "2.0"), key, Map.of(1, "B", 2, "A"), true);
        assertThat(cloze.correctCount()).isEqualTo(2);
        assertThat(cloze.score()).isEqualByComparingTo("4.0");

        // 同答案键在完形下,"C" 不会被 acceptAlternatives 放行
        GradedObjective strictCloze = service.grade(question("cloze", "2.0"), key, Map.of(1, "C", 2, "A"), true);
        assertThat(strictCloze.correctCount()).isEqualTo(1);
        assertThat(strictCloze.score()).isEqualByComparingTo("2.0");

        // 同一份答案在语法填空下,替代写法放行
        GradedObjective grammar = service.grade(question("grammar-fill", "1.5"), key, Map.of(1, "C", 2, "a"), false);
        assertThat(grammar.correctCount()).isEqualTo(2);
        assertThat(grammar.score()).isEqualByComparingTo("3.0");
    }

    @Test
    @DisplayName("未作答的空位计入总数并按错误计,明细里回传标准答案与解析")
    void missingAnswersCountAsWrongButStillReported() {
        AnswerKeyDto key = blanks(item(1, "is", null), item(2, "who", null));

        GradedObjective graded = service.grade(question("grammar-fill", "1.0"), key, Map.of(1, "is"), true);

        assertThat(graded.totalCount()).isEqualTo(2);
        assertThat(graded.correctCount()).isEqualTo(1);
        assertThat(graded.score()).isEqualByComparingTo("1.0");
        SubmitDetailDto second = graded.details().get(1);
        assertThat(second.getUserAnswer()).isEmpty();
        assertThat(second.getIsCorrect()).isFalse();
        assertThat(second.getCorrectAnswer()).isEqualTo("who");
        assertThat(second.getExplanation()).isEqualTo("解析 2");
    }

    @Test
    @DisplayName("阅读题走 questions 分支:大小写不敏感策略对 key 比对不生效")
    void readingBranchComparesKeysExactly() {
        AnswerKeyDto key = new AnswerKeyDto();
        key.setQuestions(List.of(item(1, "B", List.of("b"))));

        GradedObjective graded = service.grade(question("reading", "2.0"), key, Map.of(1, "b"), false);

        assertThat(graded.correctCount()).isZero();
        assertThat(graded.score()).isEqualByComparingTo("0.0");
    }

    @Test
    @DisplayName("单题分值缺失时按 1 分兜底,不抛空指针")
    void nullPerQuestionScoreFallsBackToOne() {
        Question question = new Question();
        question.setType("matching");
        AnswerKeyDto key = blanks(item(1, "C", null));

        GradedObjective graded = service.grade(question, key, Map.of(1, "C"), true);

        assertThat(graded.score()).isEqualByComparingTo("1.0");
    }
}
