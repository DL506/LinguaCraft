package com.linguacraft.backend.dto.practice;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 练习提交请求(对应前端 PracticeSubmitRequest)。
 *
 * <p>answers 结构按题型:
 * 阅读/完形/五选五 {题号或空位id: 选项key};语法填空 {空位id: 填空文本};写作 {content}。</p>
 */
@Data
public class PracticeSubmitRequest {

    /** 作答内容(原样 JSON,见类注释) */
    @Schema(description = "作答内容:阅读/完形/五选五 {题号或空位id:选项key};语法填空 {空位id:文本};写作 {content}")
    private JsonNode answers;

    /** 作答耗时(秒) */
    @Schema(description = "作答耗时(秒)", example = "420")
    private Integer duration;

    /** 本次提交指定批改方式;缺省时服务端读用户设置生效 */
    @Schema(description = "批改方式:ai/manual;缺省时读用户设置", example = "ai")
    private String gradingMethod;
}