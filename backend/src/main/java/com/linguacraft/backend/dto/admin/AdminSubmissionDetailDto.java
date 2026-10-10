package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端批改详情(阶段 A4):判分页一次取齐所需的全部材料。
 *
 * <p>包含学生作答原文、题目下发内容、题库参考答案(可能为空,空即"无答案题转人工的原因")、
 * 以及已有的 AI/自动判分结果(供人工复核参考)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端批改详情(含参考答案与既有判分结果)")
public class AdminSubmissionDetailDto {

    /** 提交 ID */
    @Schema(description = "提交 ID")
    private Long id;

    /** 提交用户 ID */
    @Schema(description = "提交用户 ID")
    private Long userId;

    /** 提交用户昵称 */
    @Schema(description = "提交用户昵称")
    private String nickname;

    /** 题型 */
    @Schema(description = "题型", example = "cloze")
    private String type;

    /** 标题快照 */
    @Schema(description = "标题快照")
    private String title;

    /** 题目 ID */
    @Schema(description = "题目 ID")
    private String questionId;

    /** 批改状态 */
    @Schema(description = "批改状态:graded/partially_graded/pending")
    private String status;

    /** 批改方式 */
    @Schema(description = "批改方式:ai/auto/manual")
    private String gradingMethod;

    /** 当前得分 */
    @Schema(description = "当前得分(未批改为空)")
    private BigDecimal score;

    /** 总分快照 */
    @Schema(description = "总分快照")
    private BigDecimal totalScore;

    /** 学生作答原文 JSON(客观题 {题号:作答};写作 {content:"..."}) */
    @Schema(description = "学生作答原文 JSON")
    private JsonNode answers;

    /** 题目下发内容(文章/题干/选项或写作题目) */
    @Schema(description = "题目下发内容 content")
    private JsonNode questionContent;

    /** 题库参考答案与解析(为空即无答案题,须完全依赖人工判定) */
    @Schema(description = "题库参考答案 answer_key(可空)")
    private JsonNode answerKey;

    /** 题库是否具备可比对答案 */
    @Schema(description = "题库是否具备可比对答案")
    private Boolean questionHasAnswer;

    /** 既有逐题判分明细(AI/自动结果,供复核) */
    @Schema(description = "既有逐题判分明细(可空)")
    private JsonNode details;

    /** 既有作文批改结果 */
    @Schema(description = "既有作文批改结果(可空)")
    private JsonNode writingResult;

    /** 预计出结果说明(待批改时非空;人工批改落库后由服务端置空) */
    @Schema(description = "预计出结果说明")
    private String estimatedTime;

    /** 作答耗时(秒) */
    @Schema(description = "作答耗时(秒)")
    private Integer duration;

    /** 等待时长(分钟) */
    @Schema(description = "等待时长(分钟,自提交起算)")
    private Long waitingMinutes;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    /** 批改完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "批改完成时间(可空)")
    private LocalDateTime gradedAt;
}
