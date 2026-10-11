# 手动验证方法(git 留痕)

> 每次步骤完成后,本目录同步提交该步骤的"手动验证方法"(接口操作步骤 + SQL 抽查 + 预期结果)。
> 数据库脚本索引与执行命令见 `../../src/main/resources/db/README.md`。

| 步骤 | 文档 | 数据库脚本 |
|---|---|---|
| S1 工程基座 + S2 认证与用户设置 | [S1-S2-工程基座与认证.md](S1-S2-工程基座与认证.md) | V01 / V02 / S01 |
| S3 题库与刷题主链路 | [S3-题库与刷题主链路.md](S3-题库与刷题主链路.md) | V03 / S02 |
| S4 AI 能力(Spring AI) | [S4-AI能力.md](S4-AI能力.md) | 无结构变更 |
| S5 真题模考与成绩报告 | [S5-真题模考.md](S5-真题模考.md) | V05 / S03 |
| S6 背单词与学语法 | [S6-背单词与学语法.md](S6-背单词与学语法.md) | V06 / S04 / S05 |
| S7 个人中心与统计 | [S7-个人中心与统计.md](S7-个人中心与统计.md) | V07 / S06 |
| S8 联调收尾与全量验收 | [S8-联调收尾.md](S8-联调收尾.md) | (无) |
| S9 Jackson 3 迁移与启动缺陷修复 | [S9-Jackson3启动缺陷修复.md](S9-Jackson3启动缺陷修复.md) | (无) |
| A0 联调缺口修复(首页两接口 + E1/E2 mock + E3 查词) | [A0-联调缺口修复.md](A0-联调缺口修复.md) | (无) |
| A1 后台权限与账号基座 | [A1-后台权限与账号基座.md](A1-后台权限与账号基座.md) | V08 / S07 |
| A2 后台题库管理(题目 CRUD + 五道写入校验) | [A2-后台题库管理.md](A2-后台题库管理.md) | (无) |
| A3 AI 链路加固与出题闸门(五道校验抽为共享组件,AI 出题不落脏数据) | [A3-AI链路加固与出题闸门.md](A3-AI链路加固与出题闸门.md) | (无) |
| A4 人工批改工作台(练习判分 + A4-② 模考作文判分,统一到 WritingGradeSupport) | [A4-人工批改工作台.md](A4-人工批改工作台.md) | (无) |
| A5 运营看板与用户管理(计数口径纪律、停用与调角色护栏) | [A5-运营看板与用户管理.md](A5-运营看板与用户管理.md) | (无) |
| A6 内容全家桶(词书与单词 + CSV 导入 + 语法管理 + 真题卷上下架) | [A6-内容与真题卷管理.md](A6-内容与真题卷管理.md) | V09 |

## 通用前置

- 本地 MySQL 8.4.7 已启动。程序目录 `C:\Users\ASUS\mysql-8.4.7-winx64`(其 my.ini 的 basedir/datadir 仍指向 D 盘,数据在 D 盘);Windows 服务 `MySQL` 为 Stopped/Disabled,启动方式:进 `bin` 执行 `.\mysqld.exe --console`。
- 后端启动:`cd backend` → `.\mvnw.cmd spring-boot:run`(端口 8080)。
- AI 与密钥的本地注入:`backend/.env.local`(不入库,样例见 `backend/.env.example`)填 `LC_AI_API_KEY`/`LC_AI_BASE_URL`/`LC_AI_MODEL` 后,
  启动前需导出为环境变量(Git Bash:`set -a && . ./.env.local && set +a`);未注入时应用仍会启动,但日志逐项 WARN 并让 AI 接口走降级。
- 编译复核一律带 `clean`(`.\mvnw.cmd clean compile`):IDE 的 Eclipse 编译器可能把"带编译错误的占位类"写进 `target/classes`,普通增量编译会误判为最新(详见 S9 缺陷 3)。
- 前端启动:`cd frontend` → `npm run dev`(端口 5173)。
- 端口被本机其他项目占用时:`frontend/.env.development.local`(不入库)里设 `VITE_PORT=5174` 与 `VITE_PROXY_TARGET=http://localhost:8081`,
  后端相应以 `.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` 启动;`vite.config.ts` 会读这两个变量,无需改仓库配置。
- 联调关闭 MSW:`frontend/.env.development.local` 写入 `VITE_USE_MOCK=false`(该文件不入库)。
- 接口调试入口:Swagger UI `http://localhost:8080/swagger-ui.html`(先调登录接口,再把 token 填入 Authorize)。
- 演示账号:`demo / 123456`(重跑 `S01__seed_demo_user.sql` 可重置演示账号状态)。
- 后台管理员:`admin / 123456`(重跑 `S07__seed_admin_account.sql` 可重置为 admin/enabled=1)。