package com.linguacraft.backend.service;

import com.linguacraft.backend.dto.practice.AnswerItemDto;
import com.linguacraft.backend.dto.practice.AnswerKeyDto;
import com.linguacraft.backend.dto.practice.SubmitDetailDto;
import com.linguacraft.backend.entity.Question;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客观题判分服务:数据库答案比对(设计原则:优先数据库答案比对)。
 *
 * <p>判分口径(与前端 mock 一致):</p>
 * <ul>
 *   <li>阅读/五选五/完形:选项 key 精确相等(未作答视为错误);</li>
 *   <li>语法填空:去除首尾空白后比对,大小写敏感度读用户设置,
 *       并接受 acceptAlternatives 中的替代答案;</li>
 *   <li>得分 = 正确数 × 单题分值(保留 1 位小数)。</li>
 * </ul>
 */
@Service
public class ObjectiveGradingService {

    /**
     * 对客观题判分。
     *
     * @param question      题目实体(提供题型与单题分值)
     * @param key           答案集合(来自 question.answer_key)
     * @param answers       用户作答(题号/空位编号 -> 作答)
     * @param caseSensitive 语法填空是否区分大小写(读用户设置)
     * @return 判分结果(明细/得分/正确数/总数)
     */
    public GradedObjective grade(Question question, AnswerKeyDto key, Map<Integer, String> answers, boolean caseSensitive) {
        List<SubmitDetailDto> details = new ArrayList<>();
        int correctCount = 0;

        // 阅读:questions 答案项
        if (key.getQuestions() != null) {
            for (AnswerItemDto item : key.getQuestions()) {
                String userAnswer = answers.getOrDefault(item.getId(), "");
                boolean isCorrect = userAnswer.equals(item.getAnswer());
                if (isCorrect) {
                    correctCount++;
                }
                details.add(toDetail(item, userAnswer, isCorrect));
            }
        }

        // 五选五 / 完形 / 语法填空:blanks 答案项
        if (key.getBlanks() != null) {
            boolean grammarFill = "grammar-fill".equals(question.getType());
            for (AnswerItemDto item : key.getBlanks()) {
                String userAnswer = answers.getOrDefault(item.getId(), "");
                boolean isCorrect = grammarFill
                        ? judgeGrammarBlank(userAnswer, item, caseSensitive)
                        : userAnswer.equals(item.getAnswer());
                if (isCorrect) {
                    correctCount++;
                }
                details.add(toDetail(item, userAnswer, isCorrect));
            }
        }

        BigDecimal perQuestion = question.getPerQuestionScore() == null
                ? BigDecimal.ONE : question.getPerQuestionScore();
        BigDecimal score = perQuestion.multiply(BigDecimal.valueOf(correctCount)).setScale(1, RoundingMode.HALF_UP);
        return new GradedObjective(details, score, correctCount, details.size());
    }

    /**
     * 语法填空单空判分:trim + 大小写策略 + 替代答案。
     *
     * @param userText      用户输入
     * @param item          答案项(标准答案 + 可接受替代答案)
     * @param caseSensitive 是否区分大小写
     * @return 是否答对
     */
    private boolean judgeGrammarBlank(String userText, AnswerItemDto item, boolean caseSensitive) {
        String input = userText == null ? "" : userText.trim();
        List<String> candidates = new ArrayList<>();
        if (item.getAnswer() != null) {
            candidates.add(item.getAnswer());
        }
        if (item.getAcceptAlternatives() != null) {
            candidates.addAll(item.getAcceptAlternatives());
        }
        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if (caseSensitive ? candidate.equals(input) : candidate.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建单题明细(正确答案始终返回标准答案)。
     *
     * @param item       答案项
     * @param userAnswer 用户作答
     * @param isCorrect  是否正确
     * @return 明细 DTO
     */
    private SubmitDetailDto toDetail(AnswerItemDto item, String userAnswer, boolean isCorrect) {
        return SubmitDetailDto.builder()
                .id(item.getId())
                .userAnswer(userAnswer)
                .correctAnswer(item.getAnswer())
                .isCorrect(isCorrect)
                .explanation(item.getExplanation())
                .build();
    }
}