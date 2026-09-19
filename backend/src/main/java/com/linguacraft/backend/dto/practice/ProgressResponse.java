package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作答进度响应(扩展契约 E1;无进度时接口返回 data=null)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressResponse {

    /** 作答进度(结构同提交 answers) */
    @Schema(description = "作答进度(结构同提交 answers)")
    private JsonNode answers;

    /** 已作答耗时(秒) */
    @Schema(description = "已作答耗时(秒)")
    private Integer duration;
}