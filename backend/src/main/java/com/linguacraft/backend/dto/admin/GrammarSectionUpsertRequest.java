package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语法章节新建/更新请求(阶段 A6-②)。
 *
 * <p>章节 ID 与叶节点 ID 相同(种子不变式),所以这里只给内容,不给 ID。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "语法章节新建/更新请求")
public class GrammarSectionUpsertRequest {

    /** 标题 */
    @NotBlank(message = "章节标题不能为空")
    @Schema(description = "章节标题")
    private String title;

    /** 正文(markdown,学生端按 markdown 渲染) */
    @NotBlank(message = "章节正文不能为空")
    @Schema(description = "章节正文 markdown")
    private String markdown;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;
}
