package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单词实体:对应表 vocab_word(演示 20 词;真实 3000 词库后续导入)。
 *
 * <p>meanings 为 JSON 数组字符串,应用层用 ObjectMapper 解析为 List&lt;String&gt;。</p>
 */
@Data
@TableName("vocab_word")
public class VocabWord {

    /** 单词 ID(如 w-001;非自增) */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 拼写 */
    private String spelling;

    /** 音标 */
    private String phonetic;

    /** 词性 */
    private String partOfSpeech;

    /** 释义列表 JSON(string[]) */
    private String meanings;

    /** 例句 */
    private String exampleSentence;

    /** 例句翻译 */
    private String exampleTranslation;

    /** 真题原句 */
    private String realExamSentence;

    /** 真题出处(年份+题型) */
    private String realExamSource;

    /** 分层:high 高频/core 核心/cognitive 认知 */
    private String level;

    /** 发音音频地址(可空) */
    private String audioUrl;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}