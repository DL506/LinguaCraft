package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目详情响应(对应前端 PracticeItem;不含答案与解析)。
 *
 * <p>content 为原样透传的 JSON(ForAnswer 结构,来源于 question.content 列,
 * 该列在存储层即不含答案,从源头保证"答案不下发")。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeItemResponse {

    /** 题目 ID */
    @Schema(description = "题目 ID", example = "reading-real-2023-1")
    private String id;

    /** 题型 */
    @Schema(description = "题型", example = "reading")
    private String type;

    /** 来源信息 */
    @Schema(description = "来源信息")
    private SourceDto source;

    /** 难度 1-5 */
    @Schema(description = "难度 1-5", example = "2")
    private Integer difficulty;

    /** 标签数组 */
    @Schema(description = "标签数组")
    private List<String> tags;

    /** 下发内容(ForAnswer JSON,按题型不同结构) */
    @Schema(description = "下发内容(ForAnswer JSON,按题型不同结构)")
    private JsonNode content;
}