# S9 Jackson 3 迁移与启动缺陷修复(手动验证方法)

> 本轮不改数据库、不改前端、不改接口路径与字段契约;仅修复运行期缺陷。
> 触发场景:首次以 `.\mvnw.cmd spring-boot:run` 启动后端即失败(`APPLICATION FAILED TO START`)。

## 一、现象与根因(共 4 项,均由真实启动暴露)

| 编号 | 现象 | 根因 |
|---|---|---|
| 缺陷 1 | 启动报 `Parameter 1 of constructor in ai.AiChatService required a bean of type 'com.fasterxml.jackson.databind.ObjectMapper' that could not be found` | Spring Boot 4.1.1 默认 JSON 栈是 **Jackson 3**(`tools.jackson.core:jackson-databind:3.1.5`,由 `spring-boot-starter-jackson`/`spring-boot-jackson` 自动配置,容器注册 `JsonMapper`)。代码却注入 **Jackson 2** 的 `ObjectMapper`;Jackson 2(2.21.5)只是被 `jjwt-jackson`、`spring-ai`/`openai-java-core`、`victools` 传递进 classpath 才编译通过,容器里没有对应 Bean,所有注入该类型的 Bean 全部创建失败 |
| 缺陷 2 | (无报错,但契约偏差)`id`/`submissionId` 以数字下发 | `config/JacksonConfig` 声明的是 Jackson 2 的 `com.fasterxml.jackson.databind.Module` Bean。Boot 4 的 Jackson 自动配置只收集 `tools.jackson.databind.JacksonModule`(已核对 `spring-boot-jackson-4.1.1` 字节码引用),该 Bean 被**静默忽略**,Long→String 全局规则从未生效。而前端 `types/*.d.ts` 要求 `id: string`、`submissionId: string` |
| 缺陷 3 | 启动成功后,定时任务首次执行即抛 `java.lang.Error: Unresolved compilation problem: The method fields() is undefined for the type JsonNode`(`PracticeService.toAnswerMap`) | Jackson 3 把 `JsonNode.fields()`(返回 `Iterator`)改名为 `properties()`(返回 `Set<Entry<String,JsonNode>>`)。另外该 `.class` 是 IDE 的 ECJ 编译器写进 `target/classes` 的"带错误占位类",导致 `mvnw compile` 误判"up to date"跳过重编译 |
| 缺陷 4 | `GET /v3/api-docs` 返回统一错误体 `{"code":500,...}`,swagger-ui 打开后接口列表为空 | springdoc 3.1.0 依赖 `swagger-core-jakarta:2.2.52`,其 `AnnotationsUtils` 会调用新版注解才有的 `Schema.$dynamicRef()`;而 spring-ai 传递进来的旧版注解包(`swagger-annotations:2.2.31` 来自 `com.openai:openai-java-core`、`swagger-annotations-jakarta:2.2.38` 来自 `spring-ai-client-chat`)同包同名类优先被加载 → `NoSuchMethodError` |

## 二、改动清单

1. **Jackson 2 → Jackson 3 包名迁移(20 个文件,只改 import,方法调用不变)**
   - `ai/`:`AiChatService`、`AiGenerateService`、`AiGradingService`、`ExamAsyncGrader`、`PromptTemplates`
   - `service/`:`PracticeService`、`ExamService`、`UserService`、`VocabService`
   - `config/JacksonConfig`、`common/RestAuthEntryPoint`
   - `dto/exam/`:`ExamSubmitRequest`、`ExamProgressRequest`、`ExamProgressResponse`、`ExamSectionDto`
   - `dto/practice/`:`PracticeItemResponse`、`PracticeSubmitRequest`、`ProgressRequest`、`ProgressResponse`、`SourceDto`

   映射关系:`com.fasterxml.jackson.databind.{ObjectMapper,JsonNode,node.ObjectNode,node.ArrayNode,module.SimpleModule,ser.std.ToStringSerializer}` 与 `com.fasterxml.jackson.core.type.TypeReference` → `tools.jackson.*` 对应类;`com.fasterxml.jackson.databind.Module` → `tools.jackson.databind.JacksonModule`。
   **保持不动**:`com.fasterxml.jackson.annotation.*`(`@JsonInclude`/`@JsonFormat`)—— Jackson 3 继续复用 jackson-annotations 2.21 同包名。
2. **`fields()` → `properties()`(3 处)**:`PracticeService.toAnswerMap`、`ExamService.parseSectionScores`、`ExamService.toAnswerMap`,由 `x.fields().forEachRemaining(e -> ...)` 改为 `x.properties().forEach(e -> ...)`。
3. **`config/JacksonConfig`**:Bean 返回类型改为 `JacksonModule`,注释补记"Boot 4 只收集 `JacksonModule` Bean,误用 Jackson 2 `Module` 会被静默忽略"。
4. **`pom.xml`**:`<properties>` 新增 `swagger.version=2.2.52`;`<dependencyManagement>` 强制对齐 `io.swagger.core.v3:swagger-annotations` 与 `swagger-annotations-jakarta` 到 2.2.52(附注释说明冲突来源)。

无数据库变更(S9 无脚本),无前端变更。

## 三、本次修复的实测结果(执行时间 2026-09-28)

