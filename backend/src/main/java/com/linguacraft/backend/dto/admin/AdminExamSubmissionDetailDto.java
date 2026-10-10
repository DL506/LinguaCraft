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
 * 管理端模考批改详情(阶段 A4-②):判作文前需要的全部材料。
 *
 * <p>包含学生作文正文、试卷写作部分的题目与评分维度、客观题各部分得分、
 * 以及既有 AI 批改结果(若有,供人工复核)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端模考批改详情")
public class AdminExamSubmissionDetailDto {

    /** 提交 ID */
    @Schema(description = "提交 ID")
    private Long id;

    /** 用户 ID */
    @Schema(description = "提交用户 ID")
    private Long userId;

    /** 用户昵称 */
    @Schema(description = "提交用户昵称")
    private String nickname;

    /** 试卷 ID */
    @Schema(description = "试卷 ID")
    private String examId;

    /** 试卷标题 */
    @Schema(description = "试卷标题")
    private String paperTitle;

    /** 试卷年份 */
    @Schema(description = "试卷年份")
    private Integer year;

    /** 状态 */
    @Schema(description = "状态:graded/partially_graded/pending")
    private String status;

    /** 批改方式 */
    @Schema(description = "批改方式:ai/auto/manual")
    private String gradingMethod;

    /** 客观题得分 */
    @Schema(description = "客观题得分")
    private BigDecimal objectiveScore;

    /** 写作得分(已批分时非空) */
    @Schema(description = "写作得分")
    private BigDecimal writingScore;

    /** 卷面总分快照 */
    @Schema(description = "卷面总分快照")
    private BigDecimal totalScore;

    /** 写作部分满分(取自试卷写作 section 的 score) */
    @Schema(description = "写作部分满分", example = "15")
    private BigDecimal writingFullScore;

    /** 学生作文正文 */
    @Schema(description = "学生作文正文")
    private String writingContent;

    /** 试卷写作部分内容(prompt/requirements/scoringDimensions) */
    @Schema(description = "试卷写作部分 content")
    private JsonNode writingSection;

    /** 客观题各部分得分 JSON({part: score}) */
    @Schema(description = "客观题各部分得分")
    private JsonNode sectionScores;

    /** 既有作文批改结果(AI 或人工) */
    @Schema(description = "既有作文批改结果(可空)")
    private JsonNode writingResult;

    /** 作文是否已出分 */
    @Schema(description = "作文是否已出分")
    private Boolean writingGraded;

    /** 待批改说明 */
    @Schema(description = "预计出结果说明(人工判分后由服务端置空)")
    private String estimatedTime;

    /** 答题耗时(秒) */
    @Schema(description = "答题耗时(秒)")
    private Integer duration;

    /** 等待时长(分钟) */
    @Schema(description = "等待时长(分钟)")
    private Long waitingMinutes;

    /** 交卷时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "交卷时间")
    private LocalDateTime submittedAt;

    /** 作文出分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "作文出分时间(可空)")
    private LocalDateTime writingGradedAt;
}
