-- ============================================================
-- V05__create_exam_tables.sql
-- 目的  :创建真题模考相关表(exam_paper / exam_submission / exam_progress)
-- 前置  :V01__create_database.sql 已执行
-- 幂等  :是,可重复执行(IF NOT EXISTS)
-- 执行  :Get-Content .\backend\src\main\resources\db\V05__create_exam_tables.sql -Raw |
--          mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
-- 说明  :content 为整卷下发内容(阅读为 3 篇数组);answer_key 按答题卡"全局编号"存放客观题答案,
--         写作 section 存范文;模考提交独立于"我的提交"(practice_submission),报告由本表驱动。
-- ============================================================

-- 真题试卷表(对应前端 ExamPaper)
CREATE TABLE IF NOT EXISTS `exam_paper` (
  `id`          VARCHAR(64)  NOT NULL COMMENT '试卷 ID(如 exam-2026)',
  `year`        INT          NOT NULL COMMENT '年份',
  `title`       VARCHAR(200) NOT NULL COMMENT '试卷标题',
  `total_score` DECIMAL(4,1) NOT NULL DEFAULT 100.0 COMMENT '卷面总分',
  `duration`    INT          NOT NULL DEFAULT 120 COMMENT '考试时长(分钟)',
  `content`     JSON         NOT NULL COMMENT '整卷下发内容:{sections:[{part,type,score,content}]}(阅读 content 为 3 篇数组)',
  `answer_key`  JSON         NOT NULL COMMENT '整卷答案:{sections:[{part,type,score,items:[{id(全局编号),answer,explanation,acceptAlternatives?}]} | 写作 section 含 sampleAnswer]}',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='真题试卷表';

-- 模考提交表(对应交卷响应与成绩报告数据源)
CREATE TABLE IF NOT EXISTS `exam_submission` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键(对外序列化为字符串)',
  `user_id`           BIGINT       NOT NULL COMMENT '用户 ID',
  `exam_id`           VARCHAR(64)  NOT NULL COMMENT '试卷 ID',
  `status`            VARCHAR(20)  NOT NULL COMMENT '批改状态:graded/partially_graded/pending',
  `grading_method`    VARCHAR(10)  NOT NULL COMMENT '批改方式:ai/auto/manual',
  `objective_score`   DECIMAL(5,1) NULL     COMMENT '客观题得分(0~85)',
  `writing_score`     DECIMAL(5,1) NULL     COMMENT '写作得分(0~15,批改完成后)',
  `total_score`       DECIMAL(5,1) NOT NULL COMMENT '卷面总分快照',
  `correct_count`     INT          NULL     COMMENT '客观题正确数',
  `objective`         JSON         NOT NULL COMMENT '客观题作答(全局编号 -> 答案)',
  `writing`           JSON         NULL     COMMENT '作文作答 {content}',
  `details`           JSON         NULL     COMMENT '客观题批改明细(全局编号,含答案与解析)',
  `section_scores`    JSON         NULL     COMMENT '各部分得分 {part: score}',
  `writing_result`    JSON         NULL     COMMENT '作文批改结果(dimensions/sampleAnswer/overallComment)',
  `duration`          INT          NOT NULL DEFAULT 0 COMMENT '答题耗时(秒)',
  `estimated_time`    VARCHAR(50)  NULL     COMMENT '预计出结果说明(待批改时非空)',
  `submitted_at`      DATETIME     NOT NULL COMMENT '交卷时间',
  `graded_at`         DATETIME     NULL     COMMENT '客观题批改完成时间',
  `writing_graded_at` DATETIME     NULL     COMMENT '作文批改完成时间',
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_es_user_exam` (`user_id`, `exam_id`, `created_at`),
  KEY `idx_es_status` (`status`, `submitted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模考提交表';

-- 模考作答进度表(扩展契约 E2;交卷成功后清除)
CREATE TABLE IF NOT EXISTS `exam_progress` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT      NOT NULL COMMENT '用户 ID',
  `exam_id`    VARCHAR(64) NOT NULL COMMENT '试卷 ID',
  `objective`  JSON        NULL     COMMENT '客观题作答进度(全局编号 -> 答案)',
  `writing`    JSON        NULL     COMMENT '作文作答进度 {content}',
  `duration`   INT         NOT NULL DEFAULT 0 COMMENT '已答题耗时(秒)',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ep_user_exam` (`user_id`, `exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模考作答进度表(断点续答)';