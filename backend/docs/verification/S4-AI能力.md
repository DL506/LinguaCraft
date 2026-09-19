# S4 手动验证方法(AI 能力 / Spring AI)

> 接口:POST /api/practice/generate(AI 出题)、POST /api/practice/smart(智能练习);并在 submit 中提供 AI 分支(作文 AI 全权评分、无答案客观题 AI 判分)
> 数据库:无结构变更(复用 question 表;AI 题目 source_type=ai,双 JSON 列同种子结构)

## 0. 模型配置(决定验证路径)

- 真实调用需配置 OpenAI 兼容服务(环境变量或直接修改 `application.yml`):
  - `LC_AI_API_KEY`(api-key)、`LC_AI_BASE_URL`(如 DeepSeek/OpenAI 兼容地址)、`LC_AI_MODEL`(模型名)
- 默认占位配置(fake key/placeholder.invalid)下 AI 调用必然失败:**验证降级路径**(见 1.1/1.3 失败分支);
- 超时控制:`lc.ai.timeout-seconds`(默认 60 秒)。

## 1. 接口验证

### 1.1 AI 出题

| # | 操作 | 预期 |
|---|---|---|
| 1 | POST /api/practice/generate body `{"type":"reading"}` | 有效 key:10~30 秒返回 PracticeItem(ForAnswer,无答案字段);失败:code=500「AI 服务暂时不可用」且前端可重试 |
| 2 | 五种题型各生成一次(matching/cloze/grammar-fill/writing 同法) | 返回结构符合契约;非空 passage/prompt;题号 1..n 连续 |
| 3 | 用返回的 id 调 GET /api/practice/detail/{type}/{id} | 可正常取题(生成即持久化,错题重做/提交链路可用) |
| 4 | 对该 AI 题提交(answers 按生成内容作答) | 与种子题相同链路:AI 模式即时判分 |
| 5 | type 传非法值 | code=1004 |

### 1.2 智能练习

| # | 操作 | 预期 |
|---|---|---|
| 6 | POST /api/practice/smart | 返回一道题;type 从返回值读取(前端据此进入对应题型页) |
| 7 | 完成若干题后再次调用 | 优先返回"未做过"的题目;做过一部分后权重偏向低得分率题型 |

### 1.3 AI 作文评分(练习,AI 模式)

| # | 操作 | 预期 |
|---|---|---|
| 8 | 设置 ai 批改后:POST .../writing-real-2023-1/submit,body `{"answers":{"content":"Dear Peter, ..."},"duration":600}` | 有效 key:status=graded、gradingMethod=ai、score(0~15)、dimensions 数组、sampleAnswer、overallComment |
| 9 | 将 api-key 改错后重复提交 | **降级路径**:status=pending、gradingMethod=manual、estimatedTime 非空;后端日志出现「AI 作文评分失败,降级为人工 pending」 |
| 10 | 人工模式(gradingMethod=manual)提交 | 直接 pending 转人工(不调用 AI) |

### 1.4 AI 判客观题(题库无答案时)

S02 种子客观题都有答案(走数据库比对,不触发 AI);如需验证 AI 判分路径,可临时构造"无答案题":

```sql
-- 临时把一道题改为"无答案",触发 AI 判分分支
UPDATE question SET has_answer = 0, answer_key = NULL WHERE id = 'reading-teacher-1';
```

| # | 操作 | 预期 |
|---|---|---|
| 11 | AI 模式提交该题 | 有效 key:status=graded、details 由 AI 给出 correctAnswer/explanation;失败:降级 pending manual |
| 12 | 验证完成后恢复种子 | 重跑 `S02__seed_questions.sql` 即恢复(幂等) |

## 2. SQL 抽查

```sql
-- AI 生成的题目(source_type=ai,含模型与生成时间)
SELECT id, type, has_answer, JSON_EXTRACT(source_meta,'$.meta.model') AS model, created_at
FROM question WHERE source_type = 'ai' ORDER BY created_at DESC LIMIT 5;

-- 最近提交的批改方式与状态(AI/降级可见)
SELECT id, type, status, grading_method, score, estimated_time, graded_at
FROM practice_submission ORDER BY id DESC LIMIT 5;
```

## 3. 前端验证(关 MSW;真实 key 时)

1. 任选题型页将来源切到「AI出题」:应出现 loading(AI 正在为你出题),完成后直接进入答题;失败出现「重试」。
2. 首页快捷入口「智能练习」:进入返回题目对应的题型页作答。
3. 作文页(AI 模式)提交:即时进入批改态,展示评分维度 / 范文 / 总评(任务书 10.11)。
4. key 无效时作文提交:进入 pending 占位页(「预计 24 小时内出结果」),体现"AI 失败自动转人工"。