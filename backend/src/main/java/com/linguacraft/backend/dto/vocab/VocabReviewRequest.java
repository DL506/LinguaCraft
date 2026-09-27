package com.linguacraft.backend.dto.vocab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单词记忆自评请求(POST /api/vocab/review)。
 */
@Data
public class VocabReviewRequest {

    /** 单词 ID */
    @Schema(description = "单词 ID", example = "w-001")
    private String wordId;

    /** 记忆自评:unknown 不认识 / fuzzy 模糊 / known 认识 */
    @Schema(description = "记忆自评:unknown/fuzzy/known", example = "known")
    private String rating;
}