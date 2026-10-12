# A9 · 后台一键 AI 生成草稿（不落库）

> 2026-10-12。依赖 A8 的表单权威性：草稿灌进**表单**而不是 JSON 面板，改完点保存才入库、才上架。
> 后端 +78 行、前端 +140 行；判分链路与学生端零改动。

## 1. 做了什么

**后端**（`AiGenerateService` 拆成两段，共用同一条链路）

- `generate(type)`：学生端"点即生成"，生成即落库且默认上架（A3 起的老口径，没改）。
- `generateDraft(type)`：新增出口。走完全相同的「调用模型 → 浅层结构校验 → 拆分双 JSON → 五道闸门（`QuestionContentValidator`，与人工录入同一实现）」，**区别只是到此为止不碰 `questionMapper`**。
  所以不存在"AI 草稿绕过校验"的口子：草稿和已入库题目是同一个质量标准。
- 新增 `POST /api/admin/questions/ai-draft`（请求 `AiDraftRequest{type}`，响应 `AiDraftDto`）。沿用 `MAX_ATTEMPTS=2` 的重试与 60s 超时；`AiChatService` 内部异常语义未改。
- 顺手把重复代码抽成 `resolveTitle` / `resolveDifficulty` / `buildAiMeta` / `buildAiSource` / `collectTags`，两个出口共用；`buildAndPersist` 里 source 节点改为**只构建一次**，`content.source` 与 `source_meta` 列复用同一对象（原来两处分别生成，`generatedAt` 有毫秒级差异的隐患）。

**返回的草稿不含 `content.id/type/source`** —— 这三个字段由服务端在保存时按最终题型与来源覆写，草稿阶段编一个 id 只会误导管理员去改一个马上被覆盖的值（这也是测试里专门断言的一点）。
`sourceMeta` 返回的是 **meta 子对象**（`{model, generatedAt}`），与题目详情 `AdminQuestionDetailDto.sourceMeta`、录入请求 `QuestionUpsertRequest.sourceMeta` 同形，前端拿到什么就能原样回填什么。

**前端**（`QuestionEdit.vue`，只在**新建**模式提供）

- 头部「AI 生成一版」按钮：点击期间显示"通常 10~40 秒，最长约 2 分钟（超时或结构不合格会自动重试一次）"，避免被当成点了没反应。
- 成功后调 `hydrateFrom(draft.content, draft.answerKey)` 把草稿灌进表单 —— 与编辑反填**共用同一个函数**，因此 A8 的 passthrough、编号续号、形状保留规则一律适用；面板同步显示"此刻会提交的内容"，表单仍是权威。
- 绿色提示条写明"已填入 AI 草稿（模型 … · 时间），**尚未保存**，点「创建题目」才入库上架"，并给「丢弃草稿」按钮（清内容字段 + 清 passthrough 打底对象）。
- 来源自动置为 `ai`，并把草稿的 `{model, generatedAt}` 暂存；保存时只在来源仍为 `ai` 时才带回（管理员改成真题/模拟题就不带，由服务端按原口径覆写）。
- 编辑已有题时**不提供**整题替换生成（会覆盖人工内容并破坏历史提交对应关系），按钮只在 `/admin/questions/new` 出现。
- 未注册任何 MSW handler（后台接口一律直连真实后端）。

## 2. 手动验证方法与实测结果（全部通过）

| # | 验证项 | 做法 | 实测结果 |
|---|---|---|---|
| 1 | 五种题型真实生成 | `node backend/target/a9-draft-check.mjs`（对每种题型各调一次草稿接口） | reading 12.6s / matching 37.4s / cloze 29.0s / grammar-fill 17.8s / writing 9.3s，**五类全部一次过闸门、无重试**；小题数 5/5/10/10/1，写作维度=4，来源 meta 均带 `deepseek-flash` |
| 2 | 草稿不落库 | 脚本比较调用前后 `/admin/questions` 的 `total` | 15 → 15 ✓ |
| 3 | 答案不泄漏 | 脚本检查每份草稿 `content` 是否含 `answer/explanation/sampleAnswer/acceptAlternatives` 键，以及是否含 `id/type/source` | 全部不含 ✓ |
| 4 | 参数与鉴权守卫 | 缺 `type`→`code=400 题型不能为空`；`type=speaking`→`code=1004`；学生令牌调用→`code=403 无权限访问` | 三条全对 ✓（`node backend/target/a9-guard-check.mjs` 在最终构建上复跑一遍，同样全绿） |
| 5 | 闸门同口径 | `AiGenerateServiceDraftTest` 用答案不在候选项内的坏产出 | `code=500` 且文案 `AI 出题结果不合格:` 前缀，`verify(questionMapper, never()).insert(...)` ✓ |
| 6 | 界面真实路径 | 浏览器进 `/admin/questions/new` → 点「AI 生成一版」 | 按钮进 loading、时长提示出现；返回后文章 1152 字符含 `[[n]]`、10 个空各 4 个选项、答案 `B` 与中文解析都进了表单、标题与标签自动填、行首显示「编号 1..10」、提示条给出模型与时间 |
| 7 | 点保存才入库 | 同页面点「创建题目」 | 生成 `cloze-ai-261012090842`；`sourceType=ai`、`status=1`、`hasAnswer=true`、分值按题型默认映射 20/2；`sourceMeta={"model":"deepseek-flash","generatedAt":"..."}`（证明前端把草稿来源带回并被服务端持久化）；`content` 键补齐为 `id,type,blanks,source,passage` |
| 8 | 不留验证残渣 | 删除该临时题 | `code=0`，题库回到 **15** ✓ |
| 9 | 全量门禁 | 隔离副本 `./mvnw.cmd -B clean test`；前端 `type-check`/`lint --max-warnings 0`/`build` | **Tests run: 132, Failures: 0**（基线 123 + A8 新增 5 + A9 新增 4）；前端三关全绿，PWA precache 172 条 |

## 3. 有意不做的

- 不做"AI 出整卷"、不做批量生成（用户已排除）。
- 草稿接口不接受任何题面内容入参，也不做知识点参数面板（沿用 A3 的"无参数面板"决定）。
- 不在编辑模式提供整题替换生成（见上）。
- 不改学生端 `POST /api/practice/generate` 的"生成即落库即上架"口径 —— 两条路径的差异是刻意的。

## 4. 失败口径与排查

- 模型不可用/超时：`code=500`，`message` 为「AI 服务响应超时」或「AI 服务暂时不可用」（`AiChatService`）。
- 产出结构反复不过闸门：`code=500`，`message` 以「AI 出题结果不合格:」开头并带定位路径。
- **要收紧的是 `PromptTemplates`，不是校验器**（A3 既定原则）。已有一处异常吞没要注意：`AiChatService.chat` 的 `catch (BizException e)` 分支实际命中不到（内部异常经 `CompletableFuture` 变成 `ExecutionException`），排查时看后端日志而不是只看响应文案。
- 本项目不记录 AI 调用（观测面板已按用户决定砍掉），所以失败/重试只存在于后端日志，`source_meta.model` 是唯一的落库痕迹。
