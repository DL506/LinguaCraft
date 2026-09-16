# LinguaCraft

广东专插本公共英语备考工具(纯用户前端,MVP 第一步)。规格依据:《任务书-r3.md》(v6)。

## 环境要求

- Node.js ≥ 20.19(建议 22+)
- npm(不使用 pnpm/yarn/bun)

## 常用命令

| 命令 | 说明 |
|---|---|
| `npm install` | 安装依赖 |
| `npm run dev` | 启动开发服务器(默认启用 MSW mock,见 .env.development) |
| `npm run type-check` | TypeScript 类型检查(vue-tsc) |
| `npm run lint` | ESLint 检查 |
| `npm run build` | 生产构建(类型检查 + 打包) |
| `npm run preview` | 预览生产构建 |

## Mock 说明

- 开发环境 `VITE_USE_MOCK=true` 时启用 MSW(`public/mockServiceWorker.js` 由 `npx msw init public/` 生成,勿手写,见任务书 13.1)。
- MSW 与 PWA Service Worker 互斥:开发走 dev server(仅 MSW),生产构建仅 PWA。