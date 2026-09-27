package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单词复习日志实体:对应表 user_word_review(按日统计今日完成数/新学数的数据源之一)。
 */
@Data
@TableName("user_word_review")
public class UserWordReview {

    /** 主键(自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 单词 ID */
    private String wordId;

    /** 自评:unknown/fuzzy/known */
    private String rating;

    /** 复习时间 */
    private LocalDateTime reviewedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}