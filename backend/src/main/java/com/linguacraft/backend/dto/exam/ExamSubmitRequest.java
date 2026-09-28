package com.linguacraft.backend.dto.exam;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 整卷交卷请求(对应前端 ExamSubmitRequest)。
 */
@Data
public class ExamSubmitRequest {

    /** 客观题作答:key 为答题卡全局编号(1~45),value 为选项 key 或填空文本 */
    @Schema(description = "客观题作答(全局编号 1~45 -> 选项key/填空文本)")
    private JsonNode objective;

    /** 作文作答 {content}(可空) */
    @Schema(description = "作文作答 {content}(可空)")
    private JsonNode writing;

    /** 答题耗时(秒) */
    @Schema(description = "答题耗时(秒)", example = "4200")
    private Integer duration;

    /** 本次提交指定批改方式;缺省时服务端读用户设置生效 */
    @Schema(description = "批改方式:ai/manual;缺省时读用户设置", example = "ai")
    private String gradingMethod;
}