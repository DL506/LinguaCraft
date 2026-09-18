// 路由实例与全局守卫(任务书第 4 章:守卫只做两件事——未登录跳 /login;设置 document.title)
// 沉浸模式的 body class 由 useImmersive 统一管理
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { getToken } from '@/utils/storage'

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  // 未登录且目标非公开页 → 跳登录
  if (!to.meta.public && !getToken() && to.path !== '/login') {
    return { path: '/login' }
  }
  // 设置文档标题
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} · LinguaCraft` : 'LinguaCraft · 广东专插本英语备考'
  return true
})

export default router