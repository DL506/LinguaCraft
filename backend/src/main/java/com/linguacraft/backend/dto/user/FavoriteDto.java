package com.linguacraft.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏响应(对应前端 FavoriteItem)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteDto {

    /** 收藏记录 ID(对外为字符串) */
    @Schema(description = "收藏记录 ID(字符串)")
    private Long id;

    /** 类型:word 单词 / sentence 句子 */
    @Schema(description = "类型:word/sentence")
    private String type;

    /** 收藏内容 */
    @Schema(description = "收藏内容")
    private String content;

    /** 来源引用 ID(题目/单词;可空) */
    @Schema(description = "来源引用 ID(可空)")
    private String refId;

    /** 收藏时间(ISO-8601) */
    @Schema(description = "收藏时间(ISO-8601)")
    private LocalDateTime createdAt;
}