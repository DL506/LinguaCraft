# LinguaCraft 后端(Spring Boot 4 + Spring AI + MyBatis-Plus + MySQL)

## 环境要求

- JDK 21
- 本地 MySQL 8.4.7(默认连接 `127.0.0.1:3306/linguacraft`,`root`;可用环境变量覆盖)
  - 程序目录 `C:\Users\ASUS\mysql-8.4.7-winx64`;其 `my.ini` 的 `basedir`/`datadir` 指向**同盘的 `C:\Users\ASUS\mysql-8.4.7-winx64\Data`,因此数据目录(含 linguacraft 库)在 C 盘**(2026-10-12 从 D 盘整目录搬迁而来;D 盘那份快照与 C 盘迁移前旧快照均保留未删,回退只需对调 `my.ini` 里的注释段)。启动须带 `--defaults-file=C:/Users/ASUS/mysql-8.4.7-winx64/my.ini`,起后用 `SELECT @@datadir;` 自证;Windows 服务 `MySQL` 的 PathName 仍指向 D 盘 exe,**不要用它启动**
  - Windows 服务 `MySQL` 为 Stopped/Disabled,进 `bin` 直接拉起即可:`.\mysqld.exe --console`
- 使用仓库自带 Maven wrapper(`mvnw.cmd`),无需单独安装 Maven

## 数据库初始化(从零)

脚本目录 `src/main/resources/db/`,**只增不改**、按序号执行(索引表与执行命令见该目录 `README.md`)。

执行顺序:

```
V01 → V02 → S01 → V03 → S02 → V05 → S03 → V06 → S04 → S05 → V07 → S06 → V08 → S07
```

PowerShell 示例(必须使用长选项,短选项 `-h` 在 PowerShell 下会被错误解析):

```powershell
# 建库(库尚不存在时不要附加 --database)
Get-Content .\backend\src\main\resources\db\V01__create_database.sql -Raw |
  mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4

# 其余脚本(追加库名 linguacraft)
Get-Content .\backend\src\main\resources\db\V02__create_user_auth_tables.sql -Raw |
  mysql --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> --default-character-set=utf8mb4 linguacraft
```

## 启动

```powershell
cd backend
.\mvnw.cmd spring-boot:run      # 端口 8080
```

- Swagger UI:`http://localhost:8080/swagger-ui.html`
- 健康检查:`http://localhost:8080/actuator/health`
- 演示账号:`demo / 123456`(S01 种子;**重复执行 S01 会把 demo 重置为初始状态**)
- 后台管理员:`admin / 123456`(S07 种子;仅能登录后台 `/api/admin/**`,学生端接口也可用。**本地演示口令,对外环境必须改密**)

## 环境变量(均可选,覆盖 application.yml 默认值)

| 变量 | 说明 | 默认 |
|---|---|---|
| LC_DB_USERNAME / LC_DB_PASSWORD | 数据库账号/密码 | root / 本地默认密码 |
| LC_JWT_SECRET | JWT 签名密钥(生产必改) | 开发默认值 |
| LC_AI_API_KEY / LC_AI_BASE_URL / LC_AI_MODEL | AI 服务(OpenAI 兼容) | 假占位(调用失败时走降级) |
| LC_MAIL_HOST / LC_MAIL_PORT / LC_MAIL_USERNAME / LC_MAIL_PASSWORD | SMTP(未配置时重置链接输出到后端日志) | 未配置 |
| LC_RESET_BASE_URL | 重置密码邮件链接的前端地址 | http://localhost:5173/reset-password |

业务配置(application.yml):`lc.grading.manual-delay-seconds`(人工批改延迟阈值,默认 60 秒)、`lc.grading.scan-interval-ms`(待批改扫描间隔,默认 30 秒)、`lc.ai.timeout-seconds`(AI 调用超时,默认 60 秒)、`lc.exam.date/progress`(首页倒计时)。

## 关键约定(与前端契约一致)

