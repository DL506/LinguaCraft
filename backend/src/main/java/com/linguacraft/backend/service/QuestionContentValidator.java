package com.linguacraft.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 题内容校验器五道写入校验的<b>唯一实</b>。
 *
 * <p>管理端录（阶 A2）与 AI 出题共用本组件，两条入库路径使用同一判定口径，“入库即合法”。</p>
 *
 * <p>校验顺序与拒绝文案与 AdminQuestionService 的历史行为逐字一致，
 * 因此 A2 验证文档中的拒绝示例仍然成立：</p>
 * <ol>
 *   <li>结构：按题型检查内容关键字段（文章/题干/选项/空位等）；</li>
 *   <li>答案防呆：content 的 JSON 树中递归禁止出现 answer/explanation/sampleAnswer/acceptAlternatives 键；</li>
 *   <li>编号集合一致：answer_key 的题号/空位编号必须与 content 完全相等（不多不少、不重复）；</li>
 *   <li>答案合法：客观题的 answer 必须落在该项可选项 key 集合内（五选五还要求答案互不重复）；</li>
 *   <li>占位符一致：passage 中的 {@code [[n]]} 编号集合必须与 blanks 的 id 集合相等。</li>
 * </ol>
 *
 * <p>本组件只判定、不改写 content（覆写权威字段的职责在调用方），因此无状态、可被任意服务注入。</p>
 */
@Component
public class QuestionContentValidator {

    /** content 中禁止出现的键（答案与解析只允许存在于 answer_key 列） */
    private static final Set<String> FORBIDDEN_KEYS =
            Set.of("answer", "explanation", "sampleAnswer", "acceptAlternatives");

    /** passage 中的空位占位符 [[n]] */
    private static final Pattern BLANK_MARK = Pattern.compile("\\[\\[(\\d+)]]");

