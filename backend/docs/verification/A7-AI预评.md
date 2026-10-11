# A7 AI 预评（写作件起草，不落库）

> 阶段:后台管理端第二批的 A7。目标只有一个 —— 把批改工作台里"从零手写四维评语"变成"审一份 AI 草稿"。
> 本阶段**无数据库结构变更**,也**不新增任何落库路径**。

## 1. 交付内容

| 接口 | 作用 |
|---|---|
| `POST /api/admin/submissions/{id}/ai-preview` | 练习写作件(待批改的写作题)AI 起草 |
| `POST /api/admin/exam-submissions/{id}/ai-preview` | 模考作文(AI 异步评分失败停在 `partially_graded` 的件)AI 起草 |

返回同一个 `WritingPreviewDto`:`score` / `cap` / `dimensions[{name,score,comment}]` / `sampleAnswer` / `overallComment`。

### 三条设计纪律

1. **只读不写。** 两个方法没有任何 `update`/`insert`,也不碰 `writing_graded_at`。这条不是风格问题:`writing_graded_at` 一写,学生端就认为作文出分了。判分仍必须走 A4/A4-② 那两条带 CAS 的 `/grade`、`/grade-writing`。
2. **满分取题面,不信 AI 自报。** 练习用提交时快照的 `total_score`,模考用试卷写作部分的 `score`。
3. **建议分取"维度实算"而不是 AI 自报合计。** 唯一一处共用逻辑放在 `WritingGradeSupport.toPreview`(与两条判分路径同一个写手文件),它按维度分求和再封顶。
   理由:抽屉里管理员看到的是一格一格的维度分,顶部总分若和它们加不起来,就会被当成 bug。`AiGradingService.gradeWriting` 内部另有一个 AI 自报口径的合计,预评**不用它**;两次实测的维度分(0/4/2 与 0/4/3)求和后都与界面显示的 6、7 一致,说明走的是实算这条路径。

### 前端行为(两个判分抽屉同构)

学生作文下方新增 **「AI 预评(起草)」**，填入后出现 **「撤销 AI 草稿」**。

- **不做弹窗确认,改成可撤销**:覆盖前把当前维度行/范文/总评/得分覆盖快照下来,一键还原。手写评语被冲掉是判分场景里最招人烦的失误,少一次点击又不丢东西。
- **按维度名合并不是整表替换**:抽屉里的行按题面 `scoringDimensions` 预填,AI 少给一维时不能把那一维从界面上抹掉(否则管理员看不到"还有一维没分",提交才被服务端拒);AI 多给的维度追加成行。提示语会说明"还有 N 个维度 AI 没给分"或"维度合计超过满分"。
- 失败时表单**没被动过**,提示"可以直接重试"(错误原因由 request 层弹出)。

### 明确没做的

- **观测面板已按你的决定删除。** 连带后果:AI 建议分与人工最终分的差异**不留痕**,事后无法统计"AI 判得准不准"。要留痕需要新增一张表或在 submission 上加列。
- 不做批量预评(队列里一键全判) —— 那等于把"人签字"变成走过场。
- 不给客观题做预评:它们由答案键比对出分,见 `PracticeService.java:379-381`,根本不进人工队列。

## 2. 本次核验结果(2026-10-12)

单测:新增 8 例(练习 4 + 模考 4),全量 `mvn -B clean test` → **Tests run: 123, Failures: 0, Errors: 0**。
新增用例覆盖:客观件拒绝、已判完拒绝、无正文拒绝、试卷无写作部分拒绝、**封顶后仍不写库**(`verify(never()).update(...)` + `never().insert(...)`),以及"总分取维度实算而非 AI 自报"这条口径。

前端 `npm run type-check` / `lint --max-warnings 0` / `build` 全绿。

运行期实测(后端 8081、前端 5174、真实 deepseek-flash):

