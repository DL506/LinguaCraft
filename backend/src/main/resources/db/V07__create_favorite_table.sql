-- ============================================================
-- V07__create_favorite_table.sql
-- 目的  :创建收藏表(favorite;支持单词与句子两种类型)
-- 前置  :V01__create_database.sql 已执行
-- 幂等  :是,可重复执行(IF NOT EXISTS)
-- 执行  :Get-Content .\backend\src\main\resources\db\V07__create_favorite_table.sql -Raw |
--          mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
-- ============================================================

-- 收藏表(对应前端 FavoriteItem)
CREATE TABLE IF NOT EXISTS `favorite` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键(对外序列化为字符串)',
  `user_id`    BIGINT       NOT NULL COMMENT '用户 ID',
  `type`       VARCHAR(20)  NOT NULL COMMENT '类型:word 单词 / sentence 句子',
  `content`    VARCHAR(500) NOT NULL COMMENT '收藏内容(单词拼写或句子原文)',
  `ref_id`     VARCHAR(64)  NULL     COMMENT '来源引用 ID(题目/单词;可空)',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  KEY `idx_fv_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';