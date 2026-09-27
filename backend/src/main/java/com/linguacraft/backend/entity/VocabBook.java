package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 词书实体:对应表 vocab_book(演示 3 本;learned 数由进度表实时派生,不落库)。
 */
@Data
@TableName("vocab_book")
public class VocabBook {

    /** 词书 ID(如 book-syllabus;非自增) */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 词书名称 */
    private String name;

    /** 词书总词数(展示口径) */
    private Integer total;

    /** 分层过滤:null=全部,high/core/cognitive */
    private String filterLevel;

    /** 展示排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}