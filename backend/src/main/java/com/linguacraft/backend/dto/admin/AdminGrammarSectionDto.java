package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端正文章节(阶段 A6-②):含 markdown 原文,供后台编辑。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端语法章节")
public class AdminGrammarSectionDto {

    /** 章节 ID(等于其叶节点 ID) */
    @Schema(description = "章节 ID(等于叶节点 ID)")
    private String id;

    /** 所属节点 ID */
    @Schema(description = "所属节点 ID")
    private String nodeId;

    /** 标题 */
    @Schema(description = "章节标题")
    private String title;

    /** 正文 markdown */
    @Schema(description = "正文 markdown")
    private String markdown;

    /** 排序值 */
    @Schema(description = "排序值")
    private Integer sortOrder;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
