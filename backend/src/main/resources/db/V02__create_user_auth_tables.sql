-- ============================================================
-- V02__create_user_auth_tables.sql
-- 目的  :创建认证与用户设置相关表(user / user_setting / password_reset_token)
-- 前置  :V01__create_database.sql 已执行(库 linguacraft 存在)
-- 幂等  :是,可重复执行(IF NOT EXISTS)
-- 执行  :Get-Content .\backend\src\main\resources\db\V02__create_user_auth_tables.sql -Raw |
--          mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
-- ============================================================

-- 用户表:账号基础信息与学习档案(对应前端 AuthUser / UserProfile)
CREATE TABLE IF NOT EXISTS `user` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键(对外经 Jackson 序列化为字符串)',
  `username`         VARCHAR(20)  NOT NULL COMMENT '用户名(3~20 位字母数字,唯一)',
  `password`         VARCHAR(100) NOT NULL COMMENT '密码(BCrypt 密文)',
  `nickname`         VARCHAR(50)  NOT NULL COMMENT '昵称',
  `email`            VARCHAR(100) NOT NULL COMMENT '邮箱(唯一,用于找回密码)',
  `avatar`           VARCHAR(255) NULL     COMMENT '头像地址(可空)',
  `level`            INT          NOT NULL DEFAULT 1 COMMENT '等级数值',
  `level_name`       VARCHAR(50)  NOT NULL DEFAULT '青铜学习者' COMMENT '等级名称',
  `continuous_days`  INT          NOT NULL DEFAULT 0 COMMENT '连续学习天数',
  `total_study_days` INT          NOT NULL DEFAULT 0 COMMENT '累计学习天数',
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 用户设置表:判分偏好与学习目标(对应前端 UserSettings;themeMode 不在此,仅存前端 localStorage)
CREATE TABLE IF NOT EXISTS `user_setting` (
  `user_id`                     BIGINT      NOT NULL COMMENT '用户 ID(主键,与 user.id 一对一)',
  `daily_word_goal`             INT         NOT NULL DEFAULT 50 COMMENT '每日背单词目标',
  `daily_reading_goal`          INT         NOT NULL DEFAULT 1 COMMENT '每日阅读目标(篇)',
  `accent`                      VARCHAR(10) NOT NULL DEFAULT 'us' COMMENT '发音口音:us/uk',
  `grammar_fill_case_sensitive` TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '语法填空判分是否区分大小写(服务端判分偏好)',
  `grading_method`              VARCHAR(10) NOT NULL DEFAULT 'ai' COMMENT '批改方式偏好:ai/manual(服务端判分偏好)',
  `reminder_time`               VARCHAR(10) NULL     DEFAULT '08:00' COMMENT '学习提醒时间 HH:mm',
  `created_at`                  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`                  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户设置表';

-- 密码重置令牌表:找回密码的一次性令牌(30 分钟有效,使用后作废)
CREATE TABLE IF NOT EXISTS `password_reset_token` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT      NOT NULL COMMENT '用户 ID',
  `token`      VARCHAR(64) NOT NULL COMMENT '一次性重置令牌(UUID 去连字符)',
  `expires_at` DATETIME    NOT NULL COMMENT '过期时间(创建后 30 分钟)',
  `used`       TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已使用:0 未使用,1 已使用',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reset_token` (`token`),
  KEY `idx_reset_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密码重置令牌表';