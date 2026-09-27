package com.linguacraft.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linguacraft.backend.dto.practice.SourceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 错题响应(对应前端 MistakeItem)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MistakeDto {

    /** 错题记录 ID(对外为字符串) */
    @Schema(description = "错题记录 ID(字符串)")
    private Long id;

    /** 来源题目 ID(错题重做跳转用) */
    @Schema(description = "来源题目 ID(错题重做跳转用)")
    private String questionId;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 来源信息 */
    @Schema(description = "来源信息")
    private SourceDto source;

    /** 题干摘要 */
    @Schema(description = "题干摘要")
    private String stem;

    /** 用户作答 */
    @Schema(description = "用户作答")
    private String userAnswer;

    /** 正确答案 */
    @Schema(description = "正确答案")
    private String correctAnswer;

    /** 解析 */
    @Schema(description = "解析")
    private String explanation;

    /** 收录时间(ISO-8601) */
    @Schema(description = "收录时间(ISO-8601)")
    private LocalDateTime createdAt;
}