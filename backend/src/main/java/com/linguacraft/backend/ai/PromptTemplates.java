package com.linguacraft.backend.ai;

import tools.jackson.databind.JsonNode;
import com.linguacraft.backend.entity.Question;

import java.util.List;
import java.util.Map;

/**
 * AI 提示词模板:出题、客观题判分、作文评分。
 *
 * <p>共同约定:模型只输出 JSON(可被代码块围栏包裹),所有题号/空位编号按数组顺序从 1 连续编号,
 * 解析与解析说明使用中文;答案字段必填以便服务端判分与下发。</p>
 */
public final class PromptTemplates {

    /** 统一 JSON 输出约束后缀 */
    private static final String JSON_ONLY = "\n\n要求:只输出一个合法 JSON 对象,不要输出任何其他文字或解释,不要使用代码块围栏。";

    private PromptTemplates() {
    }

    /**
     * AI 出题提示词(五种题型)。
     *
     * @param type 题型
     * @return 提示词
     */
    public static String generatePrompt(String type) {
        return switch (type) {
            case "reading" -> """
                    你是一名广东专插本公共英语命题老师。请生成 1 篇阅读理解练习(文章 150~220 词,题材贴近校园/学习/生活),
                    并出 5 道单项选择题(考查主旨、细节、词义、推断)。
                    输出 JSON 结构:
                    {"title":"标题","difficulty":2,"tags":["主旨大意","细节理解"],
                     "passage":"文章正文(英文)",
                     "questions":[{"stem":"题干(英文)","options":[{"key":"A","text":"选项1"},{"key":"B","text":"选项2"},{"key":"C","text":"选项3"},{"key":"D","text":"选项4"}],"answer":"B","explanation":"中文解析"}]}
                    其中 questions 恰好 5 项,answer 为正确选项 key;difficulty 取 1~5 的整数。""" + JSON_ONLY;
            case "matching" -> """
                    你是一名广东专插本公共英语命题老师。请生成 1 篇"五选五"短文句匹配练习:
                    写一段 120~180 词的英文短文,挖出 5 个空(空位在文章中用 [[1]] [[2]] [[3]] [[4]] [[5]] 占位),
                    并提供 5 个候选句子(A~E),每个空恰好对应其中一句,五句各用一次(本题型无干扰项)。
                    输出 JSON 结构:
                    {"title":"标题","difficulty":3,"tags":["篇章逻辑"],
                     "passage":"含 [[n]] 占位符的文章",
                     "options":[{"key":"A","text":"句子"},{"key":"B","text":"句子"},{"key":"C","text":"句子"},{"key":"D","text":"句子"},{"key":"E","text":"句子"}],
                     "blanks":[{"id":1,"answer":"B","explanation":"中文解析"}]}
                    其中 blanks 恰好 5 项,id 依次为 1~5,answer 为对应选项 key 且五个答案互不重复。""" + JSON_ONLY;
            case "cloze" -> """
                    你是一名广东专插本公共英语命题老师。请生成 1 篇完形填空练习:
                    写一段 180~240 词的英文短文,挖出 10 个空(空位在文章中用 [[1]] ~ [[10]] 占位),
                    每个空给出 4 个选项。
                    输出 JSON 结构:
                    {"title":"标题","difficulty":3,"tags":["词义辨析","上下文"],
                     "passage":"含 [[n]] 占位符的文章",
                     "blanks":[{"id":1,"options":[{"key":"A","text":"选项1"},{"key":"B","text":"选项2"},{"key":"C","text":"选项3"},{"key":"D","text":"选项4"}],"answer":"B","explanation":"中文解析"}]}
                    其中 blanks 恰好 10 项,id 依次为 1~10。""" + JSON_ONLY;
            case "grammar-fill" -> """
                    你是一名广东专插本公共英语命题老师。请生成 1 篇语法填空练习:
                    写一段 150~200 词的英文短文,挖出 10 个空(空位在文章中用 [[1]] ~ [[10]] 占位),
                    部分空给提示词(hint,如 "(work)"),部分空无提示词(纯语法功能词)。
                    输出 JSON 结构:
                    {"title":"标题","difficulty":3,"tags":["动词时态","非谓语动词"],
                     "passage":"含 [[n]] 占位符的文章",
                     "blanks":[{"id":1,"hint":"(work)","answer":"working","acceptAlternatives":["worked"],"explanation":"中文解析"}]}
                    其中 blanks 恰好 10 项,id 依次为 1~10;无提示词的空省略 hint 字段。""" + JSON_ONLY;
            case "writing" -> """
                    你是一名广东专插本公共英语命题老师。请生成 1 道应用文写作题(如书信/通知/申请,120 词左右)。
                    输出 JSON 结构:
                    {"title":"标题","difficulty":3,"tags":["应用文"],
                     "prompt":"写作题目与情境(中文说明 + 英文要点)",
                     "requirements":["要点1","要点2","要点3"],
                     "wordLimit":120,
                     "scoringDimensions":["内容要点","语言表达","篇章结构","格式规范"],
                     "sampleAnswer":"参考范文(英文,120 词左右)"}
                    scoringDimensions 恰好 4 项。""" + JSON_ONLY;
            default -> throw new IllegalArgumentException("不支持的题型: " + type);
        };
    }

