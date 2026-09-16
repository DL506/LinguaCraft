// 应用入口:装配 Vue 应用、全局状态与路由
// P1 阶段将在此追加 MSW 启动逻辑(任务书 13.1:VITE_USE_MOCK === 'true' 时顶层 await 启动 worker)
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