package com.linguacraft.backend.dto.vocab;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 词书响应(对应前端 VocabBook;learned 由进度表实时派生)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VocabBookDto {

    /** 词书 ID */
    @Schema(description = "词书 ID", example = "book-syllabus")
    private String id;

    /** 词书名称 */
    @Schema(description = "词书名称", example = "广东专插本考纲词汇")
    private String name;

    /** 词书总词数 */
    @Schema(description = "词书总词数", example = "3000")
    private Integer total;

    /** 已学词数(该词书范围内,用户有学习进度的单词数) */
    @Schema(description = "已学词数(进度表派生)", example = "128")
    private Integer learned;
}