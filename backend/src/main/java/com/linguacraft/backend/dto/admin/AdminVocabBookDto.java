package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端词书列表项(阶段 A6)。
 *
 * <p>{@code declaredTotal} 与 {@code actualWordCount} 并列下发:前者是库里的展示值,
 * 后者是真实单词数,两者不一致要在界面上让管理员看见(并可用 sync-total 显式对齐)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端词书列表项")
public class AdminVocabBookDto {

    /** 词书 ID */
    @Schema(description = "词书 ID", example = "book-syllabus")
    private String id;

    /** 名称 */
    @Schema(description = "名称")
    private String name;

    /** 声明总词数(展示值,可能大于实际导入量) */
    @Schema(description = "声明总词数")
    private Integer declaredTotal;

    /** 真实单词数 */
    @Schema(description = "真实单词数")
    private int actualWordCount;

    /** 过滤等级 */
    @Schema(description = "过滤等级:high/core/cognitive,空为不限")
    private String filterLevel;

    /** 排序 */
    @Schema(description = "排序值")
    private Integer sortOrder;
}
