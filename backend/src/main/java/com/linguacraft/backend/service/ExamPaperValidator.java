package com.linguacraft.backend.service;

import tools.jackson.databind.JsonNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 真题卷 JSON 校验器(阶段 A6-③)。
 *
 * <p>这是后台最危险的写路径:模考客观题判分<b>完全按 {@code answer_key} 里的全局题号配对</b>
 * ({@code ExamService.gradeObjective} 拿学生作答的题号去答案里找),所以一旦卷面题号与答案题号错位,
 * 交卷时不会报错,而是<b>静默判错</b> —— 学生的分数是错的却无人察觉。这里在落库前把这类错挡死。</p>
 *
 * <p>四条硬规则:</p>
 * <ol>
 *   <li>结构:每个 section 必须有 {@code part}(全卷唯一)、{@code type}、{@code score}(正数);</li>
 *   <li>分值:{@code sum(section.score)} 必须等于 {@code exam_paper.total_score};</li>
 *   <li>题号:非写作部分的答案 {@code items[].id} 集合必须与该部分卷面题号集合<b>完全相等</b>,
 *       且全卷不重复(卷面里三篇阅读的题号是 1-5/6-10/11-15 连续编号,与答案同一套);</li>
 *   <li>写作:有且仅有一个 {@code type=writing} 的部分,分值必须为 15
 *       (异步批改器按 15 分封顶,不一致会让 AI 分数与人工分数口径分叉)。</li>
 * </ol>
 */
@Component
public class ExamPaperValidator {

    /** 写作部分固定满分(与 ExamAsyncGrader 与种子的 15 分口径一致) */
    private static final BigDecimal WRITING_SCORE = new BigDecimal("15");

