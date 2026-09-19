package com.linguacraft.backend.dto.practice;

import lombok.Data;

import java.util.List;

/**
 * 答案项(服务端内部结构;question.answer_key JSON 的元素,不下发)。
 */
@Data
public class AnswerItemDto {

    /** 题号/空位编号 */
    private Integer id;

    /** 标准答案 */
    private String answer;

    /** 可接受的替代答案(语法填空,如大小写变体) */
    private List<String> acceptAlternatives;

    /** 解析 */
    private String explanation;
}