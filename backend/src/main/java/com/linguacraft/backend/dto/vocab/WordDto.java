package com.linguacraft.backend.dto.vocab;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单词响应(对应前端 Word)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordDto {

    /** 单词 ID */
    @Schema(description = "单词 ID", example = "w-001")
    private String id;

    /** 拼写 */
    @Schema(description = "拼写", example = "abandon")
    private String spelling;

    /** 音标 */
    @Schema(description = "音标", example = "/əˈbændən/")
    private String phonetic;

    /** 词性 */
    @Schema(description = "词性", example = "v.")
    private String partOfSpeech;

    /** 释义列表 */
    @Schema(description = "释义列表")
    private List<String> meanings;

    /** 例句 */
    @Schema(description = "例句")
    private String exampleSentence;

    /** 例句翻译 */
    @Schema(description = "例句翻译")
    private String exampleTranslation;

    /** 真题原句 */
    @Schema(description = "真题原句")
    private String realExamSentence;

    /** 真题出处(年份+题型) */
    @Schema(description = "真题出处(年份+题型)")
    private String realExamSource;

    /** 分层:high/core/cognitive */
    @Schema(description = "分层:high 高频/core 核心/cognitive 认知", example = "high")
    private String level;

    /** 发音音频地址(可空) */
    @Schema(description = "发音音频地址(可空)")
    private String audioUrl;
}