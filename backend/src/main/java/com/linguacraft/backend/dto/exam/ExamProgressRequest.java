package com.linguacraft.backend.dto.exam;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模考作答进度保存请求(扩展契约 E2:PUT /api/exam/{id}/progress)。
 */
@Data
public class ExamProgressRequest {

    /** 客观题作答进度(全局编号 -> 答案) */
    @Schema(description = "客观题作答进度(全局编号 -> 答案)")
    private JsonNode objective;

    /** 作文作答进度 {content} */
    @Schema(description = "作文作答进度 {content}(可空)")
    private JsonNode writing;

    /** 已答题耗时(秒) */
    @Schema(description = "已答题耗时(秒)", example = "1200")
    private Integer duration;
}