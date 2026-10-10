package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端单词列表/详情项(阶段 A6)。
 *
 * <p>字段与学生端 {@code WordDto} 对齐,另加两个"能不能删"的引用计数:
 * {@code learnedUserCount}(有多少人在学)与 {@code favoriteCount}(被收藏次数),
 * 任一大于 0 时后端拒绝删除,界面应提示"请改用停用/移出词书"。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端单词项")
public class AdminWordDto {

    /** 单词 ID */
    @Schema(description = "单词 ID", example = "w-001")
    private String id;

    /** 拼写 */
    @Schema(description = "拼写")
    private String spelling;

    /** 音标 */
    @Schema(description = "音标")
    private String phonetic;

    /** 词性 */
    @Schema(description = "词性")
    private String partOfSpeech;

    /** 释义 */
    @Schema(description = "释义列表")
    private List<String> meanings;

    /** 等级 */
    @Schema(description = "等级:high/core/cognitive")
    private String level;

    /** 例句 */
    @Schema(description = "例句")
    private String exampleSentence;

    /** 例句翻译 */
    @Schema(description = "例句翻译")
    private String exampleTranslation;

    /** 真题句 */
    @Schema(description = "真题句")
    private String realExamSentence;

    /** 真题来源 */
    @Schema(description = "真题来源")
    private String realExamSource;

    /** 音频 */
    @Schema(description = "音频 URL")
    private String audioUrl;

    /** 正在学的用户数(>0 不可删) */
    @Schema(description = "在学用户数")
    private int learnedUserCount;

    /** 被收藏次数(>0 不可删) */
    @Schema(description = "被收藏次数")
    private int favoriteCount;
}