    /**
     * 执行五道校验，任一不过即抛业务异常（code 400，含定位路径）。
     *
     * @param type      题型（调用方需先完成 PracticeRules.isValidType 判定）
     * @param content   下发内容节点
     * @param answerKey 答案与解析节点（写作题可为 null）
     * @throws BizException 校验失败（PARAM_ERROR）或题型非法（TYPE_INVALID）
     */
    public void validate(String type, JsonNode content, JsonNode answerKey) {
        // 校验 1：结构
        switch (type) {
            case "reading" -> {
                requireText(content, "passage", "content");
                ArrayNode questions = requireArray(content, "questions", 1, "content");
                for (int i = 0; i < questions.size(); i++) {
                    JsonNode q = questions.get(i);
                    String path = "content.questions[" + i + "]";
                    requireIntegerId(q, path);
                    requireText(q, "stem", path);
                    requireOptions(q.get("options"), path + ".options", 2);
                }
            }
            case "matching" -> {
                requireText(content, "passage", "content");
                requireArray(content, "blanks", 1, "content");
                requireOptions(content.get("options"), "content.options", 2);
            }
            case "cloze" -> {
                requireText(content, "passage", "content");
                ArrayNode blanks = requireArray(content, "blanks", 1, "content");
                for (int i = 0; i < blanks.size(); i++) {
                    JsonNode blank = blanks.get(i);
                    String path = "content.blanks[" + i + "]";
                    requireIntegerId(blank, path);
                    requireOptions(blank.get("options"), path + ".options", 2);
                }
            }
            case "grammar-fill" -> {
                requireText(content, "passage", "content");
                ArrayNode blanks = requireArray(content, "blanks", 1, "content");
                for (int i = 0; i < blanks.size(); i++) {
                    requireIntegerId(blanks.get(i), "content.blanks[" + i + "]");
                }
            }
            case "writing" -> {
                requireText(content, "prompt", "content");
                requireArray(content, "requirements", 1, "content");
                requireArray(content, "scoringDimensions", 1, "content");
                int wordLimit = content.path("wordLimit").asInt(0);
                if (wordLimit <= 0) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(), "content.wordLimit 必须为正整数");
                }
            }
            default -> throw new BizException(ResultCode.TYPE_INVALID);
        }

        // 校验 2：答案防呆（content 递归禁止出现答案类键）
        assertNoForbiddenKey(content, "content");

        // 校验 3~5：answer_key 与 content 的一致性
        if ("writing".equals(type)) {
            if (answerKey != null && !answerKey.isNull()) {
                if (!answerKey.isObject()
                        || (answerKey.has("sampleAnswer") && !answerKey.get("sampleAnswer").isTextual())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(), "answer_key.sampleAnswer 必须是字符串");
                }
            }
        } else {
            validateAnswerKey(type, content, answerKey);
        }
    }

    /**
     * 校验 answer_key：结构、id 集合一致、答案合法、占位符与空位一致。
     *
     * @param type    题型
     * @param content 已通过结构校验的 content
     * @param key     答案节点
     */
    private void validateAnswerKey(String type, JsonNode content, JsonNode key) {
        if (key == null || !key.isObject()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "客观题必须提供 answer_key(对象)");
        }
        boolean reading = "reading".equals(type);
        String itemsField = reading ? "questions" : "blanks";
        JsonNode keyItems = key.get(itemsField);
        if (keyItems == null || !keyItems.isArray() || keyItems.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "answer_key." + itemsField + " 不能为空数组");
        }

        // id 集合一致（不多不少）
        List<Integer> contentIds = collectIds(reading ? content.get("questions") : content.get("blanks"),
                "content." + itemsField);
        List<Integer> keyIds = collectIds(keyItems, "answer_key." + itemsField);
        if (new LinkedHashSet<>(contentIds).size() != contentIds.size()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "content." + itemsField + " 存在重复编号");
        }
        if (!new HashSet<>(contentIds).equals(new HashSet<>(keyIds))) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "answer_key 的编号集合必须与 content 完全一致(题目:" + contentIds + ",答案:" + keyIds + ")");
        }

        // 答案合法：逐题比对候选 key
        Set<String> usedAnswers = new HashSet<>();
        for (int i = 0; i < keyItems.size(); i++) {
            JsonNode item = keyItems.get(i);
            String path = "answer_key." + itemsField + "[" + i + "]";
            int id = item.path("id").asInt();
            String answer = item.path("answer").asText("").trim();
            if (answer.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + ".answer 不能为空");
            }
            if (reading) {
                Set<String> candidates = optionKeys(findById(content.get("questions"), id).get("options"),
                        "content.questions");
                if (!candidates.contains(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            path + ".answer=" + answer + " 不在该题选项 " + candidates + " 中");
                }
            } else if ("cloze".equals(type)) {
                Set<String> candidates = optionKeys(findById(content.get("blanks"), id).get("options"),
                        "content.blanks");
                if (!candidates.contains(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            path + ".answer=" + answer + " 不在该空选项 " + candidates + " 中");
                }
            } else if ("matching".equals(type)) {
                Set<String> candidates = optionKeys(content.get("options"), "content.options");
                if (!candidates.contains(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            path + ".answer=" + answer + " 不在候选句 " + candidates + " 中");
                }
                // 五选五是一对一匹配，同一句不能被填进两个空
                if (!usedAnswers.add(answer.toUpperCase())) {
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            "answer_key 五选五答案重复:" + answer);
                }
            }
            // grammar-fill：answer 为自由文本，只校验非空（可接受变体写在 acceptAlternatives）
        }

        // 占位符一致：passage 的 [[n]] 集合必须等于 blanks 的 id 集合
        if (!reading) {
            Set<Integer> marks = collectBlankMarks(content.path("passage").asText(""));
            if (!marks.equals(new HashSet<>(contentIds))) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "passage 中的 [[n]] 占位符集合 " + new ArrayList<>(marks)
                                + " 与 blanks 编号集合 " + contentIds + " 不一致");
            }
        }
    }

    /**
     * 递归禁止 content 出现答案类键。
     *
     * @param node 当前节点
     * @param path 路径（用于报错定位）
     */
    private void assertNoForbiddenKey(JsonNode node, String path) {
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                assertNoForbiddenKey(node.get(i), path + "[" + i + "]");
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        var fields = node.properties();
        for (var entry : fields) {
            String key = entry.getKey();
            if (FORBIDDEN_KEYS.contains(key)) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "content 禁止包含答案类字段 " + path + "." + key + "(答案只能写在 answer_key 里)");
            }
            assertNoForbiddenKey(entry.getValue(), path + "." + key);
        }
    }

    /**
     * 收集 questions/blanks 数组的 id 列表（保持声明顺序）。
     *
     * @param items 数组节点
     * @param path  路径（报错定位用）
     * @return id 列表
     */
    private List<Integer> collectIds(JsonNode items, String path) {
        if (items == null || !items.isArray() || items.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 必须是非空数组");
        }
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            requireIntegerId(items.get(i), path + "[" + i + "]");
            ids.add(items.get(i).path("id").asInt());
        }
        return ids;
    }

    /**
     * 提取 passage 中的 [[n]] 占位符编号集合。
     *
     * @param passage 文章正文
     * @return 编号集合
     */
    private Set<Integer> collectBlankMarks(String passage) {
        Set<Integer> marks = new LinkedHashSet<>();
        Matcher matcher = BLANK_MARK.matcher(passage);
        while (matcher.find()) {
            marks.add(Integer.parseInt(matcher.group(1)));
        }
        return marks;
    }

    /**
     * 取选项 key 集合（统一大写比较，容忍小写输入）。
     *
     * @param options 选项数组
     * @param path    路径
     * @return key 集合
     */
    private Set<String> optionKeys(JsonNode options, String path) {
        Set<String> keys = new LinkedHashSet<>();
        if (options == null || !options.isArray()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 必须是数组");
        }
        for (JsonNode option : options) {
            keys.add(option.path("key").asText("").toUpperCase());
        }
        return keys;
    }

    /**
     * 按 id 在数组中查找元素（校验答案候选集用）。
     *
     * @param items 数组节点
     * @param id    目标编号
     * @return 命中的元素
     */
    private JsonNode findById(JsonNode items, int id) {
        for (JsonNode item : items) {
            if (item.path("id").asInt() == id) {
                return item;
            }
        }
        throw new BizException(ResultCode.PARAM_ERROR.getCode(), "找不到编号为 " + id + " 的题目元素");
    }

    /**
     * 校验对象含整数 id 字段。
     *
     * @param node 目标节点
     * @param path 路径
     */
    private void requireIntegerId(JsonNode node, String path) {
        if (node == null || !node.isObject() || !node.has("id") || !node.get("id").isNumber()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 缺少数字字段 id(题号/空位编号)");
        }
    }

    /**
     * 校验非空文本字段。
     *
     * @param node  目标节点
     * @param field 字段名
     * @param path  路径
     */
    private void requireText(JsonNode node, String field, String path) {
        if (!node.hasNonNull(field) || node.get(field).asText("").isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + "." + field + " 不能为空");
        }
    }

    /**
     * 校验数组字段非空。
     *
     * @param node  目标节点
     * @param field 字段名
     * @param min   最少元素数
     * @param path  路径
     * @return 数组节点
     */
    private ArrayNode requireArray(JsonNode node, String field, int min, String path) {
        JsonNode array = node.get(field);
        if (array == null || !array.isArray() || array.size() < min) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    path + "." + field + " 至少需要 " + min + " 项");
        }
        return (ArrayNode) array;
    }

    /**
     * 校验选项数组：key 非空且互不重复。
     *
     * @param options 选项数组
     * @param path    路径
     * @param min     最少个数
     */
    private void requireOptions(JsonNode options, String path, int min) {
        if (options == null || !options.isArray() || options.size() < min) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 至少需要 " + min + " 个选项");
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            JsonNode option = options.get(i);
            String key = option.path("key").asText("").trim();
            if (key.isEmpty() || option.path("text").asText("").isBlank()) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        path + "[" + i + "] 的 key 与 text 均不能为空");
            }
            if (!seen.add(key.toUpperCase())) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), path + " 存在重复选项 key:" + key);
            }
        }
    }
}
