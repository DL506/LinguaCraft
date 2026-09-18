// 本地存储工具:统一管理 localStorage 键(任务书 5.3 主题键、认证 token)

/** token 存储键 */
const TOKEN_KEY = 'lc-token'

/** 读取登录 token;未登录返回 null */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/** 保存登录 token */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 清除登录 token(登出 / 401 时调用) */
export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}