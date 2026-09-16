// 请求封装:统一 baseURL、鉴权头、响应解包、401 处理与错误提示(任务书 12 章)
// 全局错误提示使用 naive-ui 离散 API,全局唯一实例;401 清 token 并跳登录页(验收 24/40)
import { createDiscreteApi } from 'naive-ui'
import { clearToken, getToken } from '@/utils/storage'
import type { ApiResponse } from '@/types/api'

const { message } = createDiscreteApi(['message'])

/** 接口统一前缀(任务书 12 章:Base URL = /api) */
const BASE_URL = '/api'

/** 401 统一处理:清 token、提示并跳登录页 */
function handleUnauthorized(): never {
  clearToken()
  message.error('登录已过期,请重新登录')
  window.location.href = '/login'
  throw new Error('unauthorized')
}

/**
 * 发起请求并解包 data:
 * - 自动携带 Authorization: Bearer <token>
 * - code === 0 返回 data;code !== 0 弹 n-message.error 并抛错(验收 40)
 * - code === 401 或 HTTP 401 清 token 跳登录页(验收 24)
 */
export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  const token = getToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)

  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}`, { ...init, headers })
  } catch {
    message.error('网络异常,请稍后重试')
    throw new Error('network error')
  }
  if (res.status === 401) handleUnauthorized()

  const body = (await res.json()) as ApiResponse<T>
  if (body.code === 401) handleUnauthorized()
  if (body.code !== 0) {
    message.error(body.message || '请求失败')
    throw new Error(body.message)
  }
  return body.data
}

/** GET */
export const get = <T>(path: string) => request<T>(path)

/** POST:data 为 undefined 时不带请求体 */
export const post = <T>(path: string, data?: unknown) =>
  request<T>(path, {
    method: 'POST',
    body: data === undefined ? undefined : JSON.stringify(data),
  })

/** PUT */
export const put = <T>(path: string, data: unknown) =>
  request<T>(path, { method: 'PUT', body: JSON.stringify(data) })

/** DELETE */
export const del = <T>(path: string) => request<T>(path, { method: 'DELETE' })