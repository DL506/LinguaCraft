// 后台管理端 API 封装(阶段 A1 起):全部走 /api/admin/**
// 说明:管理端接口刻意不注册 MSW handler —— main.ts 配置 onUnhandledRequest: 'bypass',
//       未匹配路径直连真实后端,因此后台功能只在接真实后端时可用(演示 mock 模式下面向的是学生端)。
import { del, get, patch, post, put } from './request'
import type {
  AdminPing,
  AdminQuestionDetail,
  AdminQuestionListItem,
  AdminQuestionPayload,
} from '@/types/admin'
import type { PageResult } from '@/types/api'
import type { PracticeType } from '@/types/practice'

/** 权限探针:验证管理员令牌能打通 /api/admin/**(学生令牌会得到 code 403) */
export function getAdminPing(): Promise<AdminPing> {
  return get<AdminPing>('/admin/ping')
}

/** 题库分页列表(管理端视角:含下架题与引用数) */
export function getAdminQuestions(
  params: { type?: PracticeType; sourceType?: string; status?: number; keyword?: string; page?: number; size?: number } = {}
): Promise<PageResult<AdminQuestionListItem>> {
  const query = new URLSearchParams()
  if (params.type) query.set('type', params.type)
  if (params.sourceType) query.set('sourceType', params.sourceType)
  if (params.status !== undefined && params.status !== null) query.set('status', String(params.status))
  if (params.keyword) query.set('keyword', params.keyword)
  query.set('page', String(params.page ?? 1))
  query.set('size', String(params.size ?? 20))
  return get<PageResult<AdminQuestionListItem>>(`/admin/questions?${query.toString()}`)
}

/** 题目详情(含 answer_key) */
export function getAdminQuestion(id: string): Promise<AdminQuestionDetail> {
  return get<AdminQuestionDetail>(`/admin/questions/${encodeURIComponent(id)}`)
}

/** 新建题目(服务端会做五道结构校验,失败返回 code 400 与定位路径) */
export function createAdminQuestion(payload: AdminQuestionPayload): Promise<AdminQuestionDetail> {
  return post<AdminQuestionDetail>('/admin/questions', payload)
}

/** 更新题目(整条覆盖 content 与 answer_key;题型不可变) */
export function updateAdminQuestion(id: string, payload: AdminQuestionPayload): Promise<AdminQuestionDetail> {
  return put<AdminQuestionDetail>(`/admin/questions/${encodeURIComponent(id)}`, payload)
}

/** 上架/下架 */
export function setAdminQuestionStatus(id: string, status: 0 | 1): Promise<null> {
  return patch<null>(`/admin/questions/${encodeURIComponent(id)}/status?status=${status}`)
}

/** 删除题目(被提交或错题引用时服务端拒绝) */
export function deleteAdminQuestion(id: string): Promise<null> {
  return del<null>(`/admin/questions/${encodeURIComponent(id)}`)
}
