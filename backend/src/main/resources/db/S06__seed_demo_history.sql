-- ============================================================
-- S06__seed_demo_history.sql
-- 目的  :写入演示历史数据(demo 用户 user_id=1):练习提交/错题/收藏/背单词进度
--         使个人中心(学习统计/我的提交/错题本/收藏/我的词书)与首页提示非空
-- 前置  :V03、V06、V07 已执行;S01(demo 用户)、S02(题库)已执行
-- 幂等  :是;提交/错题/收藏/复习日志采用"先删固定演示 ID 再插入";单词进度按唯一键覆盖
-- 说明  :演示数据仅覆盖 id 为 1/2/3 的固定记录,不影响后续真实新增数据
-- ============================================================

-- 1) 练习提交(3 条:1 条已批改阅读 / 1 条待人工批改作文 / 1 条人工模式待批改阅读)
DELETE FROM `practice_submission` WHERE `id` IN (1, 2, 3);
INSERT INTO `practice_submission`
  (`id`, `user_id`, `question_id`, `type`, `title`, `source_type`, `source_meta`, `status`, `grading_method`,
   `score`, `total_score`, `correct_count`, `total_count`, `answers`, `details`, `writing_result`, `duration`,
   `estimated_time`, `submitted_at`, `graded_at`)
VALUES
  (1, 1, 'reading-real-2023-1', 'reading', '2023年真题·阅读理解 Passage 1', 'real',
   '{"type":"real","meta":{"year":2023,"region":"广东"}}', 'graded', 'ai', 6.0, 10.0, 3, 5,
   '{"1":"B","2":"A","3":"B","4":"D","5":"B"}',
   '[{"id":1,"userAnswer":"B","correctAnswer":"B","isCorrect":true,"explanation":"全文对比数字阅读与纸质阅读对学习结果的影响,故选 B。"},{"id":2,"userAnswer":"A","correctAnswer":"C","isCorrect":false,"explanation":"第二句指出数字阅读者容易快速浏览但记得更少。"},{"id":3,"userAnswer":"B","correctAnswer":"B","isCorrect":true,"explanation":"第三句说明纸质书阅读者在理解测试中得分更高。"},{"id":4,"userAnswer":"D","correctAnswer":"B","isCorrect":false,"explanation":"scan 与 remember less 并列,结合语境意为快速浏览。"},{"id":5,"userAnswer":"B","correctAnswer":"B","isCorrect":true,"explanation":"由纸质阅读者记得更牢、得分更高可推出有助于深度学习。"}]',
   NULL, 420, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
  (2, 1, 'writing-real-2023-1', 'writing', '2023年真题·写作', 'real',
   '{"type":"real","meta":{"year":2023,"region":"广东"}}', 'pending', 'manual', NULL, 15.0, NULL, NULL,
   '{"content":"Dear Peter, I am glad to hear your plan..."}', NULL, NULL, 1500,
   '预计 24 小时内出结果', NOW() - INTERVAL 2 HOUR, NULL),
  (3, 1, 'reading-teacher-1', 'reading', '王老师·模拟阅读卷 Passage 1', 'teacher',
   '{"type":"teacher","meta":{"teacherName":"王老师","school":"启航班工作室"}}', 'pending', 'auto', NULL, 10.0, NULL, NULL,
   '{"1":"B","2":"B","3":"B","4":"A","5":"B"}', NULL, NULL, 350,
   '预计 24 小时内出结果', NOW() - INTERVAL 5 MINUTE, NULL);

-- 2) 错题(2 条:阅读细节题 + 完形填空)
DELETE FROM `mistake` WHERE `id` IN (1, 2);
INSERT INTO `mistake`
  (`id`, `user_id`, `question_id`, `type`, `source_type`, `source_meta`, `stem`, `user_answer`, `correct_answer`, `explanation`, `created_at`)
VALUES
  (1, 1, 'reading-real-2023-1', 'reading', 'real',
   '{"type":"real","meta":{"year":2023,"region":"广东"}}',
   'According to the passage, digital readers tend to ________.', 'A', 'C',
   '第二句指出数字阅读者容易快速浏览但记得更少。', NOW() - INTERVAL 1 DAY),
  (2, 1, 'cloze-teacher-1', 'cloze', 'teacher',
   '{"type":"teacher","meta":{"teacherName":"王老师","school":"启航班工作室"}}',
   'But she kept a training diary and recorded every small ________.', 'A', 'B',
   'recorded every small victory 记录每次小胜利,与训练日记呼应。', NOW() - INTERVAL 2 DAY);

-- 3) 收藏(2 条:单词 + 句子)
DELETE FROM `favorite` WHERE `id` IN (1, 2);
INSERT INTO `favorite` (`id`, `user_id`, `type`, `content`, `ref_id`, `created_at`)
VALUES
  (1, 1, 'word', 'abandon', 'w-001', NOW() - INTERVAL 1 DAY),
  (2, 1, 'sentence', 'Reading is one of the most valuable habits a student can build.', 'matching-real-2023-1', NOW() - INTERVAL 1 DAY);

-- 4) 背单词进度(3 词:w-001 认识 / w-002 模糊(已到期) / w-003 不认识)
INSERT INTO `user_word_progress`
  (`user_id`, `word_id`, `rating`, `review_count`, `first_learned_at`, `last_review_at`, `next_review_at`)
VALUES
  (1, 'w-001', 'known', 3, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, NOW() + INTERVAL 7 DAY),
  (1, 'w-002', 'fuzzy', 1, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 1 HOUR),
  (1, 'w-003', 'unknown', 1, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE, NOW() + INTERVAL 1 DAY) AS new
ON DUPLICATE KEY UPDATE
  `rating` = new.`rating`, `review_count` = new.`review_count`,
  `first_learned_at` = new.`first_learned_at`, `last_review_at` = new.`last_review_at`,
  `next_review_at` = new.`next_review_at`;

-- 5) 今日复习日志(2 条,用于 task/today.doneCount)
DELETE FROM `user_word_review` WHERE `id` IN (1, 2);
INSERT INTO `user_word_review` (`id`, `user_id`, `word_id`, `rating`, `reviewed_at`)
VALUES
  (1, 1, 'w-001', 'known', NOW() - INTERVAL 2 HOUR),
  (2, 1, 'w-002', 'fuzzy', NOW() - INTERVAL 1 HOUR);