- 统一响应 `{code,message,data}`;认证 `Authorization: Bearer <token>`;401 → 前端清 token 跳登录;
- **答案不下发**:GET 题目/试卷接口不含 answer/explanation;`details` 仅 graded 下发;pending 返回 estimatedTime;
- 批改状态机:AI 模式即时出分;人工模式先 pending、约 1 分钟后自动流转;写作 AI 全权评分,失败降级转人工;
- 断点续答(扩展契约):练习 E1 `/api/practice/detail/{type}/{id}/progress`、模考 E2 `/api/exam/{id}/progress`(前端已对接,提交/交卷后自动清除;MSW 侧已补同名 handler);
- 查词(扩展契约 E3):`GET /api/vocab/lookup?word=`,大小写无关且自动剥离标点,不做词形归并;未收录返回 `code=0` 且 `data=null`;
- 首页:`GET /api/home/today-tasks` 的 done 与 `/api/user/stats` 同用 `created_at` 口径,背单词完成数直接复用 `VocabService.todayTask()`;`GET /api/home/exam-countdown` 取配置 `lc.exam.date`/`lc.exam.progress`。
- 统计口径:`/api/user/stats` 的 `typeAccuracy.grammar` 映射 `grammar-fill`,`sourceAccuracy` 无数据返回 -1。
- 权限(阶段 A1 起):`user.role` 单角色模型(student/admin),JWT 携带 role 声明并映射为 `ROLE_ADMIN`/`ROLE_STUDENT`;`/api/admin/**` 要求管理员角色。
  未登录 → HTTP 401 + `code:401`(前端清 token 跳登录);已登录但角色不足 → HTTP 403 + `code:403`(仅提示,不清 token);
  注册链路强制 `role=student`,停用(`enabled=0`)账号在登录环节被拒。
- 后台题库(阶段 A2 起):`/api/admin/questions` 提供列表/详情/新建/更新/上下架/删除六个接口,**只有详情接口返回 `answerKey`**。
  写入前由共享组件 **`service/QuestionContentValidator`** 做五道校验(结构、`content` 递归禁答案类键、`content` 与 `answer_key` 编号集合相等、客观题答案落在候选内、`passage` 的 `[[n]]` 与空位编号一致),
  任一不过返回 `code:400` 且 `message` 带定位路径;`content.id/type/source` 与分值(`PracticeRules` 按题型默认映射)一律由服务端覆写,题型创建后不可变更,删除带提交/错题引用保护。
  CORS 的 `allowedMethods` 在 `WebConfig` 与 `SecurityConfig` **两处都要含 PATCH**。背景见 `docs/verification/A2-后台题库管理.md`。
- 录入表单权威性(阶段 A8):前端录入页**以表单为权威**提交"库里原始 `content`/`answer_key` 打底 + 表单覆盖",因此服务端必须**原样保留请求里它没建模的键**(自定义键、将来的校核标记等),只覆写 `id/type/source` 三个权威字段;这条契约由 `AdminQuestionServiceTest` 锁住(含"请求不带 `sourceMeta` 时沿用库值,且 `content.source` 与 `source_meta` 同源")。服务端不做键白名单 —— 白名单会让任何新增键被静默洗掉。背景见 `docs/verification/A8-题目录入表单权威化.md`。
- AI(阶段 A3 起):出题与判分统一经 `AiChatService`(超时 `lc.ai.timeout-seconds`,失败即 `code=500` 可重试);
  **AI 出题与后台录入共用 `QuestionContentValidator`**,校验不过不落库,因此不会出现"AI 静默写入脏题"。
  端点与密钥经环境变量注入(`LC_AI_API_KEY`/`LC_AI_BASE_URL`/`LC_AI_MODEL`,样例 `backend/.env.example`,本地实值放不入库的 `backend/.env.local`);
  未注入时应用仍启动,但 `config/RuntimeSafetyReporter` 会逐项 WARN,且四个 AI 入口走降级(作文与无答案题转待批改)。背景见 `docs/verification/A3-AI链路加固与出题闸门.md`。
- 人工批改(阶段 A4 起):`/api/admin/submissions` 提供队列/材料/判分三接口。待批改件分两种 —— `grading_method=auto` 由 `PendingGradingJob` 到期自动比对出分,**`manual`(写作题与 AI 失败件)只能由本接口判完**。
  判分落库必须一次写齐 `status='graded'`、`grading_method='manual'`、`score/correct_count/total_count`、`details` 或 `writing_result`、`graded_at`,并**置空 `estimated_time`**;
  更新带 `status='pending'` 的 CAS 条件,避免与定时任务互相覆盖。背景见 `docs/verification/A4-人工批改工作台.md`。
- 模考作文判分(阶段 A4-②):`/api/admin/exam-submissions` 队列/材料/`POST /{id}/grade-writing`。只写写作列(`writing_score`/`writing_result`/`writing_graded_at`/`status='graded'`/`grading_method='manual'`,清空 `estimated_time`),客观分不动;
  CAS 用 `writing_graded_at IS NULL AND status<>'graded'`,与 `ExamAsyncGrader` 互斥。**写作满分取自试卷 writing section 的 score**,不是硬编码。
  两条人工批改路径的 `writing_result` 一律由 `service/WritingGradeSupport` 产出;成绩报告 `ExamReportDto` 现含 `writingDimensions`/`writingSampleAnswer`/`writingOverallComment`(此前只落库不下发)。
