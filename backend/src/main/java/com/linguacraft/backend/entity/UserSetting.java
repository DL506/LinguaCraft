package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设置实体:对应表 user_setting(判分偏好与学习目标,与 user 一对一)。
 *
 * <p>注意:themeMode 不在本表,主题偏好仅存前端 localStorage(任务书 5.3)。</p>
 */
@Data
@TableName("user_setting")
public class UserSetting {

    /** 用户 ID(主键,与 user.id 一对一,非自增) */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 每日背单词目标 */
    private Integer dailyWordGoal;

    /** 每日阅读目标(篇) */
    private Integer dailyReadingGoal;

    /** 发音口音:us/uk */
    private String accent;

    /** 语法填空判分是否区分大小写(服务端判分偏好) */
    private Boolean grammarFillCaseSensitive;

    /** 批改方式偏好:ai/manual(服务端判分偏好) */
    private String gradingMethod;

    /** 学习提醒时间 HH:mm(可空) */
    private String reminderTime;

    /** 创建时间(数据库默认值维护) */
    private LocalDateTime createdAt;

    /** 更新时间(数据库默认值维护) */
    private LocalDateTime updatedAt;
}