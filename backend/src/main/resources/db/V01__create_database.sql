-- ============================================================
-- V01__create_database.sql
-- 目的  :创建项目数据库 linguacraft(不存在时创建)
-- 前置  :本地 MySQL 8.4.7 已启动
-- 幂等  :是,可重复执行
-- 执行  :PowerShell 必须使用长选项(短选项 -h 会被错误解析):
--   Get-Content .\backend\src\main\resources\db\V01__create_database.sql -Raw |
--     mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4
-- 注意  :建库时库尚不存在,执行本脚本不要附加 --database 参数
-- ============================================================

CREATE DATABASE IF NOT EXISTS `linguacraft`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;