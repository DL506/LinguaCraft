package com.linguacraft.backend.dto.grammar;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 语法目录节点响应(对应前端 GrammarNode;children 递归)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrammarNodeDto {

    /** 节点 ID */
    @Schema(description = "节点 ID", example = "tenses")
    private String id;

    /** 考点名称 */
    @Schema(description = "考点名称", example = "动词时态")
    private String name;

    /** 子考点(可空) */
    @Schema(description = "子考点(可空)")
    private List<GrammarNodeDto> children;
}