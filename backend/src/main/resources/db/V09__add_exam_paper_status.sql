-- ============================================================
-- V09__add_exam_paper_status.sql
-- 目的  :为 exam_paper 增加上下架状态列(阶段 A6-③ 真题卷管理)
--         status:1 上架(学生端模考列表可见)0 下架(仅后台可见,历史提交的成绩报告仍可读)
-- 前置  :V05__create_exam_tables.sql 已执行
-- 幂等  :是 —— MySQL 8 没有 ADD COLUMN IF NOT EXISTS,先查 information_schema 判重,
--         再用 PREPARE/EXECUTE 动态执行;重复执行不报错也不会改动已有列
-- 执行  :见 db/README.md 的 Start-Process 命令;执行前请先 mysqldump 备份并验证可还原
-- 说明  :只增不改 —— 默认 1,既有试卷自动保持上架,不影响任何既有接口;
--         读侧过滤在 ExamService.list / findPaper,写侧入口在 AdminExamPaperService
-- ============================================================

-- 1) status 列
SET @sql_v09 := IF(
      EXISTS(SELECT 1 FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_paper' AND COLUMN_NAME = 'status'),
      'SELECT ''status 列已存在,跳过'' AS msg',
      'ALTER TABLE `exam_paper` ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''上下架:1 上架(学生可见),0 下架'' AFTER `duration`'
);
PREPARE stmt_v09 FROM @sql_v09;
EXECUTE stmt_v09;
DEALLOCATE PREPARE stmt_v09;

-- 2) 状态索引(学生端列表按 status 过滤)
SET @sql_v09 := IF(
      EXISTS(SELECT 1 FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_paper' AND INDEX_NAME = 'idx_exam_status'),
      'SELECT ''idx_exam_status 已存在,跳过'' AS msg',
      'ALTER TABLE `exam_paper` ADD KEY `idx_exam_status` (`status`)'
);
PREPARE stmt_v09 FROM @sql_v09;
EXECUTE stmt_v09;
DEALLOCATE PREPARE stmt_v09;

-- 3) 执行后核对:应看到 status 列(默认 1)
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_paper' AND COLUMN_NAME = 'status';