| 验收点 | 实测 |
|---|---|
| 练习件预评(#2,种子里那条 2023 真题写作) | `code=0` 11.4 秒,建议 **3 / 满分 15**,三维齐全;总评指出"只写了称呼和开头一句,未展开任何建议,也未完成书信格式" |
| **预评不写库** | 调完两次接口后 SQL:`practice_submission #2` 仍 `status=pending`、`writing_result` 仍 NULL;`exam_submission #1` 仍 `partially_graded`、`writing_graded_at` 仍 NULL |
| 模考件预评(#1) | `code=0` 14.5 秒,建议 **6 / 15**;**内容要点直接给 0** 并说明"题目要求写人工智能与学习的投稿短文,但本文写的是申请英语角志愿者" —— 判分真的读了题干而不是只看语言 |
| 客观件拒绝 | `code=400 只有写作题需要 AI 预评;客观题按答案键自动比对,不进人工队列` |
| 已判完拒绝 | `code=400 该提交的作文已出分(7.0 分),无需 AI 预评` |
| 界面实拍(in-app browser,DOM 驱动) | 点「判作文」→ 抽屉预填 3 个维度行(空)→ 点「AI 预评」→ 按钮转 loading + 提示"AI 正在读这份作文(通常 10~40 秒)…" → 完成后三行变为 `内容要点=0 / 语言表达=4 / 篇章结构=3`,范文与总评两个文本域被填上,合计显示「维度合计 7 / 写作满分 15」,提示语「这只是草稿,点『提交判分』才对学生生效」,并出现「撤销 AI 草稿」 |
| 撤销 | 点撤销后:三行分数与评语回到空、两个文本域长度归 0、按钮消失、提示「已撤销 AI 草稿,回到覆盖前的内容」 |
| 看板口径 | `practicePending=1`、`examPending=0`、`examWritingUngraded=1` 与 SQL 一致 |

## 3. 你如何复现验证

```text
前置:admin / 123456 登录,后端 8081、前端 http://localhost:5174,backend/.env.local 里 AI 三项已配好

1) http://localhost:5174/admin/submissions → 「模考提交」页签
   点第一行「判作文」→ 抽屉里点「AI 预评(起草)」→ 等 10~40 秒
   看到:三个维度行被填分,范文/总评被填上,提示语说明"只是草稿"
   点「撤销 AI 草稿」→ 应完全回到覆盖前
2) 再点一次「AI 预评」,这次改成你满意的分数 → 点「提交判分」
   → 学生端(demo / 123456)「我的模考」这份报告立刻出现维度表、范文与总评
3) 「练习提交」页签对那条写作件重复一遍(队列筛选「只看必须人工」)
4) SQL 抽查(第 1、2 步之间执行,应看到 pre-eval 完全不改库):
   SELECT id,status,grading_method,writing_score,writing_graded_at FROM exam_submission;
   SELECT id,status,grading_method,score,writing_result IS NULL FROM practice_submission ORDER BY id;
```

## 4. 踩坑与取舍记录

- **`ExamService.report` 有防重入兜底**(`ExamService.java:268-271`):读到 `partially_graded` + `grading_method='ai'` 且作文未批的件,会**再触发一次异步 AI 评分**。所以演示用的断链样本必须做成 `grading_method='manual'`,否则你在浏览器里打开一次学生成绩报告,这件就自己被判掉、从队列里消失了 —— 看着像 bug,其实是兜底按设计工作。
- 抽屉维度行按题面预填,而**题面是 3 个维度**(内容要点/语言表达/篇章结构);早先我在测试夹具里写的 4 维(含"格式规范")不是真实试卷数据,别拿夹具当口径。
- **前端拿不到 ref 对象**:Vue 模板里 ref 会自动解包,所以 `@click="restoreSnapshot({rows: dimensionRows, ...})"` 传进去的是解包后的值而不是 Ref,撤销会静默失效。改成脚本里包一层 `undoPreview()` / `undoExamPreview()`。
- 没有引入 `n-dialog-provider`(后台布局只有 message-provider)。本来要用弹窗确认,改成可撤销之后不需要 —— 顺带避开给 `AdminLayout` 整棵子树重新缩进的风险(`--max-warnings 0` 下不划算)。
- 一次预评 11~15 秒,按钮必须给 loading 且允许反复点;失败路径特意**不覆盖**已填内容。

## 5. 边界

- 预评只在抽屉里存在,**不落库、不缓存**,关掉抽屉就没了(重新点开需再要一次)。这是刻意的:任何"AI 结果先存起来"的做法都会和"学生选了人工批改"的承诺打架。
- 模考作文件的预评需要该件已有客观分(与判分同一前提)。
- `has_answer=0` 的客观题(即 AI 判客观题那条分支)仍然没有入口能触发 —— 本次没动它,它属于后面「AI 校核/补全这一题」那一步要一并解决的事。
- 待办:表单权威化与未知字段 passthrough → 后台 AI 生成草稿 → AI 校核单题(补 `acceptAlternatives` + 校核解析)→ 真题卷结构化编辑。
