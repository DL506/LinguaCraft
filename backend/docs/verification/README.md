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

## 通用前置

- 本地 MySQL 8.4.7 已启动(Windows 服务 `MySQL`;若未启动需以管理员执行 `net start MySQL`)。
- 后端启动:`cd backend` → `.\mvnw.cmd spring-boot:run`(端口 8080)。
- 编译复核一律带 `clean`(`.\mvnw.cmd clean compile`):IDE 的 Eclipse 编译器可能把"带编译错误的占位类"写进 `target/classes`,普通增量编译会误判为最新(详见 S9 缺陷 3)。
- 前端启动:`cd frontend` → `npm run dev`(端口 5173)。
- 联调关闭 MSW:`frontend/.env.development.local` 写入 `VITE_USE_MOCK=false`(该文件不入库)。
- 接口调试入口:Swagger UI `http://localhost:8080/swagger-ui.html`(先调登录接口,再把 token 填入 Authorize)。
- 演示账号:`demo / 123456`(重跑 `S01__seed_demo_user.sql` 可重置演示账号状态)。