// MSW 浏览器端入口:聚合全部模块 handler(任务书 13.1)
// 由 main.ts 在 VITE_USE_MOCK === 'true' 时动态 import 并 start
import { setupWorker } from 'msw/browser'
import { authHandlers } from './handlers/auth'
import { homeHandlers } from './handlers/home'
import { practiceHandlers } from './handlers/practice'
import { examHandlers } from './handlers/exam'
import { vocabHandlers } from './handlers/vocab'
import { grammarHandlers } from './handlers/grammar'
import { userHandlers } from './handlers/user'

export const worker = setupWorker(
  ...authHandlers,
  ...homeHandlers,
  ...practiceHandlers,
  ...examHandlers,
  ...vocabHandlers,
  ...grammarHandlers,
  ...userHandlers
)