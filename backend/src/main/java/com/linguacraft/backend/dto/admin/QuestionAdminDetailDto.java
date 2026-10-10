package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端题目详情(GET /api/admin/questions/{id})。
 *
 * <p>与 student 端详情的关键差异:<b>本响应包含 {@code answerKey}</b>,因此该接口位于
 * {@code /api/admin/**} 命名空间下,由 Spring Security 强制管理员角色;
 * 学生端详情接口永远只读 {@code content} 列,答案不下发。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端题目详情(含答案与解析)")
public class QuestionAdminDetailDto {

    /** 题目 ID */
    @Schema(description = "题目 ID", example = "cloze-real-2023-1")
    private String id;

    /** 题型 */
    @Schema(description = "题型", example = "cloze")
    private String type;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 来源类型:real/teacher/ai */
    @Schema(description = "来源类型:real/teacher/ai", example = "real")
    private String sourceType;

    /** 来源附带信息(年份/地区/老师名/模型等,结构同前端 source.meta) */
    @Schema(description = "来源附带信息(meta 对象)")
    private JsonNode sourceMeta;

    /** 难度 1~5 */
    @Schema(description = "难度 1~5", example = "3")
    private Integer difficulty;

    /** 标签 */
    @Schema(description = "标签数组")
    private List<String> tags;

    /** 上架状态:1 上架,0 下架 */
    @Schema(description = "上架状态:1 上架,0 下架", example = "1")
    private Integer status;

    /** 是否具备可比对答案 */
    @Schema(description = "是否具备可比对答案")
    private Boolean hasAnswer;

    /** 练习场景总分 */
    @Schema(description = "练习场景总分", example = "20.0")
    private BigDecimal totalScore;

    /** 单题分值 */
    @Schema(description = "单题分值", example = "2.0")
    private BigDecimal perQuestionScore;

    /** 下发内容(不含答案) */
    @Schema(description = "下发内容 content(ForAnswer 结构,不含答案)")
    private JsonNode content;

    /** 答案与解析(仅管理端可见) */
    @Schema(description = "答案与解析 answer_key(仅管理端下发)")
    private JsonNode answerKey;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "创建时间(ISO 本地日期时间)")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "更新时间(ISO 本地日期时间)")
    private LocalDateTime updatedAt;
}
