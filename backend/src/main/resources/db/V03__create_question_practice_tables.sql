-- ============================================================
-- V03__create_question_practice_tables.sql
-- 目的  :创建题库与刷题主链路相关表(question / practice_submission / mistake / practice_progress)
-- 前置  :V01__create_database.sql 已执行(库 linguacraft 存在)
-- 幂等  :是,可重复执行(IF NOT EXISTS)
-- 执行  :Get-Content .\backend\src\main\resources\db\V03__create_question_practice_tables.sql -Raw |
--          mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
-- 说明  :question 采用"双 JSON 列"设计:content 存下发用内容(不含答案),
--         answer_key 存答案与解析(接口链路永不读取下发),从存储层杜绝答案泄漏。
-- ============================================================

-- 题库表:五种题型的练习题目(对应前端 PracticeItem / PracticeListItem)
CREATE TABLE IF NOT EXISTS `question` (
  `id`                 VARCHAR(64)  NOT NULL COMMENT '题目 ID(语义化,如 reading-real-2023-1)',
  `type`               VARCHAR(20)  NOT NULL COMMENT '题型:reading/matching/cloze/grammar-fill/writing',
  `source_type`        VARCHAR(20)  NOT NULL COMMENT '来源:real/teacher/ai',
  `source_meta`        JSON         NOT NULL COMMENT '来源附带信息(完整 source 对象:{type,meta:{year,region,teacherName,school,model,generatedAt}})',
  `title`              VARCHAR(200) NOT NULL COMMENT '标题(列表展示)',
  `difficulty`         TINYINT      NOT NULL DEFAULT 3 COMMENT '难度 1-5',
  `tags`               JSON         NOT NULL COMMENT '标签数组(string[])',
  `content`            JSON         NOT NULL COMMENT '下发内容(ForAnswer,不含答案/解析)',
  `answer_key`         JSON         NULL     COMMENT '答案与解析(reading:questions[];matching/cloze/grammar-fill:blanks[];writing:sampleAnswer)',
  `has_answer`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否具备可比对答案:1 有(可数据库比对),0 无(转 AI 批改或人工)',
  `total_score`        DECIMAL(4,1) NOT NULL DEFAULT 10.0 COMMENT '练习场景总分',
  `per_question_score` DECIMAL(3,1) NOT NULL DEFAULT 2.0 COMMENT '练习场景单题分值',
  `status`             TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1 启用,0 停用',
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_type_source` (`type`, `source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库表';

-- 练习提交表:单题练习的提交记录(对应前端 SubmitResult / WritingSubmitResult / SubmissionListItem)
CREATE TABLE IF NOT EXISTS `practice_submission` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键(对外序列化为字符串)',
  `user_id`        BIGINT       NOT NULL COMMENT '用户 ID',
  `question_id`    VARCHAR(64)  NOT NULL COMMENT '题目 ID',
  `type`           VARCHAR(20)  NOT NULL COMMENT '题型',
  `title`          VARCHAR(200) NOT NULL COMMENT '标题快照(列表展示,避免联表)',
  `source_type`    VARCHAR(20)  NOT NULL COMMENT '来源类型(快照)',
  `source_meta`    JSON         NOT NULL COMMENT '来源信息(快照)',
  `status`         VARCHAR(20)  NOT NULL COMMENT '批改状态:graded/partially_graded/pending',
  `grading_method` VARCHAR(10)  NOT NULL COMMENT '批改方式:ai/auto/manual',
  `score`          DECIMAL(5,1) NULL     COMMENT '得分(未批改时为空)',
  `total_score`    DECIMAL(5,1) NOT NULL COMMENT '总分(快照)',
  `correct_count`  INT          NULL     COMMENT '正确数(未批改时为空)',
  `total_count`    INT          NULL     COMMENT '总题数',
  `answers`        JSON         NOT NULL COMMENT '用户作答原文(客观题 Record<题号,答案>;写作 {content})',
  `details`        JSON         NULL     COMMENT '逐题批改明细(SubmitDetail 数组,仅批改完成后下发)',
  `writing_result` JSON         NULL     COMMENT '写作批改结果(dimensions/sampleAnswer/overallComment/writingScore)',
  `duration`       INT          NOT NULL DEFAULT 0 COMMENT '作答耗时(秒)',
  `estimated_time` VARCHAR(50)  NULL     COMMENT '预计出结果说明(待批改时返回)',
  `submitted_at`   DATETIME     NOT NULL COMMENT '提交时间',
  `graded_at`      DATETIME     NULL     COMMENT '批改完成时间',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ps_user_created` (`user_id`, `created_at`),
  KEY `idx_ps_user_question` (`user_id`, `question_id`),
  KEY `idx_ps_user_type` (`user_id`, `type`),
  KEY `idx_ps_status_submitted` (`status`, `submitted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习提交表';

-- 错题表:批改完成后自动收录的错误明细(追加式,对应前端 MistakeItem)
CREATE TABLE IF NOT EXISTS `mistake` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键(对外序列化为字符串)',
  `user_id`        BIGINT       NOT NULL COMMENT '用户 ID',
  `question_id`    VARCHAR(64)  NOT NULL COMMENT '来源题目 ID(错题重做跳转用)',
  `type`           VARCHAR(20)  NOT NULL COMMENT '题型',
  `source_type`    VARCHAR(20)  NOT NULL COMMENT '来源类型(快照)',
  `source_meta`    JSON         NOT NULL COMMENT '来源信息(快照)',
  `stem`           VARCHAR(500) NOT NULL COMMENT '题干摘要(阅读取题干;其余为"标题 第 N 空")',
  `user_answer`    VARCHAR(500) NOT NULL COMMENT '用户作答',
  `correct_answer` VARCHAR(500) NOT NULL COMMENT '正确答案',
  `explanation`    TEXT         NULL     COMMENT '解析',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收录时间',
  PRIMARY KEY (`id`),
  KEY `idx_mk_user_created` (`user_id`, `created_at`),
  KEY `idx_mk_user_type` (`user_id`, `type`),
  KEY `idx_mk_user_source` (`user_id`, `source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题表';

-- 作答进度表:未提交作答的断点续答持久化(扩展契约 E1;提交成功后清除)
CREATE TABLE IF NOT EXISTS `practice_progress` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '用户 ID',
  `question_id` VARCHAR(64) NOT NULL COMMENT '题目 ID',
  `type`        VARCHAR(20) NOT NULL COMMENT '题型',
  `answers`     JSON        NOT NULL COMMENT '作答进度原文(结构同提交 answers)',
  `duration`    INT         NOT NULL DEFAULT 0 COMMENT '已作答耗时(秒)',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pp_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='作答进度表(断点续答)';