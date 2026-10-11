package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 真题试卷实体:对应表 exam_paper。
 *
 * <p>content 为整卷下发内容(sections 结构;阅读 section 的 content 为 3 篇数组);
 * answer_key 按答题卡"全局编号"存放客观题答案(阅读 1-15/五选五 16-20/完形 21-35/语法 36-45),
 * 写作 section 存范文。两列均以 JSON 字符串存取,应用层用 ObjectMapper 解析。</p>
 */
@Data
@TableName("exam_paper")
public class ExamPaper {

    /** 试卷 ID(如 exam-2026;非自增) */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 年份 */
    private Integer year;

    /** 试卷标题 */
    private String title;

    /** 卷面总分 */
    private BigDecimal totalScore;

    /** 考试时长(分钟) */
    private Integer duration;

    /** 上下架:1 上架(学生端可见),0 下架(V09 新增;历史提交的成绩报告不受影响) */
    private Integer status;

    /** 整卷下发内容 JSON(不含答案) */
    private String content;

    /** 整卷答案 JSON(判分时读取,永不随 GET 接口下发) */
    private String answerKey;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}