package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题实体:对应表 mistake(批改完成后自动收录的错误明细,追加式)。
 */
@Data
@TableName("mistake")
public class Mistake {

    /** 主键(自增;对外经 Jackson 序列化为字符串) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 来源题目 ID(错题重做跳转 /practice/xxx?id= 用) */
    private String questionId;

    /** 题型 */
    private String type;

    /** 来源类型快照 */
    private String sourceType;

    /** 来源信息快照 JSON */
    private String sourceMeta;

    /** 题干摘要(阅读取题干;其余为"标题 第 N 空") */
    private String stem;

    /** 用户作答 */
    private String userAnswer;

    /** 正确答案 */
    private String correctAnswer;

    /** 解析 */
    private String explanation;

    /** 收录时间 */
    private LocalDateTime createdAt;
}