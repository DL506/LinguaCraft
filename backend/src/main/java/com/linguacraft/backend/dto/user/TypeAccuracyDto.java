package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 各题型得分率(对应前端 UserStats.typeAccuracy;注意 grammar 对应语法填空)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeAccuracyDto {

    /** 阅读理解得分率(0-100) */
    @Schema(description = "阅读理解得分率(0-100)", example = "72")
    private Integer reading;

    /** 五选五得分率(0-100) */
    @Schema(description = "五选五得分率(0-100)", example = "80")
    private Integer matching;

    /** 完形填空得分率(0-100) */
    @Schema(description = "完形填空得分率(0-100)", example = "55")
    private Integer cloze;

    /** 语法填空得分率(0-100;映射 grammar-fill) */
    @Schema(description = "语法填空得分率(0-100;映射 grammar-fill)", example = "62")
    private Integer grammar;

    /** 作文得分率(0-100) */
    @Schema(description = "作文得分率(0-100)", example = "78")
    private Integer writing;
}