- **写作评分维度全项目统一为 4 项**:内容要点 / 语言表达 / 篇章结构 / 格式规范(2026-10-12 用户拍板,应用文的"格式"独立计分)。
  口径同时落在三处,改任何一处都要同步另两处,否则新装环境与本机分叉:`ai/PromptTemplates.java` 的写作出题提示词(要求恰好 4 项)、
  `db/S02`/`db/S03` 种子题面、`db/V10__normalize_writing_dimensions.sql`(老库补齐,幂等,只主题面**不动历史 `writing_result`**)。
  维度个数不参与分值计算(判分只校验各维度之和 ≤ 卷面总分,单维输入上限 `dimensionCap=100`),所以加减维度不影响任何已有分数。背景见 `docs/verification/A8前-写作评分维度统一.md`。
- 看板与用户管理(阶段 A5):`GET /api/admin/dashboard` 出全站计数,`/api/admin/users` 出列表/详情与 `PATCH .../status`、`PATCH .../role`。
  看板「今日」一律 `created_at >= 今日零点`(同 `HomeService` 与 `UserService.stats`,不用 `submitted_at`);`practicePending` 与学生端 `stats.pendingGradingCount` 同口径仅练习,模考欠账分列 `examPending` 与 `examWritingUngraded`;
  DTO 计数用**基本类型**,避开全局 `Long`→字符串规则。三条护栏:不许停用自己、不许给自己降权、必须保留至少一个管理员;停用/降权**不影响已签发令牌**(过滤器只读声明,最长 2 小时)。背景见 `docs/verification/A5-运营看板与用户管理.md`。
- 内容管理(阶段 A6):`/api/admin/vocab`(词书+单词+CSV 导入)、`/api/admin/grammar`(节点+章节)、`/api/admin/exam-papers`(真题卷+上下架)。
  **词书是过滤器不是归属**:`vocab_book.filter_level` 按等级筛词,没有中间表,所以"改词书"=改单词 `level`;`vocab_book.total` 不自动派生,只有 `PATCH /books/{id}/sync-total` 显式对齐(自动派生会让首页数字暴跌)。
  单词/词书/试卷的删除一律**引用保护**(命中学习记录、收藏、交卷即拒),新单词 ID 沿用 `w-` + 位宽补零,学生端列表按 `LENGTH(id), id` 排序;CSV 解析用自写 `CsvWordParser`(RFC 4180 状态机,不新增依赖),**行级失败跳过、整单级失败(空文件/表头缺列)400 拒绝**。背景见 `docs/verification/A6-内容与真题卷管理.md`。
- AI 预评(阶段 A7):`POST /api/admin/submissions/{id}/ai-preview` 与 `/api/admin/exam-submissions/{id}/ai-preview` 让 AI 起草写作件的四维分/范文/总评,前端回填到判分抽屉。**只读不写库**(尤其不碰 `writing_graded_at`,一写学生端就认为作文出分),满分取题面口径,建议分取**维度实算**而非 AI 自报合计;映射集中在 `WritingGradeSupport.toPreview`。前端覆盖前快照、给「撤销 AI 草稿」,并按维度名合并而不是整表替换。注意 `ExamService.report` 对 `partially_graded + ai` 有防重入兜底会补触发 AI 评分,演示用样本要做成 `manual` 才不会被读一次就消失。背景见 `docs/verification/A7-AI预评.md`。
- 真题卷上下架(阶段 A6-③):`exam_paper.status` 由 `db/V09` 加列,默认 1。学生侧**列表/详情/作答/进度**四处按 `status=1` 过滤并拒下架卷,**但 `buildReport` 不看状态** —— 下架是停止投放,不是抹掉历史成绩。写入前必过 `ExamPaperValidator` 四条硬校验(编号集合相等、编号全卷唯一、`sum(section.score)==total_score`、恰好一个 15 分写作部分),因为编号错配的后果是**客观题静默判错**而不是报错。
- **JSON 栈统一为 Jackson 3**(`tools.jackson.*`,Spring Boot 4 默认):实体主键 `Long` 由 `config/JacksonConfig` 的 `JacksonModule` Bean 全局序列化为字符串(对应前端 `id: string`)。不得再使用 `com.fasterxml.jackson.databind/core` 的类(`com.fasterxml.jackson.annotation` 注解包除外);swagger 注解包版本已在 `pom.xml` 固定为 2.2.52,勿降级。背景见 `docs/verification/S9-Jackson3启动缺陷修复.md`。

## 目录速览

- `src/main/java/com/linguacraft/backend/`:`config`(安全/异步/文档/序列化/CORS)、`common`(统一响应与异常)、`controller`、`service`、`mapper`、`entity`、`dto`、`ai`(Spring AI 出题与批改)、`job`(定时任务)、`util`
- `src/main/resources/db/`:数据库脚本(索引见其 README)
- `docs/verification/`:分步骤手动验证方法(索引见其 README)