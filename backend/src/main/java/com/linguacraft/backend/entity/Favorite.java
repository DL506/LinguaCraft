package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体:对应表 favorite(支持单词与句子两种类型,对应前端 FavoriteItem)。
 */
@Data
@TableName("favorite")
public class Favorite {

    /** 主键(自增;对外经 Jackson 序列化为字符串) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 类型:word 单词 / sentence 句子 */
    private String type;

    /** 收藏内容(单词拼写或句子原文) */
    private String content;

    /** 来源引用 ID(题目/单词;可空) */
    private String refId;

    /** 收藏时间 */
    private LocalDateTime createdAt;
}