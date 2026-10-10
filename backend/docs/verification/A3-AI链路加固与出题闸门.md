# A3 AI 链路加固与出题闸门(手动验证方法)

> 阶段:后台管理端计划的 A3(原路线图 A3 为"内容全家桶",本次按用户决策改为先接通 AI)。
> 本阶段**无数据库结构变更**;做的是把已有 AI 代码从"能跑但每次都失败降级"推进到"接入真实端点前不产生脏数据,接入后判分口径正确"。

## 1. 为什么要做这一步

`application.yml:29-33` 里 AI 三项配置是假占位(`sk-fake-placeholder-key` / `https://placeholder.invalid/v1` / `fake-model`),因此四个 AI 入口每次都失败并走降级:

| 入口 | 加固前的实际表现 |
|---|---|
| `POST /api/practice/generate` | `code=500`「AI 出题失败,请重试」,出不来题 |
| 作文 AI 评分(练习提交) | 降级 `pending` + `grading_method=manual`,**永远无人批改** |
| 无答案客观题 AI 判分 | 同上 |
| 模考作文异步评分 | 停在 `partially_graded`,`writing_graded_at` 为空 |

排查过程中纠正了两个判断:

- ~~"AI 返回需要剥代码块围栏"~~ —— `PromptTemplates.extractJson:131` 早已实现,无需改动。
- ~~"作文总分没有封顶"~~ —— `AiGradingService.gradeWriting` 已有 `cap` 逻辑;真正的缺陷在客观题明细行数。

## 2. 本阶段改了什么

### 2.1 出题闸门(核心)

AI 出题原先只做浅层"字段在不在"检查(`require`/`requireArray`),然后 `questionMapper.insert` 直接入库,**A2 的五道入库校验一道没过**。所以模型少写一个 `[[3]]`、答案给了不存在的选项 key、空位编号重复,都会静默落库成脏题,并且 `status=1` 立刻出现在学生端练习列表。

做法是把五道校验从 `AdminQuestionService` 抽成共享组件 **`service/QuestionContentValidator`**(逐字搬迁,报错文案不变),两条入库路径共用:

- `AdminQuestionService.validateAndNormalize` 改为委托自身瘦身 325 行,只管请求解析与权威字段覆写;
- `AiGenerateService` 的 `callWithRetry` 现在返回 `Draft(raw, content, answerKey)`,闸门不通过就**不落库**,并把原因转成可重试错误(`AI 出题结果不合格:...`)后重试一次。

顺带修正五选五提示词的自相矛盾:原文要求"5 个候选句子(A~E,**其中 1 个为干扰项**)",而校验 4 要求五个答案互不重复 —— 5 空 5 句还留干扰项必然产生重复答案,模型照着写必然被闸门拦死。改为"五句各用一次(本题型无干扰项)",与 `S02` 种子里那道五选五的实际形状一致。

### 2.2 判分口径

`gradeObjective` 原先按**模型返回的明细**建行:模型漏答某个题号,学生端逐题表就少一行,`correct_count/total_count` 也跟着错位。现改为按**学生实际作答的题号集合**建行 —— 漏答补成 `isCorrect=false` 且解析写明「AI 未返回该题判定,请转人工复核」,多答的未知题号忽略,得分另按卷面总分封顶。

### 2.3 配置注入与密钥自检

- 新增入库的 `backend/.env.example`:变量清单 + 三种注入方式(Git Bash / PowerShell / VSCode `envFile`)。
- 新增 `config/RuntimeSafetyReporter`:启动完成后对未显式注入的 `LC_AI_*` / `LC_JWT_SECRET` / `LC_DB_PASSWORD` 逐项 WARN。判定方式是"环境变量是否设置"而非比对默认串,因此源码里不必再放一份默认密钥;日志**只报变量名不打印任何值**。
- `backend/.env.local`(不入库,`git check-ignore` 已验证)承载真实 key/base-url/model。
- 实测确认 DeepSeek 的 `/chat/completions` 与 `/v1/chat/completions` 两条路径都存在,因此**不需要**覆盖 `spring.ai.openai.chat.completions-path`。

