package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.GradeDimension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 作文判分写手测试(练习批改与模考批改共用同一路径)。
 *
 * <p>重点锁两件事:产出的 JSON 形状是学生渲染器的隐式契约(键名与小数位不可漂),
 * 以及满分文案随调用方语义变化(练习=卷面总分,模考=作文满分)。</p>
 */
class WritingGradeSupportTest {

    private final WritingGradeSupport support = new WritingGradeSupport(new ObjectMapper());

    private GradeDimension dimension(String name, String score, String comment) {
        return GradeDimension.builder().name(name).score(new BigDecimal(score)).comment(comment).build();
    }

    @Test
    @DisplayName("产出形状为 dimensions[{name,score,comment}] + sampleAnswer + overallComment")
    void producesStudentRenderableShape() {
        JsonNode result = support.buildResult(
                List.of(dimension("内容要点", "4", "要点齐全"), dimension("语言表达", "3.25", "尚通顺")),
                "范文正文", "总评:结构完整。", new BigDecimal("15"), "作文满分");

        JsonNode dimensions = result.path("dimensions");
        assertThat(dimensions.isArray()).isTrue();
        assertThat(dimensions).hasSize(2);
        assertThat(dimensions.get(0).path("name").asText()).isEqualTo("内容要点");
        // 分值统一保留 1 位小数,学生端直接展示
        assertThat(dimensions.get(1).path("score").decimalValue()).isEqualByComparingTo(new BigDecimal("3.3"));
        assertThat(dimensions.get(0).path("comment").asText()).isEqualTo("要点齐全");
        assertThat(result.path("sampleAnswer").asText()).isEqualTo("范文正文");
        assertThat(result.path("overallComment").asText()).isEqualTo("总评:结构完整。");
    }

    @Test
    @DisplayName("空白范文与总评不写进 JSON(避免下发空字符串键)")
    void omitsBlankOptionalFields() {
        JsonNode result = support.buildResult(List.of(dimension("内容要点", "5", "")), "   ", null,
                new BigDecimal("15"), "作文满分");

        assertThat(result.has("sampleAnswer")).isFalse();
        assertThat(result.has("overallComment")).isFalse();
    }

    @Test
    @DisplayName("维度列表为空即拒绝")
    void requiresDimensions() {
        assertThatThrownBy(() -> support.buildResult(List.of(), null, null, new BigDecimal("15"), "作文满分"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("作文题必须提交至少一个评分维度");

        assertThatThrownBy(() -> support.buildResult(null, null, null, new BigDecimal("15"), "作文满分"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("作文题必须提交至少一个评分维度");
    }

    @Test
    @DisplayName("维度得分为空或为负即拒绝,并点名是哪个维度")
    void rejectsNegativeDimensionScore() {
        assertThatThrownBy(() -> support.buildResult(List.of(dimension("语言表达", "-1", "")), null, null,
                new BigDecimal("15"), "作文满分"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("维度「语言表达」得分不能为空或为负");
    }

    @Test
    @DisplayName("维度分之和超过满分即拒绝,文案用调用方给的满分叫法")
    void rejectsSumOverCap() {
        assertThatThrownBy(() -> support.buildResult(
                List.of(dimension("内容要点", "8", ""), dimension("语言表达", "8", "")), null, null,
                new BigDecimal("15"), "作文满分"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("维度分之和 16.0 超过作文满分 15");

        // 练习批改路径沿用"卷面总分"文案(A4 既有测试依赖该片段)
        assertThatThrownBy(() -> support.buildResult(
                List.of(dimension("内容要点", "8", ""), dimension("语言表达", "8", "")), null, null,
                new BigDecimal("15"), "卷面总分"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("超过卷面总分");
    }

    @Test
    @DisplayName("满分未给(为 null)时不做求和封顶,只保证形状")
    void allowsNullCap() {
        assertThatCode(() -> support.buildResult(List.of(dimension("内容要点", "20", "")), null, null,
                null, "作文满分")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("维度合计保留一位小数")
    void sumsToOneDecimal() {
        assertThat(support.dimensionSum(List.of(dimension("内容要点", "4", ""), dimension("语言表达", "3.2", ""))))
                .isEqualByComparingTo(new BigDecimal("7.2"));
    }
}
