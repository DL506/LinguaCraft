// 后台管理端 API 封装(阶段 A1 起):全部走 /api/admin/**
// 说明:管理端接口刻意不注册 MSW handler —— main.ts 配置 onUnhandledRequest: 'bypass',
//       未匹配路径直连真实后端,因此后台功能只在接真实后端时可用(演示 mock 模式下面向的是学生端)。
import { get } from './request'
import type { AdminPing } from '@/types/admin'

/** 权限探针:验证管理员令牌能打通 /api/admin/**(学生令牌会得到 code 403) */
export function getAdminPing(): Promise<AdminPing> {
  return get<AdminPing>('/admin/ping')
}