## 3. 本次核验结果(2026-10-11)

本仓库此前只有 `BackendApplicationTests` 一个上下文加载测试,本次新增 18 个用例:

```
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0   (mvn -B clean test)
  QuestionContentValidatorTest  11  五道校验逐条 + 合法通过 + 作文/语法填空特例
  AiGenerateServiceGateTest      3  脏结构不落库、重试两次、合格件 content 不含答案
  AiGradingServiceTest           4  漏答补行、多答忽略、客观题与作文封顶
  BackendApplicationTests        1  上下文加载
```

运行期实测(后端 8081 + 前端 5174 代理链路,MySQL 3306):

| 验收点 | 实测 |
|---|---|
| 启动自检 | `WARN RuntimeSafetyReporter: 环境变量 LC_JWT_SECRET 未设置 —— 正在使用 application.yml 内置默认值`(LC_DB_PASSWORD 同) |
| `POST /api/practice/generate {"type":"cloze"}` | `{"code":500,"message":"AI 服务暂时不可用"}` |
| 闸门是否拦住脏数据 | `SELECT COUNT(*) FROM question WHERE id LIKE '%-ai-%'` 调用前后都是 **0** |
| 重试 | 日志出现「AI 出题第 1 次尝试失败」「第 2 次尝试失败」 |
| key 可用性 | DeepSeek 三条端点(`/models`、`/chat/completions`、`/v1/chat/completions`)一律 **HTTP 401 `Your api key: ****5eca is invalid`** —— 用户提供的 key 无效,已如实记录,未伪造成功 |

`/api/practice/generate` 用 `?type=cloze` 查询串会返回「请求体格式错误」:该接口是 `@RequestBody GenerateRequest`,必须发 JSON body(非缺陷,记录以免误判)。

## 4. 你如何复现验证

```bash
# 1) 填真实 key(该文件不入库)
#    backend/.env.local:
#      LC_AI_API_KEY=sk-<你的可用 key>
#      LC_AI_BASE_URL=https://api.deepseek.com
#      LC_AI_MODEL=deepseek-chat

# 2) 带环境变量启动后端(端口 8081,因本机 8080 被 theca 占用)
cd backend && set -a && . ./.env.local && set +a \
  && ./mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# 3) 离线核验(不需要 key,走桩测试)
./mvnw.cmd -B clean test
```

key 可用后要补的真实验证(批次二,尚未完成):五种题型各生成 1 题并检查 `content` 无答案键、`[[n]]` 与空位齐;作文 AI 评分返回四维;无答案客观题 AI 判分行数与作答一致;模考交卷后 `writing_graded_at` 被异步写入。

## 5. 踩坑记录

- **Jackson 3 的泛型返回破坏链式调用**:`ObjectNode.putArray(String)`、`withArray(String)`、`put(String,String)` 在 Jackson 3 里返回 `<T extends JsonNode> T`,类型由赋值目标推断。测试里写 `content.putArray("blanks").addObject()` 会编译成"在 ObjectNode 上找 addObject()"而失败;`ArrayNode blanks = content.putArray("blanks");` 先落显式类型变量才成立。值对象同理:`GradedWriting` 的总分访问器是 `score()` 而不是 `totalScore()`。
- **MyBatis-Plus 的 `insert` 有重载**:`verify(mapper, never()).insert(any())` 因推断不明确编译失败,须写 `any(Question.class)`。
- **提示词必须与校验器同口径**:五选五"干扰项"那句就是例子 —— 提示词让模型产出的结构如果会被自家校验拒绝,接通真 key 后表现为"永远出题失败",而根因在提示词。
- **闸门报错要换语义**:校验器抛的是 `code=400`(面向录入者改数据),但模型产出缺陷应当重试并让前端可重试,所以 `buildDraft` 捕获后改写成 `code=500` 的「AI 出题结果不合格:...」。

## 6. 边界与已知取舍

