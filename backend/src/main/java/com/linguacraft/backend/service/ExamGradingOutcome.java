package com.linguacraft.backend.service;

import com.linguacraft.backend.dto.practice.SubmitDetailDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 整卷客观题判分结果(内部值对象)。
 *
 * @param details        逐题明细(全局编号,含答案与解析)
 * @param objectiveScore 客观题总分(各 section 得分之和,1 位小数)
 * @param correctCount   正确题数
 * @param totalCount     客观题总题数(45)
 * @param sectionScores  各部分得分(part -> 分值)
 */
public record ExamGradingOutcome(List<SubmitDetailDto> details, BigDecimal objectiveScore, int correctCount,
                                 int totalCount, Map<Integer, BigDecimal> sectionScores) {
}