# LinguaCraft

广东专插本公共英语备考工具 —— 前后端单仓库(monorepo)。

## 结构

- `frontend/` 前端(Vue 3 + Vite + TS + Naive UI,PWA;开发期含 MSW mock,详见 frontend/README.md)
- `backend/` 后端(Spring Boot 工程,规划中)
- `任务书-r1/r2/r3.md` 项目规格文档(r3 为终检版)

## 快速开始(前端)

```bash
cd frontend
npm install
npm run dev      # 开发服务器(默认启用 MSW mock,演示账号 demo / 123456)
npm run build    # 生产构建(类型检查 + 打包,含 PWA)
```

## 后端(待初始化)

后端工程将在 `backend/` 目录初始化;接口契约基准:前端的 `frontend/src/types/` 请求/响应类型与 `frontend/src/mocks/handlers/` 行为逻辑(判分口径、批改阈值、答案不下发规则)。