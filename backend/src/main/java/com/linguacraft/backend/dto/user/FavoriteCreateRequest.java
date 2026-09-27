package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新增收藏请求(POST /api/user/favorites)。
 */
@Data
public class FavoriteCreateRequest {

    /** 类型:word 单词 / sentence 句子 */
    @Schema(description = "类型:word/sentence", example = "sentence")
    private String type;

    /** 收藏内容(单词拼写或句子原文) */
    @Schema(description = "收藏内容(单词拼写或句子原文)")
    private String content;

    /** 来源引用 ID(题目/单词;可空) */
    @Schema(description = "来源引用 ID(可空)")
    private String refId;
}