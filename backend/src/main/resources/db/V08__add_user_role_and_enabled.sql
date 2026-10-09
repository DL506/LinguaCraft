-- ============================================================
-- V08__add_user_role_and_enabled.sql
-- 目的  :为 user 表增加角色与启用状态列(后台管理端权限基座)
--         role:student/admin;enabled:1 启用 0 停用(停用账号禁止登录)
-- 前置  :V02__create_user_auth_tables.sql 已执行
-- 幂等  :是 —— MySQL 8 没有 ADD COLUMN IF NOT EXISTS,这里先查 information_schema 判重,
--         再用 PREPARE/EXECUTE 动态执行拼好的语句;重复执行不报错也不会改动已有列
-- 执行  :Start-Process mysql.exe -ArgumentList ... -RedirectStandardInput 本文件(命令见 db/README.md)
-- 说明  :只增不改 —— 两列均带默认值,现有账号自动回填为 student / enabled=1,不影响任何既有接口
-- ============================================================

-- 1) role 列
SET @sql_v08 := IF(
      EXISTS(SELECT 1 FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role'),
      'SELECT ''role 列已存在,跳过'' AS msg',
      'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(10) NOT NULL DEFAULT ''student'' COMMENT ''角色:student 学生/admin 管理员'' AFTER `email`'
);
PREPARE stmt_v08 FROM @sql_v08;
EXECUTE stmt_v08;
DEALLOCATE PREPARE stmt_v08;

-- 2) enabled 列
SET @sql_v08 := IF(
      EXISTS(SELECT 1 FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'enabled'),
      'SELECT ''enabled 列已存在,跳过'' AS msg',
      'ALTER TABLE `user` ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''启用状态:1 启用,0 停用(停用后禁止登录)'' AFTER `role`'
);
PREPARE stmt_v08 FROM @sql_v08;
EXECUTE stmt_v08;
DEALLOCATE PREPARE stmt_v08;

-- 3) 角色索引(管理端用户列表按角色筛选)
SET @sql_v08 := IF(
      EXISTS(SELECT 1 FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND INDEX_NAME = 'idx_user_role'),
      'SELECT ''idx_user_role 已存在,跳过'' AS msg',
      'ALTER TABLE `user` ADD KEY `idx_user_role` (`role`)'
);
PREPARE stmt_v08 FROM @sql_v08;
EXECUTE stmt_v08;
DEALLOCATE PREPARE stmt_v08;

-- 4) 执行后核对:应看到 role 与 enabled 两列(若已存在则上面输出"跳过")
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME IN ('role', 'enabled')
ORDER BY ORDINAL_POSITION;
