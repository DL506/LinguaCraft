package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 真题卷「AI 补解析」结果(阶段 A11):按答题卡编号给出的中文解析建议,<b>服务端一条都不写库</b>。
 *
 * <p>前端据此把建议定位到表单的具体一行,由管理员逐条(或批量"只填空白行")采纳进表单;
 * 落库仍然只有点「校验并保存」那一刻。{@code answer} 不在返回结构里 —— 答案不由模型决定。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "AI 补解析结果(建议清单,未写库)")
public class ExamExplainDto {

    /** 题型(回显) */
    @Schema(description = "题型")
    private String type;

    /** 逐编号解析建议 */
    @Schema(description = "逐编号解析建议(按答题卡编号)")
    private List<Item> items;

    /** 本部分送去处理的答案项数(已排除没有标准答案的) */
    @Schema(description = "送去处理的答案项数")
    private Integer requestedCount;

    /** 因为没有标准答案而跳过的项数(界面提示"先补答案再让它写解析") */
    @Schema(description = "因缺标准答案被跳过的项数")
    private Integer skippedNoAnswer;

    /** 被服务端丢弃的返回条数(编号不属于本部分、解析为空或结构非法) */
    @Schema(description = "被丢弃的条数(编号对不上或解析为空)")
    private Integer droppedCount;

    /** 实际调用模型的批数(每批 ≤5 项) */
    @Schema(description = "调用模型的批数")
    private Integer batchCount;

    /**
     * 单个答题卡编号的建议。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "单个答题卡编号的解析建议")
    public static class Item {

        /** 答题卡编号(与表单行的卡号对应) */
        @Schema(description = "答题卡编号", example = "21")
        private Integer id;

        /** 建议的中文解析(服务端已去空白并截断超长) */
        @Schema(description = "建议的中文解析")
        private String explanation;
    }
}