- `mvnw clean compile` → `BUILD SUCCESS`(必须带 `clean`,见缺陷 3 的 ECJ 占位类问题)。
- 启动:`Tomcat started on port 8080` → `Started BackendApplication in 11.247 seconds`,无 `UnsatisfiedDependencyException`;定时任务 `PendingGradingJob` 首轮执行无异常。
- 只读接口全链路(统一响应 `code=0`):

  | 接口 | 结果 | 关键校验 |
  |---|---|---|
  | `GET /api/system/ping` | code=0 | 免登录放行 |
  | `POST /api/auth/login`(demo/123456) | code=0 | token 长度 170;`user.id` 类型 **String** |
  | `GET /api/user/profile` | code=0 | 原始响应含 `"id":"1"`(Long→String 生效) |
  | `GET /api/user/settings` / `stats` / `submissions` / `mistakes` / `favorites` | code=0 | `stats.totalStudyTime=37`、`pendingGradingCount=1`(与 S06 演示数据一致) |
  | `GET /api/practice/list?type=cloze` | code=0 | `total=2`,`firstId=cloze-real-2023-1` 为 String;响应无 `"answer"` |
  | `GET /api/practice/detail/cloze/cloze-real-2023-1` | code=0 | `content.blanks=10`、`options=4`(JsonNode 结构完整);无 answer/explanation |
  | `GET /api/practice/detail/.../progress` | code=0 | `data=null`(未作答,契约允许) |
  | `GET /api/exam/list` / `GET /api/exam/exam-2026` | code=0 | `sections=5`、阅读 `content` 长度 3;无 answer/explanation/sampleAnswer |
  | `GET /api/exam/exam-2026/progress` | code=0 | 无进度时 data 为 null |
  | `GET /api/vocab/books` / `book/{id}/words` / `task/today` / `task/due` | code=0 | `bookId=book-syllabus` 为 String |
  | `GET /api/grammar/tree` | code=0 | 根节点 6 个 |
  | 无 token 访问 `GET /api/user/profile` | **HTTP 401** + `{"code":401,"data":null,"message":"未登录或登录已过期"}` | 401 统一响应体正常 |
  | `GET /v3/api-docs` | openapi `3.1.0`,paths **32**,schemas **72**,tags **7** | 32 条路径模板承载 36 个映射(4 条各有两个方法:`/api/user/settings` GET+PUT、`/api/user/favorites` GET+POST、`/api/practice/detail/{type}/{id}/progress` GET+PUT、`/api/exam/{id}/progress` GET+PUT) |
  | `GET /swagger-ui/index.html` | HTTP 200 | 文档可渲染 |

## 四、你如何复现验证

```powershell
# 1) 必须 clean(避免 IDE 的 ECJ 占位类混入 target/classes)
cd backend
.\mvnw.cmd clean compile          # 期望 BUILD SUCCESS
.\mvnw.cmd spring-boot:run        # 期望日志出现 Started BackendApplication

# 2) 另开终端:探活 + 登录 + 验证 id 序列化为字符串
curl.exe -s http://127.0.0.1:8080/api/system/ping
$login = Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/auth/login `
  -ContentType 'application/json' -Body '{"username":"demo","password":"123456"}'
$h = @{ Authorization = "Bearer $($login.data.token)" }
(Invoke-WebRequest -Uri http://127.0.0.1:8080/api/user/profile -Headers $h -UseBasicParsing).Content
# 期望:{"code":0,...,"data":{"id":"1",...}}   <- id 带引号

# 3) 答案不下发 + 文档完整性
((Invoke-WebRequest -Uri http://127.0.0.1:8080/api/exam/exam-2026 -Headers $h -UseBasicParsing).Content) -match '"answer"|"sampleAnswer"'   # 期望 False
(Invoke-RestMethod http://127.0.0.1:8080/v3/api-docs).paths.PSObject.Properties.Name.Count   # 期望 32
```

SQL 侧抽查(可选,确认迁移未影响 JSON 列读取):

```powershell
$m = "D:\GraduationProject\mysql-8.4.7-winx64\bin\mysql.exe"
& $m --host=127.0.0.1 --port=3306 --user=root --password=<你的密码> linguacraft --execute="SELECT COUNT(*) leak FROM question WHERE JSON_CONTAINS_PATH(content,'one','$.blanks[0].answer')=1"
# 期望 0
```

## 五、遗留注意事项

- **构建习惯**:本项目 `target/classes` 可能被 IDE(Eclipse 编译器)写入"带编译错误的占位类",出现"编译通过但运行报 `Unresolved compilation problem`"时,一律用 `.\mvnw.cmd clean compile` 复核。
- **依赖纪律**:后续新增依赖若带入 `io.swagger.core.v3:swagger-annotations*` 或 Jackson 2 的 databind,需确认与 `swagger.version` / Boot 4 Jackson 3 不冲突(见 `pom.xml` 注释)。
- **AI 相关接口**(`/api/practice/generate`、`/api/practice/smart`、作文 AI 评分)本轮未联网实测:需配置 `OPENAI_API_KEY` 后按 S4 文档验证;未配置时的降级路径已在 S4/S5 文档说明。
- 首页两接口(`/api/home/today-tasks`、`/api/home/exam-countdown`)仍缺失,属独立待办(见全量检查报告),不在本轮范围。
