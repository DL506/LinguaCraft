// 路由实例:history 模式,路由表定义在 routes.ts(任务书第 4 章)
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router