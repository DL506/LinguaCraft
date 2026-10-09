package com.linguacraft.backend.dto.home;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 今日任务响应(GET /api/home/today-tasks,对应前端 TodayTasks)。
 *
 * <p>固定四项,顺序与前端展示一致:reading、cloze、grammar-fill、vocab。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "今日任务响应")
public class TodayTasksResponse {

    /** 任务项列表(固定 4 项) */
    @Schema(description = "任务项列表:reading/cloze/grammar-fill/vocab")
    private List<TodayTaskItemDto> items;
}
