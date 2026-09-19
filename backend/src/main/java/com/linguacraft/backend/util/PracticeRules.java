package com.linguacraft.backend.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * 练习题库规则常量:题型合法性校验与练习场景分值。
 *
 * <p>分值口径与前端 mock scoreRule 一致(AI 出题等新建题目时使用);
 * 已有题目以数据库 question 表中的分值为准。</p>
 */
public final class PracticeRules {

    /** 合法题型集合 */
    public static final Set<String> VALID_TYPES = Set.of("reading", "matching", "cloze", "grammar-fill", "writing");

    /** 题型中文名(用于生成默认标题) */
    private static final Map<String, String> TYPE_NAMES = Map.of(
            "reading", "阅读理解",
            "matching", "五选五",
            "cloze", "完形填空",
            "grammar-fill", "语法填空",
            "writing", "作文");

    /** 练习场景总分(与前端 mock 一致) */
    private static final Map<String, BigDecimal> TOTAL_SCORES = Map.of(
            "reading", new BigDecimal("10"),
            "matching", new BigDecimal("10"),
            "cloze", new BigDecimal("20"),
            "grammar-fill", new BigDecimal("15"),
            "writing", new BigDecimal("15"));

    /** 练习场景单题分值(与前端 mock 一致) */
    private static final Map<String, BigDecimal> PER_QUESTION_SCORES = Map.of(
            "reading", new BigDecimal("2"),
            "matching", new BigDecimal("2"),
            "cloze", new BigDecimal("2"),
            "grammar-fill", new BigDecimal("1.5"),
            "writing", new BigDecimal("15"));

    private PracticeRules() {
    }

    /**
     * 题型是否合法。
     *
     * @param type 题型
     * @return 是否属于五种合法题型
     */
    public static boolean isValidType(String type) {
        return type != null && VALID_TYPES.contains(type);
    }

    /**
     * 题型中文名。
     *
     * @param type 题型
     * @return 中文名(未知题型返回原值)
     */
    public static String typeName(String type) {
        return TYPE_NAMES.getOrDefault(type, type);
    }

    /**
     * 练习场景总分。
     *
     * @param type 题型
     * @return 总分(未知题型返回 10)
     */
    public static BigDecimal totalScore(String type) {
        return TOTAL_SCORES.getOrDefault(type, new BigDecimal("10"));
    }

    /**
     * 练习场景单题分值。
     *
     * @param type 题型
     * @return 单题分值(未知题型返回 2)
     */
    public static BigDecimal perQuestionScore(String type) {
        return PER_QUESTION_SCORES.getOrDefault(type, new BigDecimal("2"));
    }
}