package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户设置响应(GET /api/user/settings,对应前端 UserSettings)。
 *
 * <p>注意:themeMode 不在本接口(主题偏好仅存前端 localStorage)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {

    /** 每日背单词目标 */
    @Schema(description = "每日背单词目标", example = "50")
    private Integer dailyWordGoal;

    /** 每日阅读目标(篇) */
    @Schema(description = "每日阅读目标(篇)", example = "1")
    private Integer dailyReadingGoal;

    /** 发音口音:us/uk */
    @Schema(description = "发音口音:us/uk", example = "us")
    private String accent;

    /** 语法填空判分是否区分大小写(服务端判分偏好) */
    @Schema(description = "语法填空判分是否区分大小写", example = "false")
    private Boolean grammarFillCaseSensitive;

    /** 批改方式偏好:ai/manual(服务端判分偏好) */
    @Schema(description = "批改方式偏好:ai/manual", example = "ai")
    private String gradingMethod;

    /** 学习提醒时间 HH:mm(可空) */
    @Schema(description = "学习提醒时间 HH:mm", example = "08:00")
    private String reminderTime;
}