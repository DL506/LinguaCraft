# LinguaCraft

广东专插本公共英语备考工具 —— 前后端单仓库(monorepo)。

## 结构

- `frontend/` 前端(Vue 3 + Vite + TS + Naive UI,PWA;开发期含 MSW mock,详见 frontend/README.md)
- `backend/` 后端(Spring Boot 4 + Spring AI + MyBatis-Plus + MySQL;启动/环境变量/SQL 初始化详见 backend/README.md)
- `任务书-r1/r2/r3.md` 项目规格文档(r3 为终检版)
- `交接与后续计划.md` **当前进度快照、已验证事实与 A8–A11 待实现计划**(换人/换会话先读这份)

## 后端快速开始(详细见 backend/README.md)

1. 启动本地 MySQL 8.4.7(程序与数据都在 `C:\Users\ASUS\mysql-8.4.7-winx64`,进 `bin` 执行 `.\mysqld.exe --defaults-file=C:/Users/ASUS/mysql-8.4.7-winx64/my.ini --console`;Windows 服务 `MySQL` 默认未启用且其路径指向旧的 D 盘位置,不要用它。起后用 `SELECT @@datadir;` 确认为 `C:\Users\ASUS\mysql-8.4.7-winx64\Data\`);
2. 按顺序执行建库与种子脚本(全量清单与命令见 `backend/src/main/resources/db/README.md`);
3. 启动后端:`cd backend` → `.\mvnw.cmd spring-boot:run`(端口 8080;Swagger UI `/swagger-ui.html`);
4. 账号:学生端 `demo / 123456`;后台管理端 `admin / 123456`(登录后访问 `/admin`,仅管理员可进入;`/api/admin/**` 后端同时做 403 校验)。

AI 能力(AI 出题/作文评分)需配置 `LC_AI_API_KEY / LC_AI_BASE_URL / LC_AI_MODEL`;未配置时按降级路径运行(作文转人工 pending、出题返回可重试错误)。

## 前端快速开始

```bash
cd frontend
npm install
npm run dev      # 开发服务器(默认启用 MSW mock,演示账号 demo / 123456)
npm run build    # 生产构建(类型检查 + 打包,含 PWA)
```

前后端联调:后端与 MySQL 就绪后,在 `frontend/.env.development.local` 写入 `VITE_USE_MOCK=false` 并重启 `npm run dev`;接口请求经 vite 代理(`/api` → `http://localhost:8080`)直达后端。

## 手动验证方法

每个开发步骤的手动验证方法(接口操作步骤 + SQL 抽查 + 预期结果)集中在 `backend/docs/verification/`(索引见该目录 README);全量验收对照清单一节见 `backend/docs/verification/S8-联调收尾.md`。