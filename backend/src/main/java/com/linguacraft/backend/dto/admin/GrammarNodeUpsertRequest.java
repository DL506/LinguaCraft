package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语法节点新建/更新请求(阶段 A6-②)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "语法节点新建/更新请求")
public class GrammarNodeUpsertRequest {

    /** 节点名称 */
    @NotBlank(message = "节点名称不能为空")
    @Schema(description = "节点名称", example = "动词")
    private String name;

    /** 父节点 ID(空表示顶层) */
    @Schema(description = "父节点 ID(空为顶层)")
    private String parentId;

    /** 排序值(小在前) */
    @Schema(description = "排序值")
    private Integer sortOrder;
}
