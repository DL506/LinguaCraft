package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端语法节点行(阶段 A6-②):平铺列表,由前端按 parentId 组装成树。
 *
 * <p>后台用平铺而非嵌套:编辑时要看的是"这一层的顺序"与"这个节点有没有正文",
 * 嵌套树在多 children 时会把编辑控件挤成深缩进。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端语法节点行")
public class AdminGrammarNodeDto {

    /** 节点 ID */
    @Schema(description = "节点 ID", example = "g-verb")
    private String id;

    /** 名称 */
    @Schema(description = "节点名称")
    private String name;

    /** 父节点 ID(空为顶层) */
    @Schema(description = "父节点 ID(空为顶层)")
    private String parentId;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;

    /** 是否有子节点(有子节点即为目录,不能直接删) */
    @Schema(description = "是否有子节点")
    private boolean hasChildren;

    /** 是否已有正文章节(叶节点应有一份 id 相同的章节) */
    @Schema(description = "是否已有正文章节")
    private boolean hasSection;

    /** 章节标题(有正文时) */
    @Schema(description = "章节标题")
    private String sectionTitle;
}
