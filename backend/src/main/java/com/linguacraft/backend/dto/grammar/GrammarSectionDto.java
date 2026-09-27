package com.linguacraft.backend.dto.grammar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语法章节响应(对应前端 GrammarSection:markdown 由前端 markdown-it + hljs 渲染)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarSectionDto {

    /** 章节 ID */
    @Schema(description = "章节 ID", example = "tenses-overview")
    private String id;

    /** 章节标题 */
    @Schema(description = "章节标题", example = "时态基本用法")
    private String title;

    /** Markdown 内容 */
    @Schema(description = "Markdown 内容(考点精讲/真题示例/易错点辨析/专项练习)")
    private String markdown;
}