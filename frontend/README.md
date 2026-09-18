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

## 演示账号

- 用户名:`demo` / 密码:`123456`(mock 内存态,刷新后数据重置;注册接口可创建新用户)

## Mock 说明

- 开发环境 `VITE_USE_MOCK=true` 时启用 MSW(`public/mockServiceWorker.js` 由 `npx msw init public/` 生成,勿手写,见任务书 13.1)。
- MSW 与 PWA Service Worker 互斥:开发走 dev server(仅 MSW),生产构建仅 PWA。
- 真实题目/词库数据由后端契约提供,前端 mock 仅为联调用演示数据。

## 结构速览

- `src/api/` 接口封装(统一响应解包、401 拦截、错误提示)
- `src/mocks/` MSW handler 与 Full 版演示数据(GET 题目经 stripAnswer 过滤,答案仅 submit 下发)
- `src/stores/` app(双主题,localStorage 权威源)/ user / vocab / grammar / practice
- `src/views/` 页面:认证、首页、五种题型答题、真题模考、背单词、学语法、个人中心
- 三端断点:`<768` 手机 / `768-1023` 平板 / `≥1024` 桌面;答题页沉浸模式手动开启