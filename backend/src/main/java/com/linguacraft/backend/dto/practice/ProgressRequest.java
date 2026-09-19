package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 作答进度保存请求(扩展契约 E1:PUT /api/practice/detail/{type}/{id}/progress)。
 */
@Data
public class ProgressRequest {

    /** 作答进度(结构同提交 answers) */
    @Schema(description = "作答进度(结构同提交 answers)")
    private JsonNode answers;

    /** 已作答耗时(秒) */
    @Schema(description = "已作答耗时(秒)", example = "120")
    private Integer duration;
}