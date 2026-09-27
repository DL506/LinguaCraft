package com.linguacraft.backend.dto.vocab;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日背单词任务响应(对应前端 VocabTodayTask)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabTodayTaskDto {

    /** 今日新学词数(今日首次接触的单词数) */
    @Schema(description = "今日新学词数", example = "8")
    private Integer newCount;

    /** 待复习词数(到期应复习) */
    @Schema(description = "待复习词数", example = "36")
    private Integer reviewCount;

    /** 今日已完成数(今日复习提交数) */
    @Schema(description = "今日已完成数", example = "12")
    private Integer doneCount;

    /** 今日目标(读用户设置 dailyWordGoal) */
    @Schema(description = "今日目标(用户设置)", example = "50")
    private Integer target;
}