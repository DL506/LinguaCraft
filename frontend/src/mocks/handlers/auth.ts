// 认证域 mock handlers(任务书 12.1)
// 演示账号:demo / 123456;注册用户仅存内存,刷新页面后重置
// 本文件同时承载公共 mock 工具(delay/ok/fail/currentUser),供各 handler 文件复用
import { http, HttpResponse } from 'msw'

/** 内置演示用户结构(内存用户表) */
export interface MockUser {
  id: string
  username: string
  password: string
  nickname: string
  email: string
}

/** 内存用户表:初始仅演示账号 */
export const mockUsers = new Map<string, MockUser>([
  ['demo', { id: 'u-demo', username: 'demo', password: '123456', nickname: '小明', email: 'demo@linguacraft.dev' }],
])

/** 生成 mock token(格式 mock-token-<username>,后端阶段将替换为 JWT) */
function issueToken(username: string): string {
  return `mock-token-${username}`
}

/** 从 Auth 头解析当前用户;未登录或 token 失效返回 null */
export function currentUser(request: Request): MockUser | null {
  const auth = request.headers.get('Authorization') ?? ''
  const token = auth.replace(/^Bearer\s+/, '')
  if (!token.startsWith('mock-token-')) return null
  return mockUsers.get(token.slice('mock-token-'.length)) ?? null
}

/** 成功响应包装(任务书 12 章统一格式) */
export function ok<T>(data: T) {
  return HttpResponse.json({ code: 0, message: 'ok', data })
}

/** 业务错误响应包装;httpStatus 默认 200,401 时前端统一清 token(任务书 12 章) */
export function fail(code: number, message: string, httpStatus = 200) {
  return HttpResponse.json({ code, message, data: null }, { status: httpStatus })
}

/** 模拟网络延时 200~500ms(任务书 13.3) */
export function delay(ms = 200 + Math.random() * 300): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

export const authHandlers = [
  /** 登录:校验用户名密码,成功返回 token 与用户信息(12.1) */
  http.post('/api/auth/login', async ({ request }) => {
    await delay()
    const body = (await request.json()) as { username?: string; password?: string }
    const user = mockUsers.get(body.username ?? '')
    if (!user || user.password !== body.password) {
      return fail(1001, '用户名或密码错误')
    }
    return ok({
      token: issueToken(user.username),
      expiresIn: 7200,
      user: { id: user.id, username: user.username, nickname: user.nickname },
    })
  }),

  /** 注册:用户名冲突返回业务错误;成功即视为登录(注册即登录,任务书 10.2) */
  http.post('/api/auth/register', async ({ request }) => {
    await delay()
    const body = (await request.json()) as {
      username?: string
      password?: string
      nickname?: string
      email?: string
    }
    const username = body.username ?? ''
    if (!username) return fail(1003, '用户名不能为空')
    if (mockUsers.has(username)) return fail(1002, '用户名已存在')
    const user: MockUser = {
      id: `u-${username}`,
      username,
      password: body.password ?? '',
      nickname: body.nickname ?? username,
      email: body.email ?? '',
    }
    mockUsers.set(username, user)
    return ok({
      token: issueToken(username),
      expiresIn: 7200,
      user: { id: user.id, username: user.username, nickname: user.nickname },
    })
  }),

  /** 忘记密码:mock 直接成功(提示语由前端展示) */
  http.post('/api/auth/forgot-password', async () => {
    await delay()
    return ok(null)
  }),

  /** 重置密码:mock 直接成功 */
  http.post('/api/auth/reset-password', async () => {
    await delay()
    return ok(null)
  }),
]