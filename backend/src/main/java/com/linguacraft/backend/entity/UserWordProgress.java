package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户单词学习进度实体:对应表 user_word_progress。
 *
 * <p>间隔复习口径:unknown +1 天 / fuzzy +2 天 / known +7 天(下一次应复习时间见 nextReviewAt)。</p>
 */
@Data
@TableName("user_word_progress")
public class UserWordProgress {

    /** 主键(自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 单词 ID */
    private String wordId;

    /** 最近一次自评:unknown/fuzzy/known */
    private String rating;

    /** 累计复习次数 */
    private Integer reviewCount;

    /** 首次学习时间(用于今日新学统计) */
    private LocalDateTime firstLearnedAt;

    /** 最近复习时间 */
    private LocalDateTime lastReviewAt;

    /** 下次应复习时间(到期复习查询) */
    private LocalDateTime nextReviewAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}