    /**
     * 客观题 AI 判分提示词(题库无答案时使用)。
     *
     * @param question 题目(含下发内容)
     * @param answers  用户作答(题号 -> 作答)
     * @return 提示词
     */
    public static String objectiveGradingPrompt(Question question, Map<Integer, String> answers) {
        return """
                你是严谨的英语阅卷老师。请对以下客观题作答进行判分,并给出正确答案与中文解析。
                题目内容(JSON):
                """ + question.getContent() + """

                学生作答(题号 -> 作答):
                """ + answers + """

                输出 JSON 结构:
                {"details":[{"id":1,"correctAnswer":"标准答案","isCorrect":true,"explanation":"中文解析"}]}
                要求:details 必须覆盖上述学生作答中的全部题号;isCorrect 按标准答案判定;解析简洁明确。""" + JSON_ONLY;
    }

    /**
     * 作文 AI 评分提示词。
     *
     * @param question 写作题(含题目内容)
     * @param content  学生作文正文
     * @return 提示词
     */
    public static String writingGradingPrompt(Question question, String content) {
        return """
                你是广东专插本英语阅卷老师。请按给定评分维度对学生的应用文作文进行评分。
                题目(JSON):
                """ + question.getContent() + """

                学生作文:
                """ + content + """

                输出 JSON 结构:
                {"dimensions":[{"name":"内容要点","score":5,"comment":"中文评语"}],
                 "totalScore":11,
                 "sampleAnswer":"参考范文(英文,120 词左右)",
                 "overallComment":"总评(中文,含优点与改进建议)"}
                要求:dimensions 覆盖题目的评分维度列表,各维度分数之和等于 totalScore,totalScore 不超过满分 15 的整数或一位小数。""" + JSON_ONLY;
    }

