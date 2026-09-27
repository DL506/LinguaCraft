package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模考提交实体:对应表 exam_submission(模考独立于"我的提交",报告由本表 + 试卷答案驱动)。
 */
@Data
@TableName("exam_submission")
public class ExamSubmission {

    /** 主键(自增;对外经 Jackson 序列化为字符串) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 试卷 ID */
    private String examId;

    /** 批改状态:graded/partially_graded/pending */
    private String status;

    /** 批改方式:ai/auto/manual */
    private String gradingMethod;

    /** 客观题得分(0~85) */
    private BigDecimal objectiveScore;

    /** 写作得分(0~15,批改完成后) */
    private BigDecimal writingScore;

    /** 卷面总分快照 */
    private BigDecimal totalScore;

    /** 客观题正确数 */
    private Integer correctCount;

    /** 客观题作答 JSON(全局编号 -> 答案) */
    private String objective;

    /** 作文作答 JSON({content}) */
    private String writing;

    /** 客观题批改明细 JSON(SubmitDetail 数组,全局编号) */
    private String details;

    /** 各部分得分 JSON({part: score}) */
    private String sectionScores;

    /** 作文批改结果 JSON(dimensions/sampleAnswer/overallComment) */
    private String writingResult;

    /** 答题耗时(秒) */
    private Integer duration;

    /** 预计出结果说明(待批改时非空) */
    private String estimatedTime;

    /** 交卷时间 */
    private LocalDateTime submittedAt;

    /** 客观题批改完成时间 */
    private LocalDateTime gradedAt;

    /** 作文批改完成时间 */
    private LocalDateTime writingGradedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}