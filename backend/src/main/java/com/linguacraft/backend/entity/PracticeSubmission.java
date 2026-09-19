package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 练习提交实体:对应表 practice_submission(单题练习的提交记录)。
 *
 * <p>JSON 列均以字符串存取,应用层解析:</p>
 * <ul>
 *   <li>{@code answers}:用户作答原文(客观题 {题号:答案};写作 {content});</li>
 *   <li>{@code details}:逐题批改明细(仅批改完成后下发);</li>
 *   <li>{@code writingResult}:写作批改结果(dimensions/sampleAnswer/overallComment)。</li>
 * </ul>
 */
@Data
@TableName("practice_submission")
public class PracticeSubmission {

    /** 主键(自增;对外经 Jackson 序列化为字符串) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 题目 ID */
    private String questionId;

    /** 题型 */
    private String type;

    /** 标题快照 */
    private String title;

    /** 来源类型快照 */
    private String sourceType;

    /** 来源信息快照 JSON */
    private String sourceMeta;

    /** 批改状态:graded/partially_graded/pending */
    private String status;

    /** 批改方式:ai/auto/manual */
    private String gradingMethod;

    /** 得分(未批改时为空) */
    private BigDecimal score;

    /** 总分(快照) */
    private BigDecimal totalScore;

    /** 正确数(未批改时为空) */
    private Integer correctCount;

    /** 总题数 */
    private Integer totalCount;

    /** 用户作答原文 JSON */
    private String answers;

    /** 逐题批改明细 JSON(SubmitDetail 数组;仅批改完成后下发) */
    private String details;

    /** 写作批改结果 JSON */
    private String writingResult;

    /** 作答耗时(秒) */
    private Integer duration;

    /** 预计出结果说明(待批改时非空) */
    private String estimatedTime;

    /** 提交时间 */
    private LocalDateTime submittedAt;

    /** 批改完成时间 */
    private LocalDateTime gradedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}