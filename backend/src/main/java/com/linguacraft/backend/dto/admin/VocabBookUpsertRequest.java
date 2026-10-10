package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 词书新建/更新请求(阶段 A6)。
 *
 * <p>{@code total} 是**声明值**而非派生值:种子数据里写的是考纲词量(3000/800/1200),
 * 而库里只导入了演示用的少量单词。因此更新时不自动改写它,列表里同时下发真实词数,
 * 需要一致时由管理员显式调 {@code PATCH /api/admin/vocab/books/{id}/sync-total}。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "词书新建/更新请求")
public class VocabBookUpsertRequest {

    /** 词书 ID(新建时可空,由服务端按名称生成) */
    @Schema(description = "词书 ID(新建可空)")
    private String id;

    /** 名称 */
    @NotBlank(message = "词书名称不能为空")
    @Schema(description = "词书名称", example = "专插本核心词汇")
    private String name;

    /** 声明总词数 */
    @Schema(description = "声明总词数(展示用,不等于实际导入量)", example = "3000")
    private Integer total;

    /** 词汇分级过滤:high/core/cognitive,空表示不限 */
    @Schema(description = "过滤等级:high/core/cognitive,空为不限")
    private String filterLevel;

    /** 排序 */
    @Schema(description = "排序值(小在前)")
    private Integer sortOrder;
}
