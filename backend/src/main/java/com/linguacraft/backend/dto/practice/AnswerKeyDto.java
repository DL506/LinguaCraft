package com.linguacraft.backend.dto.practice;

import lombok.Data;

import java.util.List;

/**
 * 答案集合(服务端内部结构;question.answer_key JSON 的反序列化目标,不下发)。
 */
@Data
public class AnswerKeyDto {

    /** 阅读题答案项(reading) */
    private List<AnswerItemDto> questions;

    /** 空位答案项(matching/cloze/grammar-fill) */
    private List<AnswerItemDto> blanks;

    /** 写作范文(writing) */
    private String sampleAnswer;
}