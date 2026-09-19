package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户设置更新请求(PUT /api/user/settings)。
 *
 * <p>部分更新语义:未提供的字段(为 null)保持不变(与前端契约一致)。</p>
 */
@Data
public class UserSettingsUpdateRequest {

    /** 每日背单词目标(可不传) */
    @Schema(description = "每日背单词目标(可不传)")
    private Integer dailyWordGoal;

    /** 每日阅读目标(篇)(可不传) */
    @Schema(description = "每日阅读目标(篇)(可不传)")
    private Integer dailyReadingGoal;

    /** 发音口音:us/uk(可不传) */
    @Schema(description = "发音口音:us/uk(可不传)")
    private String accent;

    /** 语法填空判分是否区分大小写(可不传) */
    @Schema(description = "语法填空判分是否区分大小写(可不传)")
    private Boolean grammarFillCaseSensitive;

    /** 批改方式偏好:ai/manual(可不传) */
    @Schema(description = "批改方式偏好:ai/manual(可不传)")
    private String gradingMethod;

    /** 学习提醒时间 HH:mm(可不传) */
    @Schema(description = "学习提醒时间 HH:mm(可不传)")
    private String reminderTime;
}