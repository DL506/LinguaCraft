// 路由实例与全局守卫(任务书第 4 章:守卫只做两件事——未登录跳 /login;设置 document.title)
// 第 3 件事为后台新增(阶段 A1):meta.adminOnly 页需校验管理员角色
// 沉浸模式的 body class 由 useImmersive 统一管理
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { getToken } from '@/utils/storage'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  // 未登录且目标非公开页 → 跳登录
  if (!to.meta.public && !getToken() && to.path !== '/login') {
    return { path: '/login' }
  }
  // 后台页:资料未加载先补拉(刷新页面时 store 为空);非管理员弹回首页
  // 说明:这只是可用性拦截,真正的边界是后端 /api/admin/** 的 403,不可只依赖此处
  if (to.meta.adminOnly) {
    const userStore = useUserStore()
    if (!userStore.profile) await userStore.ensureProfile()
    if (!userStore.isAdmin) return { path: '/dashboard' }
  }
  // 设置文档标题
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} · LinguaCraft` : 'LinguaCraft · 广东专插本英语备考'
  return true
})

export default router