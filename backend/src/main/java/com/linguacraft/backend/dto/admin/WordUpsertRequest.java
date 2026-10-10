package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单词新建/更新请求(阶段 A6)。
 *
 * <p>入库形状与学生端读接口一致:{@code level} 必须是 {@code high/core/cognitive} 之一
 * (前端 {@code types/vocab.d.ts} 的联合类型与 {@code WordListTable} 的标签映射都依赖它);
 * {@code meanings} 落 {@code vocab_word.meanings} JSON 列,存成字符串数组。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "单词新建/更新请求")
public class WordUpsertRequest {

    /** 单词 ID(新建可空,服务端按现有最大编号生成;更新必填路径值) */
    @Schema(description = "单词 ID(新建可空)")
    private String id;

    /** 拼写 */
    @NotBlank(message = "拼写 spelling 不能为空")
    @Schema(description = "拼写", example = "abandon")
    private String spelling;

    /** 音标 */
    @NotBlank(message = "音标 phonetic 不能为空")
    @Schema(description = "音标", example = "/əˈbændən/")
    private String phonetic;

    /** 词性 */
    @NotBlank(message = "词性 partOfSpeech 不能为空")
    @Schema(description = "词性", example = "v.")
    private String partOfSpeech;

    /** 释义列表(落库为 JSON 字符串数组) */
    @NotEmpty(message = "释义 meanings 至少一项")
    @Schema(description = "释义列表")
    private List<String> meanings;

    /** 等级 */
    @NotBlank(message = "等级 level 不能为空")
    @Schema(description = "等级:high/core/cognitive", example = "core")
    private String level;

    /** 例句 */
    @Schema(description = "例句(可空)")
    private String exampleSentence;

    /** 例句翻译 */
    @Schema(description = "例句翻译(可空)")
    private String exampleTranslation;

    /** 真题句 */
    @Schema(description = "真题句(可空)")
    private String realExamSentence;

    /** 真题来源 */
    @Schema(description = "真题来源说明(可空)")
    private String realExamSource;

    /** 发音音频 */
    @Schema(description = "音频 URL(可空)")
    private String audioUrl;
}
