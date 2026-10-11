-- ============================================================
-- V10__normalize_writing_dimensions.sql
-- 目的  :把写作题的评分维度统一为 4 项 —— 内容要点 / 语言表达 / 篇章结构 / 格式规范
--         (2026-10-12 用户拍板:应用文的"格式"独立成一个给分维度)
-- 背景  :出题提示词 PromptTemplates 从 A3 起就要求模型输出恰好 4 项,而种子题面与
--         真题卷写作部分只有 3 项 —— 于是 AI 生成的写作题与真题在后台判分抽屉、
--         A7 预评里的维度行数不同,同一篇作文在两类题上的分项口径不可比
-- 前置  :V03(建 question 表)、V05(建 exam_paper 表)已执行;S02/S03 是否执行都无关
-- 幂等  :是 —— 仅当该数组里不含"格式规范"时才追加;重复执行不改动已合规的行
-- 执行  :见 db/README.md 的 mysql.exe 命令;执行前请先 mysqldump 备份 question 与
--         exam_paper 两张表,并把备份灌进临时库验证可还原(本次已演练通过)
-- 说明  :只改**题面** content.scoringDimensions,不改任何历史判分快照 writing_result ——
--         已 graded 的件保留判分当时的维度记录(库里唯一一份提交 #4 本来就是 4 维);
--         exam_paper 的 answer_key 里写作部分只有 sampleAnswer,没有维度字段,无需变更;
--         维度个数不参与分值计算(判分只校验各维度之和 ≤ 卷面总分),故本变更不动任何已有分数;
--         用 JSON_ARRAY_APPEND 而非整体覆写,是为了保留卷面原有的维度顺序与可能的自定义维度名
-- ============================================================

-- 1) 题库:所有写作题题面补齐"格式规范"
UPDATE `question`
SET `content` = JSON_ARRAY_APPEND(`content`, '$.scoringDimensions', '格式规范')
WHERE `type` = 'writing'
  AND JSON_LENGTH(JSON_EXTRACT(`content`, '$.scoringDimensions')) > 0
  AND NOT JSON_CONTAINS(JSON_EXTRACT(`content`, '$.scoringDimensions'), JSON_QUOTE('格式规范'));

-- 2) 真题卷:定位每张卷面写作部分题面的维度数组并补齐
--    (ExamPaperValidator 保证一张卷面"恰好一个 type=writing 的部分",故取首个匹配路径即可;
--     JSON_SEARCH 命中的是节级路径 $.sections[i].type,换成题面路径需接 .content.scoringDimensions)
UPDATE `exam_paper`
SET `content` = JSON_ARRAY_APPEND(
        `content`,
        REPLACE(JSON_UNQUOTE(JSON_SEARCH(`content`, 'one', 'writing', NULL, '$.sections[*].type')),
                '.type', '.content.scoringDimensions'),
        '格式规范')
WHERE JSON_SEARCH(`content`, 'one', 'writing', NULL, '$.sections[*].type') IS NOT NULL
  AND NOT JSON_CONTAINS(
        JSON_EXTRACT(`content`,
          REPLACE(JSON_UNQUOTE(JSON_SEARCH(`content`, 'one', 'writing', NULL, '$.sections[*].type')),
                  '.type', '.content.scoringDimensions')),
        JSON_QUOTE('格式规范'));
