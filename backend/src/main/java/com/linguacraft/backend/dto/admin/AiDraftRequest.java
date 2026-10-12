package com.linguacraft.backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 生成草稿请求(阶段 A9,POST /api/admin/questions/ai-draft)。
 *
 * <p>刻意只接 {@code type}:后台草稿路径不传知识点、不选难度(与 A3 决定的"无参数面板"口径一致),
 * 也<b>不接受</b>任何题面内容 —— 内容一律由模型产出、由校验器把关、由管理员在表单里改。</p>
 */
@Data
@Schema(description = "AI 生成草稿请求")
public class AiDraftRequest {

    /** 题型:reading/matching/cloze/grammar-fill/writing */
    @NotBlank(message = "题型不能为空")
    @Schema(description = "题型:reading/matching/cloze/grammar-fill/writing", requiredMode = Schema.RequiredMode.REQUIRED, example = "cloze")
    private String type;
}
