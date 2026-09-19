package com.linguacraft.backend.dto.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目来源信息(对应前端 QuestionSource:{type, meta})。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceDto {

    /** 来源类型:real/teacher/ai */
    @Schema(description = "来源类型:real/teacher/ai", example = "real")
    private String type;

    /** 来源附带信息(year/region/teacherName/school/model/generatedAt,原样透传 JSON) */
    @Schema(description = "来源附带信息(year/region/teacherName/school/model/generatedAt)")
    private JsonNode meta;
}