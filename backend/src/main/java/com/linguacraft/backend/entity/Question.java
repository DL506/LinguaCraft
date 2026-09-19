package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题库实体:对应表 question(五种题型的练习题目)。
 *
 * <p>双 JSON 列设计:</p>
 * <ul>
 *   <li>{@code content}:下发用内容(ForAnswer,不含答案/解析),接口链路只读取本列;</li>
 *   <li>{@code answerKey}:答案与解析(判分时读取,永不随 GET 接口下发)。</li>
 * </ul>
 * <p>JSON 列以字符串存取,应用层使用 ObjectMapper 解析(避免 MyBatis 类型处理器配置面)。</p>
 */
@Data
@TableName("question")
public class Question {

    /** 题目 ID(语义化,如 reading-real-2023-1;非自增) */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 题型:reading/matching/cloze/grammar-fill/writing */
    private String type;

    /** 来源类型:real/teacher/ai */
    private String sourceType;

    /** 来源信息 JSON(完整 source 对象:{type,meta:{...}}) */
    private String sourceMeta;

    /** 标题(列表展示) */
    private String title;

    /** 难度 1-5 */
    private Integer difficulty;

    /** 标签数组 JSON(string[]) */
    private String tags;

    /** 下发内容 JSON(单引号字段名保持驼峰,与前端契约一致) */
    private String content;

    /** 答案与解析 JSON(reading:questions[];matching/cloze/grammar-fill:blanks[];writing:sampleAnswer) */
    private String answerKey;

    /** 是否具备可比对答案:true 可数据库比对,false 转 AI 批改或人工 */
    private Boolean hasAnswer;

    /** 练习场景总分 */
    private BigDecimal totalScore;

    /** 练习场景单题分值 */
    private BigDecimal perQuestionScore;

    /** 状态:1 启用,0 停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}