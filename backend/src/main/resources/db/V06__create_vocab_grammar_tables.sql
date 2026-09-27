-- ============================================================
-- V06__create_vocab_grammar_tables.sql
-- 目的  :创建背单词与学语法相关表
--         (vocab_book / vocab_word / user_word_progress / user_word_review / grammar_node / grammar_section)
-- 前置  :V01__create_database.sql 已执行
-- 幂等  :是,可重复执行(IF NOT EXISTS)
-- 执行  :Get-Content .\backend\src\main\resources\db\V06__create_vocab_grammar_tables.sql -Raw |
--          mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
-- ============================================================

-- 词书表(3 本演示词书;learned 数由进度表实时派生)
CREATE TABLE IF NOT EXISTS `vocab_book` (
  `id`           VARCHAR(64)  NOT NULL COMMENT '词书 ID(如 book-syllabus)',
  `name`         VARCHAR(100) NOT NULL COMMENT '词书名称',
  `total`        INT          NOT NULL COMMENT '词书总词数(展示口径)',
  `filter_level` VARCHAR(20)  NULL     COMMENT '分层过滤:NULL=全部,high/core/cognitive',
  `sort_order`   INT          NOT NULL DEFAULT 0 COMMENT '展示排序',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='词书表';

-- 单词表(演示 20 词;真实 3000 词库后续导入)
CREATE TABLE IF NOT EXISTS `vocab_word` (
  `id`                  VARCHAR(64)  NOT NULL COMMENT '单词 ID(如 w-001)',
  `spelling`            VARCHAR(100) NOT NULL COMMENT '拼写',
  `phonetic`            VARCHAR(100) NOT NULL COMMENT '音标',
  `part_of_speech`      VARCHAR(50)  NOT NULL COMMENT '词性',
  `meanings`            JSON         NOT NULL COMMENT '释义列表(string[])',
  `example_sentence`    VARCHAR(500) NULL     COMMENT '例句',
  `example_translation` VARCHAR(500) NULL     COMMENT '例句翻译',
  `real_exam_sentence`  VARCHAR(500) NULL     COMMENT '真题原句',
  `real_exam_source`    VARCHAR(100) NULL     COMMENT '真题出处(年份+题型)',
  `level`               VARCHAR(20)  NOT NULL COMMENT '分层:high 高频/core 核心/cognitive 认知',
  `audio_url`           VARCHAR(255) NULL     COMMENT '发音音频地址(可空)',
  `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_vw_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单词表';

-- 用户单词学习进度表(简单间隔复习:unknown +1 天 / fuzzy +2 天 / known +7 天)
CREATE TABLE IF NOT EXISTS `user_word_progress` (
  `id`               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`          BIGINT      NOT NULL COMMENT '用户 ID',
  `word_id`          VARCHAR(64) NOT NULL COMMENT '单词 ID',
  `rating`           VARCHAR(20) NULL     COMMENT '最近一次自评:unknown/fuzzy/known',
  `review_count`     INT         NOT NULL DEFAULT 0 COMMENT '累计复习次数',
  `first_learned_at` DATETIME    NOT NULL COMMENT '首次学习时间(用于今日新学统计)',
  `last_review_at`   DATETIME    NOT NULL COMMENT '最近复习时间',
  `next_review_at`   DATETIME    NULL     COMMENT '下次应复习时间(到期复习查询)',
  `created_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uwp_user_word` (`user_id`, `word_id`),
  KEY `idx_uwp_due` (`user_id`, `next_review_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户单词学习进度表';

-- 单词复习日志表(按日统计:今日完成数 / 新学数)
CREATE TABLE IF NOT EXISTS `user_word_review` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '用户 ID',
  `word_id`     VARCHAR(64) NOT NULL COMMENT '单词 ID',
  `rating`      VARCHAR(20) NOT NULL COMMENT '自评:unknown/fuzzy/known',
  `reviewed_at` DATETIME    NOT NULL COMMENT '复习时间',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uwr_user_time` (`user_id`, `reviewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单词复习日志表';

-- 语法目录树(自引用;顶层为考点,叶节点对应章节)
CREATE TABLE IF NOT EXISTS `grammar_node` (
  `id`         VARCHAR(64)  NOT NULL COMMENT '节点 ID(如 tenses / tenses-overview)',
  `parent_id`  VARCHAR(64)  NULL     COMMENT '父节点 ID(顶层为 NULL)',
  `name`       VARCHAR(100) NOT NULL COMMENT '节点名称',
  `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '同级排序',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_gn_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='语法目录树表';

-- 语法章节表(叶节点章节内容,markdown 文本;含代码块,前端 hljs 双主题渲染)
CREATE TABLE IF NOT EXISTS `grammar_section` (
  `id`         VARCHAR(64)  NOT NULL COMMENT '章节 ID(与叶节点 node id 一致)',
  `node_id`    VARCHAR(64)  NOT NULL COMMENT '所属节点 ID',
  `title`      VARCHAR(200) NOT NULL COMMENT '章节标题',
  `markdown`   MEDIUMTEXT   NOT NULL COMMENT 'Markdown 内容:考点精讲/真题示例/易错点辨析/专项练习',
  `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '展示排序',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_gs_node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='语法章节表';