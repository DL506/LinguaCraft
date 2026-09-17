// 路由表(任务书第 4 章全量注册)
// 公开页(登录/注册/忘记密码/重置密码)由 BlankLayout 包裹,meta.public 供守卫放行
// 业务页按布局嵌套在 AppLayout 下;重做跳转用 query 传参,不新增动态路由(任务书第 4 章)
import type { RouteRecordRaw } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题(守卫写入 document.title) */
    title?: string
    /** 所属一级导航:home/practice/learn/user(任务书 6.3 高亮规则) */
    section?: string
    /** 是否支持沉浸模式(答题页标记:头部显示手动开启按钮,不再默认沉浸,见 useImmersive) */
    immersive?: boolean
    /** 是否公开页(免登录,守卫放行) */
    public?: boolean
  }
}

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      {
        path: '',
        name: 'login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录', public: true },
      },
    ],
  },
  {
    path: '/register',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      {
        path: '',
        name: 'register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '注册', public: true },
      },
    ],
  },
  {
    path: '/forgot-password',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      {
        path: '',
        name: 'forgot-password',
        component: () => import('@/views/ForgotPassword.vue'),
        meta: { title: '忘记密码', public: true },
      },
    ],
  },
  {
    path: '/reset-password',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      {
        path: '',
        name: 'reset-password',
        component: () => import('@/views/ResetPassword.vue'),
        meta: { title: '重置密码', public: true },
      },
    ],
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', section: 'home' },
      },

      { path: 'practice/reading', component: () => import('@/views/practice/Reading.vue'), meta: { title: '阅读理解', section: 'practice', immersive: true } },
      { path: 'practice/matching', component: () => import('@/views/practice/Matching.vue'), meta: { title: '五选五', section: 'practice', immersive: true } },
      { path: 'practice/cloze', component: () => import('@/views/practice/Cloze.vue'), meta: { title: '完形填空', section: 'practice', immersive: true } },
      { path: 'practice/grammar-fill', component: () => import('@/views/practice/GrammarFill.vue'), meta: { title: '语法填空', section: 'practice', immersive: true } },
      { path: 'practice/writing', component: () => import('@/views/practice/Writing.vue'), meta: { title: '作文', section: 'practice', immersive: true } },

      { path: 'exam', component: () => import('@/views/exam/ExamList.vue'), meta: { title: '真题模考', section: 'practice' } },
      { path: 'exam/:id', component: () => import('@/views/exam/ExamDetail.vue'), meta: { title: '模考', section: 'practice', immersive: true } },

      { path: 'learn/vocab', component: () => import('@/views/learn/Vocab.vue'), meta: { title: '背单词', section: 'learn' } },
      { path: 'learn/grammar', component: () => import('@/views/learn/Grammar.vue'), meta: { title: '学语法', section: 'learn' } },

      // 个人中心:路径与任务书第 4 章一致,UserCenter 作为共享布局(资料卡 + Tab)包裹子页
      {
        path: 'user',
        component: () => import('@/views/user/UserCenter.vue'),
        redirect: '/user/stats',
        meta: { section: 'user' },
        children: [
          { path: 'stats', component: () => import('@/views/user/Stats.vue'), meta: { title: '学习统计', section: 'user' } },
          { path: 'submissions', component: () => import('@/views/user/Submissions.vue'), meta: { title: '我的提交', section: 'user' } },
          { path: 'books', component: () => import('@/views/user/Books.vue'), meta: { title: '我的词书', section: 'user' } },
          { path: 'mistakes', component: () => import('@/views/user/Mistakes.vue'), meta: { title: '错题本', section: 'user' } },
          { path: 'favorites', component: () => import('@/views/user/Favorites.vue'), meta: { title: '收藏', section: 'user' } },
          { path: 'settings', component: () => import('@/views/user/Settings.vue'), meta: { title: '设置', section: 'user' } },
        ],
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]