- AI 出题**仍默认 `status=1`**(用户确认"其余不变"):生成的题立刻进学生端。若希望管理员先过一眼,把 `AiGenerateService.buildAndPersist` 的 `setStatus(1)` 改为 0 即可,一行。
- 闸门的真实拦截力目前由桩测试证明;模型实际产出哪些畸形结构,要等可用 key 才能统计。
- 判分仍不落逐题分值与人工评语(`SubmitDetailDto` 只有 `id/userAnswer/correctAnswer/isCorrect/explanation`),这属于 A4 人工批改工作台的范畴。

## 7. 批次二:接入真实模型后的验证(2026-10-11)

换上可用 key 后,`backend/.env.local` 填 `LC_AI_API_KEY` / `LC_AI_BASE_URL=https://api.deepseek.com` / `LC_AI_MODEL=deepseek-flash`,重启后端(8081)。

`GET https://api.deepseek.com/models` 返回 **`deepseek-flash | deepseek-v4-pro`**,即 `deepseek-flash` 是真实存在的模型名。另需注意**它是推理型模型**:用 8 token 预算探一次会看到 `content` 为空而 `reasoning_tokens=8`(预算全被思考吃掉);放到 800 token 后 `content` 正常返回 JSON,推理另在 `reasoning_content`。本项目没设 `max_tokens`,所以走服务端默认上限,不受这个小预算影响。

| 入口 | 实测 | 结论 |
|---|---|---|
| AI 出题 · 五种题型 | 全部 `code=0`,耗时 6~26 秒/题;新行 `cloze-ai-mv2y0bwx`、`reading-ai-mv2y0vse`、`matching-ai-…`、`grammar-fill-ai-…`、`writing-ai-…` | 一次通过,**日志里 0 次重试**(闸门未被触发,说明产出结构合格) |
| 闸门对真实产出的核验 | 逐行 SQL 核对:`content` 中 `"answer"` / `"explanation"` / `"sampleAnswer"` 出现次数全为 **0**,答案只落在 `answer_key`;`content.blanks/questions` 条目数与 `answer_key` 条目数**逐行相等** | "答案不下发"不变量在 AI 路径上同样成立 |
| 作文 AI 评分 | demo 提交 `POST /api/practice/detail/writing/{id}/submit`(gradeMethod=ai)→ `status=graded method=ai score=7.5/15`,四维 `dimensions` 齐全、`sampleAnswer` 有、`overallComment` 有 | 我**故意答非所问**(题目要英语角志愿者,我写图书馆志愿者),模型准确指出「内容严重偏离题目…未说明申请理由…完全缺少请求面试和提供联系方式」→ 判分真的读了题目而不只是读语言 |
| 模考作文异步评分 | demo 交卷 `exam-2026` → `partially_graded`(客观 48/100),约 15 秒后日志「模考作文 AI 评分完成: submissionId=1 score=7.0」,库行变 `graded` 且 `writing_graded_at` 非空、`estimated_time` 为 NULL;报告 `score=55/100`、分节得分与薄弱项正确 | 异步链路通 |
| 无答案客观题 AI 判分(`gradeObjective`) | **当前无法从任何写入口触发**:`AdminQuestionService` 与 `AiGenerateService` 都把 `has_answer` 设为 `!(type=='writing')`,种子里 `has_answer=0` 也只有写作题 | 该分支今天是**事实上的死代码**,只在单测里验证过;要让它可用,需要允许录入"无可比对答案的客观题"(产品决策,未擅自改) |

**发现的新缺口(交给 A4-② 一起做)**:`ExamReportDto` 只有 `writingScore`/`writingGraded`,**不含 `dimensions` 与 `overallComment`** —— 即使 AI 已给出逐维评语,学生成绩报告也拿不到数据。这不是"前端忘了渲染",而是后端没下发。

本批产生的数据:5 道 AI 题(均 `status=1` 上架)、`practice_submission` 新增 id=4(写作,已批改)、`exam_submission` 新增 id=1(已批改,55/100)。种子里那条 `id=2` 的待批改写作件**未被触碰**,仍留给后台批改验收。

待办更新:①批次二验证 ✅(本节);②A4 人工批改工作台 ✅;③`ExamService.report` 越权读 ✅(见 A4 文档第 6 节)。