    /**
     * 校验整卷内容。
     *
     * @param content    卷面 JSON({@code {sections:[{part,type,score,content}]}})
     * @param answerKey  答案 JSON({@code {sections:[{part,type,score,items|sampleAnswer}]}})
     * @param totalScore 卷面总分
     * @throws BizException 任一条不过即 400,message 指明部分号与原因
     */
    public void validate(JsonNode content, JsonNode answerKey, BigDecimal totalScore) {
        JsonNode sections = sectionsOf(content, "content");
        JsonNode keySections = sectionsOf(answerKey, "answer_key");

        Set<Integer> parts = new HashSet<>();
        Set<Integer> allAnswerIds = new HashSet<>();
        BigDecimal sum = BigDecimal.ZERO;
        int writingSections = 0;

        for (JsonNode section : sections) {
            int part = requirePart(section, parts, "content");
            String type = section.path("type").asText("").trim();
            if (type.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "第 " + part + " 部分缺少 type");
            }
            BigDecimal score = section.path("score").decimalValue();
            if (score == null || score.signum() <= 0) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "第 " + part + " 部分的 score 必须为正数");
            }
            sum = sum.add(score);

            if ("writing".equals(type)) {
                writingSections++;
                if (score.compareTo(WRITING_SCORE) != 0) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            "写作部分分值必须为 " + WRITING_SCORE + "(异步批改器按此封顶),当前为 " + score);
                }
                require(section.path("content").hasNonNull("prompt"), "第 " + part + " 部分缺少写作题目 prompt");
                require(section.path("content").path("scoringDimensions").isArray()
                        && !section.path("content").path("scoringDimensions").isEmpty(),
                        "第 " + part + " 部分缺少评分维度 scoringDimensions");
            } else {
                List<Integer> contentIds = paperIdsOf(section.path("content"), part);
                JsonNode keySection = findKeySection(keySections, part);
                List<Integer> keyIds = answerIdsOf(keySection, part);
                if (!new LinkedHashSet<>(contentIds).equals(new LinkedHashSet<>(keyIds))) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            "第 " + part + " 部分的答案编号与题目编号不一致(题目:" + contentIds + ",答案:" + keyIds
                                    + ");客观题按编号配对,不一致会静默判错");
                }
                for (Integer id : keyIds) {
                    if (!allAnswerIds.add(id)) {
                        throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                                "答题卡编号 " + id + " 在全卷重复出现,判分会串题");
                    }
                }
            }
        }

        // 答案里多出来的部分同样要拦(否则某部分永远判不到答案)
        for (JsonNode keySection : keySections) {
            int part = keySection.path("part").asInt(-1);
            boolean matched = false;
            for (JsonNode section : sections) {
                if (section.path("part").asInt(-2) == part) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "answer_key 多出第 " + part + " 部分,卷面没有它");
            }
        }

        if (writingSections != 1) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "整卷必须恰好包含一个写作部分,当前 " + writingSections + " 个");
        }
        if (totalScore != null && sum.compareTo(totalScore) != 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "各部分分值之和 " + sum.stripTrailingZeros().toPlainString() + " 与卷面总分 "
                            + totalScore.stripTrailingZeros().toPlainString() + " 不一致");
        }
    }

    /**
     * 取某一侧的 sections 数组。
     *
     * @param root 卷面或答案节点
     * @param side 侧名(报错用)
     * @return sections 数组
     */
    private JsonNode sectionsOf(JsonNode root, String side) {
        if (root == null || !root.isObject()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), side + " 必须是 JSON 对象");
        }
        JsonNode sections = root.get("sections");
        if (sections == null || !sections.isArray() || sections.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), side + ".sections 必须是非空数组");
        }
        return sections;
    }

    /**
     * 取并校验 part 唯一。
     *
     * @param section 部分节点
     * @param seen    已出现过的 part
     * @param side    侧名
     * @return part 号
     */
    private int requirePart(JsonNode section, Set<Integer> seen, String side) {
        if (!section.path("part").isNumber()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), side + " 中每个部分都必须有数字字段 part");
        }
        int part = section.path("part").asInt();
        if (!seen.add(part)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), side + " 存在重复的 part:" + part);
        }
        return part;
    }

    /**
     * 收集卷面某部分的题目编号。
     *
     * <p>阅读的 content 是<b>数组</b>(多篇文章,每篇带自己的 questions),
     * 其余题型是对象(带 blanks)。两种都要覆盖。</p>
     *
     * @param content 该部分的卷面内容
     * @param part    部分号(报错用)
     * @return 题号列表
     */
    private List<Integer> paperIdsOf(JsonNode content, int part) {
        List<Integer> ids = new ArrayList<>();
        if (content == null || content.isNull() || content.isMissingNode()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "第 " + part + " 部分缺少卷面内容");
        }
        if (content.isArray()) {
            for (JsonNode passage : content) {
                collectFrom(passage.get("questions"), part, "questions", ids);
                if (ids.isEmpty()) {
                    collectFrom(passage.get("blanks"), part, "blanks", ids);
                }
            }
            if (ids.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "第 " + part + " 部分的每篇文章都应有 questions(或 blanks)");
            }
            return ids;
        }
        collectFrom(content.get("questions"), part, "questions", ids);
        collectFrom(content.get("blanks"), part, "blanks", ids);
        if (ids.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "第 " + part + " 部分卷面缺少 questions/blanks 题号数组");
        }
        return ids;
    }

    /**
     * 从题目或空位数组里取编号,并顺手检查选项齐备。
     *
     * @param items  数组节点
     * @param part   部分号
     * @param field  字段名(报错用)
     * @param target 收集容器
     */
    private void collectFrom(JsonNode items, int part, String field, List<Integer> target) {
        if (items == null || !items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (!item.path("id").isNumber()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "第 " + part + " 部分的 " + field + " 每项都必须有数字 id");
            }
            target.add(item.path("id").asInt());
            JsonNode options = item.get("options");
            if (options != null && options.isArray() && options.size() < 2) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "第 " + part + " 部分题号 " + item.path("id").asInt() + " 的选项不足 2 个");
            }
        }
    }

    /**
     * 找到答案侧对应部分。
     *
     * @param keySections 答案 sections
     * @param part        部分号
     * @return 答案部分节点
     */
    private JsonNode findKeySection(JsonNode keySections, int part) {
        for (JsonNode section : keySections) {
            if (section.path("part").asInt(-1) == part) {
                return section;
            }
        }
        throw new BizException(ResultCode.PARAM_ERROR.getCode(), "answer_key 缺少第 " + part + " 部分");
    }

    /**
     * 取答案部分的编号列表。
     *
     * @param keySection 答案部分
     * @param part       部分号
     * @return 编号列表
     */
    private List<Integer> answerIdsOf(JsonNode keySection, int part) {
        JsonNode items = keySection.get("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "answer_key 第 " + part + " 部分缺少非空 items 数组");
        }
        List<Integer> ids = new ArrayList<>();
        for (JsonNode item : items) {
            if (!item.path("id").isNumber()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "answer_key 第 " + part + " 部分的 items 每项都必须有数字 id");
            }
            ids.add(item.path("id").asInt());
            if (item.path("answer").asText("").isBlank()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "answer_key 第 " + part + " 部分题号 " + item.path("id").asInt() + " 的答案为空");
            }
        }
        return ids;
    }

    /**
     * 布尔断言。
     *
     * @param condition 条件
     * @param message   文案
     */
    private void require(boolean condition, String message) {
        if (!condition) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), message);
        }
    }
}
