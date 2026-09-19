package com.linguacraft.backend.dto.practice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 出题请求(POST /api/practice/generate)。
 */
@Data
public class GenerateRequest {

    /** 题型(必填,五种题型之一) */
    @Schema(description = "题型", example = "reading")
    private String type;
}