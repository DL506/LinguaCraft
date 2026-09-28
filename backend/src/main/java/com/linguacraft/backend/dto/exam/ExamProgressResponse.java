package com.linguacraft.backend.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模考作答进度响应(扩展契约 E2;无进度时接口返回 data=null)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamProgressResponse {

    /** 客观题作答进度(全局编号 -> 答案) */
    @Schema(description = "客观题作答进度(全局编号 -> 答案)")
    private JsonNode objective;

    /** 作文作答进度 {content} */
    @Schema(description = "作文作答进度 {content}")
    private JsonNode writing;

    /** 已答题耗时(秒) */
    @Schema(description = "已答题耗时(秒)")
    private Integer duration;
}