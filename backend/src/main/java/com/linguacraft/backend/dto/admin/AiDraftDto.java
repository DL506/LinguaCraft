package com.linguacraft.backend.dto.admin;

import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 题目草稿(阶段 A9)。
 *
 * <p>由后台「AI 生成一版」返回,<b>不入库</b>:管理员在录入表单里改完并点保存,才走
 * {@code POST /api/admin/questions} 落库、才上架。这样 AI 产出永远先经过人的眼睛,
 * 影响范围(此后所有学生看到的题)与人工录题完全一致。</p>
 *
 * <p>{@code content} 与 {@code answerKey} 已经过 {@code QuestionContentValidator} 五道校验,
 * 因此可放心直接填进表单;但 <b>不含 {@code content.id/type/source} 三个权威字段</b> ——
 * 它们由服务端在落库时按最终题型与来源覆写,草稿阶段编一个 id 反而会误导管理员。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "AI 题目草稿(已过闸门校验,未入库)")
public class AiDraftDto {

    /** 题型(与请求一致,便于前端确认灌进了哪套表单) */
    @Schema(description = "题型", example = "cloze")
    private String type;

    /** AI 给的标题(可空,空则录入页保留自己的标题) */
    @Schema(description = "标题(可空)")
    private String title;

    /** 难度 1~5(已夹到合法区间) */
    @Schema(description = "难度 1~5", example = "3")
    private Integer difficulty;

    /** 标签 */
    @Schema(description = "标签列表")
    private List<String> tags;

    /** 下发内容(不含答案,不含 id/type/source) */
    @Schema(description = "content 草稿(已过五道校验,不含权威字段)")
    private JsonNode content;

    /** 答案与解析 */
    @Schema(description = "answer_key 草稿")
    private JsonNode answerKey;

    /** 来源 meta 子对象({@code model} 与 {@code generatedAt}),形状与题目详情的 sourceMeta 一致,保存时可原样带回 */
    @Schema(description = "来源 meta 子对象(模型名与生成时刻;不含外层 type)",
            example = "{\"model\":\"deepseek-flash\",\"generatedAt\":\"2026-10-12T09:00:00\"}")
    private JsonNode sourceMeta;
}
