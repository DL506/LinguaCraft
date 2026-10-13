package com.linguacraft.backend.dto.admin;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 真题卷「AI 补解析」请求(阶段 A11,POST /api/admin/exam-papers/ai-explain)。
 *
 * <p>粒度是<b>一个部分</b>而不是整卷:整卷 45 题要 9 批、每批 15~40 秒(A10 实测),
 * 同步等一次要三五分钟;按部分请求把等待压到 1~3 批。</p>
 *
 * <p>和 A10 一样收<b>表单此刻的内容</b>而不是试卷 id:没保存也能用,也不会出现
 * "改了表单却让模型读库里的旧版本"。本接口不读库也不写库。</p>
 */
@Data
@Schema(description = "真题卷 AI 补解析请求(提交某个部分的卷面与该部分答案项,服务端只给解析不写库)")
public class ExamExplainRequest {

    /** 本部分题型:reading/matching/cloze/grammar-fill(writing 无客观题解析,直接拒绝) */
    @NotBlank(message = "题型不能为空")
    @Schema(description = "本部分题型", requiredMode = Schema.RequiredMode.REQUIRED, example = "cloze")
    private String type;

    /** 本部分卷面(与保存时的 content 同形:阅读是数组,其余是带 blanks 的对象) */
    @Schema(description = "本部分卷面 content", requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonNode content;

    /** 本部分答案项数组(items:[{id(答题卡编号),answer,explanation?}]) */
    @Schema(description = "本部分答案项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonNode items;
}
