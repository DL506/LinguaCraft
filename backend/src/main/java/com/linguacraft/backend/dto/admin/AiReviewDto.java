package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 校核结果(阶段 A10):对当前题面与答案的建议清单,<b>服务端一条都不写库</b>。
 *
 * <p>每条建议对应 answer_key 里的一个编号,前端据此定位到表单的具体行。
 * 只有 {@code alternatives} 与 {@code explanation} 是"可采纳"的(采纳即写进表单,仍需点保存才落库);
 * {@code answerRisk} 只是提醒 —— 改答案必须由管理员自己动手,因为答案键影响此后所有学生。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "AI 校核结果(建议清单,未写库)")
public class AiReviewDto {

    /** 题型(回显) */
    @Schema(description = "题型")
    private String type;

    /** 整题结论(模型给的一句话,可能为空) */
    @Schema(description = "整题结论(中文)")
    private String summary;

    /** 逐编号建议 */
    @Schema(description = "逐编号建议(按 answer_key 编号顺序)")
    private List<Item> items;

    /** 被服务端丢弃的建议数(编号对不上 answer_key 或结构非法),用于界面如实提示"模型还返回了 N 条无法对应的内容" */
    @Schema(description = "被丢弃的建议条数(编号无法对应或结构非法)")
    private Integer droppedCount;

    /**
     * 单个编号的建议。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "单个编号的校核建议")
    public static class Item {

        /** answer_key 里的题号/空位编号(与表单行对应) */
        @Schema(description = "题号/空位编号", example = "3")
        private Integer id;

        /** 建议补充的可接受写法(仅语法填空非空;服务端已剔除空白项、重复项与标准答案本身) */
        @Schema(description = "建议的可接受写法(非语法填空恒为空数组)")
        private List<String> alternatives;

        /** 标准答案风险:ok / mayBeWrong / notUnique(服务端已归一,未知值按 ok) */
        @Schema(description = "标准答案风险", example = "notUnique")
        private String answerRisk;

        /** 解析风险:ok / mismatch / tooThin */
        @Schema(description = "解析风险", example = "mismatch")
        private String explanationRisk;

        /** 建议改写后的中文解析(空表示不必改写) */
        @Schema(description = "建议解析(无需改写则为空)")
        private String explanation;

        /** 一句话理由(中文) */
        @Schema(description = "建议理由")
        private String note;
    }
}
