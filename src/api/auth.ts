// 认证域 API 封装(任务书 12.1)
import { post } from './request'
import { setToken } from '@/utils/storage'

/** 登录/注册返回的用户信息 */
export interface AuthUser {
  id: string
  username: string
  nickname: string
  avatar?: string
}

/** 登录/注册接口响应(注册即登录,任务书 10.2) */
export interface AuthResult {
  token: string
  expiresIn: number
  user: AuthUser
}

/** 登录:成功后持久化 token */
export async function login(username: string, password: string): Promise<AuthResult> {
  const result = await post<AuthResult>('/auth/login', { username, password })
  setToken(result.token)
  return result
}

/** 注册:成功即登录,同样持久化 token */
export async function register(payload: {
  username: string
  password: string
  nickname: string
  email: string
}): Promise<AuthResult> {
  const result = await post<AuthResult>('/auth/register', payload)
  setToken(result.token)
  return result
}

/** 忘记密码:发送重置邮件 */
export function forgotPassword(email: string): Promise<null> {
  return post<null>('/auth/forgot-password', { email })
}

/** 重置密码 */
export function resetPassword(token: string, newPassword: string): Promise<null> {
  return post<null>('/auth/reset-password', { token, newPassword })
}