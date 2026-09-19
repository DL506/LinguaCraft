-- ============================================================
-- S01__seed_demo_user.sql
-- 目的  :写入演示账号(demo / 123456)与其默认设置(与前端 mock 演示数据一致)
-- 前置  :V02__create_user_auth_tables.sql 已执行
-- 幂等  :是,可重复执行;重复执行会将 demo 账号重置为初始状态(含密码)
-- 执行  :Get-Content .\backend\src\main\resources\db\S01__seed_demo_user.sql -Raw |
--          mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
-- 说明  :密码为 BCrypt 加密的 123456;档案数值(等级/连续天数等)与前端 mock 保持一致;
--         demo 账号固定 id=1,user_setting 与其一对一
-- ============================================================

INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `email`, `level`, `level_name`, `continuous_days`, `total_study_days`)
VALUES (1, 'demo', '$2a$10$MuRJM9NuIkSjV2o36oOTP.oegg8o3RdVbrarl51ncfN1AqGsPVMvm', '小明', 'demo@linguacraft.dev', 5, '青铜学习者', 21, 128) AS new
ON DUPLICATE KEY UPDATE
  `password`         = new.`password`,
  `nickname`         = new.`nickname`,
  `email`            = new.`email`,
  `level`            = new.`level`,
  `level_name`       = new.`level_name`,
  `continuous_days`  = new.`continuous_days`,
  `total_study_days` = new.`total_study_days`;

INSERT INTO `user_setting` (`user_id`, `daily_word_goal`, `daily_reading_goal`, `accent`, `grammar_fill_case_sensitive`, `grading_method`, `reminder_time`)
VALUES (1, 50, 1, 'us', 0, 'ai', '08:00') AS new
ON DUPLICATE KEY UPDATE
  `daily_word_goal`             = new.`daily_word_goal`,
  `daily_reading_goal`          = new.`daily_reading_goal`,
  `accent`                      = new.`accent`,
  `grammar_fill_case_sensitive` = new.`grammar_fill_case_sensitive`,
  `grading_method`              = new.`grading_method`,
  `reminder_time`               = new.`reminder_time`;