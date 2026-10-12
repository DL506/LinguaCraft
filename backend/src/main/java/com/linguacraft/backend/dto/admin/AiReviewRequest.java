package com.linguacraft.backend.dto.admin;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 校核请求(阶段 A10,POST /api/admin/questions/ai-review)。
 *
 * <p>刻意收<b>整份表单内容</b>而不是题目 id:校核要能在管理员还没保存时就用(新建与 AI 草稿同理),
 * 也避免"改了表单却校核的是旧库存版本"这种错位。因此本接口<b>不读库</b>。</p>
 *
 * <p>也不跑五道闸门:闸门会拒绝"答案不在候选项内"这类结构缺陷,而那恰恰是我们想让校核暴露的问题之一。</p>
 */
@Data
@Schema(description = "AI 校核请求(提交当前题面与答案,服务端只给建议不写库)")
public class AiReviewRequest {

    /** 题型:reading/matching/cloze/grammar-fill/writing */
    @NotBlank(message = "题型不能为空")
    @Schema(description = "题型", requiredMode = Schema.RequiredMode.REQUIRED, example = "grammar-fill")
    private String type;

    /** 当前题面(表单此刻的内容,可含未通过闸门的问题) */
    @Schema(description = "题面 content", requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonNode content;

    /** 当前答案与解析 */
    @Schema(description = "答案 answer_key", requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonNode answerKey;
}
