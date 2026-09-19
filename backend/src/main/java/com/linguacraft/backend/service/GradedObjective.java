package com.linguacraft.backend.service;

import com.linguacraft.backend.dto.practice.SubmitDetailDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客观题判分结果(内部值对象)。
 *
 * @param details      逐题明细(含答案与解析)
 * @param score        得分(按题型单题分值累计,保留 1 位小数)
 * @param correctCount 正确数
 * @param totalCount   总题数
 */
public record GradedObjective(List<SubmitDetailDto> details, BigDecimal score, int correctCount, int totalCount) {
}