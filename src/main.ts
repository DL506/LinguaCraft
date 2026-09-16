// 应用入口:装配 Vue 应用、全局状态与路由
// MSW 启动(任务书 13.1):仅开发环境启用;顶层 await 依赖 tsconfig target ES2022 支持
if (import.meta.env.VITE_USE_MOCK === 'true') {
  const { worker } = await import('@/mocks/browser')
  await worker.start({ onUnhandledRequest: 'bypass' })
}

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 全局样式:加载顺序为 设计变量 → 基础重置 → 全局样式
import '@/assets/styles/variables.css'
import '@/assets/styles/reset.css'
import '@/assets/styles/global.css'

const app = createApp(App)
app.use(createPinia()) // Pinia 状态管理
app.use(router) // Vue Router 路由
app.mount('#app')