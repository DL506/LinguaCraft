-- ============================================================
-- S07__seed_admin_account.sql
-- 目的  :写入后台管理员账号 admin / 123456(role=admin、enabled=1)与其默认设置
-- 前置  :V02__create_user_auth_tables.sql 与 V08__add_user_role_and_enabled.sql 已执行
-- 幂等  :是 —— 以用户名唯一键做 ON DUPLICATE KEY UPDATE,重复执行会把 admin 重置为初始状态(含密码与角色)
-- 执行  :Start-Process mysql.exe -ArgumentList ... -RedirectStandardInput 本文件(命令见 db/README.md)
-- 说明  :1) 密码密文与 S01 完全一致(BCrypt 加密的 123456),避免重复生成密文;
--         2) **这是本地演示口令,任何对外环境必须立即改密**;
--         3) 管理员不走注册入口(注册链路强制 role=student),只能由本脚本或管理端角色接口产生;
--         4) 不指定主键 id,避免与用户已注册的账号发生主键冲突而误改他人角色
-- ============================================================

INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `enabled`, `level`, `level_name`, `continuous_days`, `total_study_days`)
VALUES ('admin', '$2a$10$MuRJM9NuIkSjV2o36oOTP.oegg8o3RdVbrarl51ncfN1AqGsPVMvm', '管理员', 'admin@linguacraft.dev', 'admin', 1, 1, '青铜学习者', 0, 0) AS new
ON DUPLICATE KEY UPDATE
  `password` = new.`password`,
  `nickname` = new.`nickname`,
  `email`    = new.`email`,
  `role`     = new.`role`,
  `enabled`  = new.`enabled`;

-- 管理员同样需要一行设置(后台不判分,但 /api/user/settings 与个人中心接口共用该表)
INSERT INTO `user_setting` (`user_id`, `daily_word_goal`, `daily_reading_goal`, `accent`, `grammar_fill_case_sensitive`, `grading_method`, `reminder_time`)
SELECT `id`, 50, 1, 'us', 0, 'ai', '08:00' FROM `user` WHERE `username` = 'admin'
ON DUPLICATE KEY UPDATE
  `daily_word_goal`             = VALUES(`daily_word_goal`),
  `daily_reading_goal`          = VALUES(`daily_reading_goal`),
  `accent`                      = VALUES(`accent`),
  `grammar_fill_case_sensitive` = VALUES(`grammar_fill_case_sensitive`),
  `grading_method`              = VALUES(`grading_method`),
  `reminder_time`               = VALUES(`reminder_time`);

-- 执行后核对:demo 应为 student,admin 应为 admin/enabled=1
SELECT `id`, `username`, `role`, `enabled` FROM `user` ORDER BY `id`;
