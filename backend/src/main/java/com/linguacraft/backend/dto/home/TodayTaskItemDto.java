package com.linguacraft.backend.dto.home;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日任务单项响应(对应前端 TodayTaskItem,任务书 11.2)。
 *
 * <p>type 取值:reading/cloze/grammar-fill/vocab;target 为目标数,done 为今日已完成数。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "今日任务单项")
public class TodayTaskItemDto {

    /** 任务类型:reading/cloze/grammar-fill/vocab */
    @Schema(description = "任务类型", example = "reading")
    private String type;

    /** 目标数(阅读取用户设置;完形与语法填空固定 1;单词取用户设置) */
    @Schema(description = "目标数", example = "1")
    private Integer target;

    /** 今日已完成数(练习按提交计数;单词按今日复习记录计数) */
    @Schema(description = "今日已完成数", example = "0")
    private Integer done;
}