    /**
     * 题目校核提示词(阶段 A10):对"已经写好的一道题"做三类检查。
     *
     * <p>与出题提示词的关键差别:这里不产出新题,只评判现有答案与解析,所以要求模型
     * <b>不得新增编号</b>(否则前端无法对回表单的行)。三类检查各自的边界写在提示词里:</p>
     * <ul>
     *   <li>{@code alternatives}:只有语法填空需要,且只允许填"确实该判对"的写法 ——
     *       判分是纯字符串比对,塞进错误写法会直接放过错答;</li>
     *   <li>{@code answerRisk}:标准答案本身是否可疑(可能选错、可能不唯一)。
     *       服务端与前端都<b>不会</b>据此自动改答案,只作为提醒;</li>
     *   <li>{@code explanationRisk}:解析与答案是否一致、是否过简,建议解析只在管理员采纳时写入。</li>
     * </ul>
     *
     * <p>分批评故:{@code answerKey} 只是本题的一部分(见 {@code AiReviewService#REVIEW_CHUNK_SIZE}),
     * 而题面是完整的。不告诉模型这一点,它会回报"answer_key 缺了 1-5 空"这种<b>假问题</b>
     * (实测出现过),管理员看了会以为题目坏了。所以必须显式给出本批编号范围。</p>
     *
     * @param type      题型
     * @param content   题面 JSON(可含未通过闸门的内容,校核正是要在它入库前看)
     * @param answerKey 本批答案 JSON
     * @param batchIds  本批要校核的编号(题面里其它编号属其它批次)
     * @return 提示词
     */
    public static String reviewPrompt(String type, JsonNode content, JsonNode answerKey, List<Integer> batchIds) {
        boolean grammar = "grammar-fill".equals(type);
        String alternativeRule = grammar
                ? "- alternatives:除标准答案外,这个空还应该判对的写法(如 another/another one 这类同义写法、"
                        + "两种都说得通的时态)。只填你确信的,不确定的就不填;大小写差异不要写进来(系统已有大小写开关);"
                : "- 本题型按选项 key 精确比对,alternatives 一律返回空数组。";
        // 刻意用紧凑 JSON(不是 toPrettyString):校核要带完整题面与答案,缩进会白耗一成输入 token
        return """
                你是严谨的英语命题与阅卷双岗老师,现在请校核一道已经写好的题目,只找问题、不要重写题目。
                题型:""" + type + """

                本批只校核这些编号:""" + batchIds + """
                (题面是完整的,但其中不属于上述编号的空位/小题由别的批次负责,本批一律不评、也不要报告"答案缺失"。)

                题面 content(JSON,不含答案):
                """ + (content == null ? "{}" : content.toString()) + """

                本批答案 answer_key(JSON):
                """ + (answerKey == null ? "{}" : answerKey.toString()) + """

                输出 JSON 结构:
                {"summary":"本批一句话结论(中文,不超过40字)",
                 "items":[{"id":1,"alternatives":[],"answerRisk":"ok","explanationRisk":"ok","explanation":"","note":""}]}

                硬性要求:
                - items 只能包含上述本批编号,一个不多一个不少;不要出现别的编号,也不要合并多项;
                - answerRisk 只能取 ok / mayBeWrong / notUnique:
                  mayBeWrong = 按题面看这个标准答案本身可能是错的;
                  notUnique = 题面语境下还有另一个明显也成立的答案,纯字符串比对会误判;
                - explanationRisk 只能取 ok / mismatch / tooThin:mismatch = 解析与答案不一致或答非所问,
                  tooThin = 解析过简、学生看不懂为什么选它;
                - note 与 explanation 都要简短(note 不超过30字),没有问题的编号两者都留空字符串;
                - 只有当 answerRisk 或 explanationRisk 不是 ok 时,才在 note 里写清具体理由;
                  需要改写解析时把改写后的中文解析放进 explanation,否则该字段留空字符串。""" + alternativeRule + """
                - 不要为了凑问题而编造问题:确实没问题就全填 ok、note 留空;
                - 绝对不要输出除这个 JSON 以外的任何文字,不要使用代码块围栏。""" + JSON_ONLY;
    }

    /**
     * 从 AI 返回文本中提取 JSON 主干(容错:剥离代码块围栏与多余文字)。
     *
     * @param text AI 返回文本
     * @return JSON 文本
     */
    public static String extractJson(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        int brace = trimmed.indexOf('{');
        int bracket = trimmed.indexOf('[');
        int start;
        int end;
        if (brace >= 0 && (bracket < 0 || brace < bracket)) {
            start = brace;
            end = trimmed.lastIndexOf('}');
        } else if (bracket >= 0) {
            start = bracket;
            end = trimmed.lastIndexOf(']');
        } else {
            return trimmed;
        }
        return start >= 0 && end > start ? trimmed.substring(start, end + 1) : trimmed;
    }

    /**
     * 判断 AI 返回 JSON 是否具备指定非空字段(用于结构校验)。
     *
     * @param node  JSON 节点
     * @param field 字段名
     * @return 是否存在且非空
     */
    public static boolean hasField(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) && !node.get(field).asText("").isBlank();
    }
}