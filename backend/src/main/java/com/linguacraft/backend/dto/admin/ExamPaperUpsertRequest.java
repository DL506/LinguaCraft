package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

/**
 * 真题卷新建/更新请求(阶段 A6-③)。
 *
 * <p>{@code content} 与 {@code answerKey} 是整卷的两份 JSON,题号必须同一套全局编号 ——
 * 落库前由 {@code ExamPaperValidator} 硬校验,不一致会静默判错。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "真题卷新建/更新请求")
public class ExamPaperUpsertRequest {

    /** 试卷 ID(新建可空,服务端生成 exam-<年份>-<时间戳>) */
    @Schema(description = "试卷 ID(新建可空)")
    private String id;

    /** 年份 */
    @NotNull(message = "年份不能为空")
    @Schema(description = "年份", example = "2026")
    private Integer year;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    /** 考试时长(分钟) */
    @Schema(description = "考试时长(分钟)", example = "120")
    private Integer duration;

    /** 卷面总分 */
    @NotNull(message = "卷面总分不能为空")
    @Schema(description = "卷面总分", example = "100.0")
    private BigDecimal totalScore;

    /** 上下架:1 上架,0 下架(新建默认 1) */
    @Schema(description = "上下架:1 上架,0 下架")
    private Integer status;

    /** 卷面 JSON({sections:[{part,type,score,content}]}) */
    @NotNull(message = "content 不能为空")
    @Schema(description = "卷面 JSON")
    private JsonNode content;

    /** 答案 JSON({sections:[{part,type,score,items|sampleAnswer}]}) */
    @NotNull(message = "answer_key 不能为空")
    @Schema(description = "答案 JSON")
    private JsonNode answerKey;
}
