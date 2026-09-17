// 用户 store:登录态、昵称、资料缓存与登出(配合 api/auth 与 api/user)
// token 持久化由 storage + api 层负责;本 store 只维护内存态并供全局响应式使用
import { ref } from 'vue'
import { defineStore } from 'pinia'
import { clearToken, getToken } from '@/utils/storage'
import { getUserProfile } from '@/api/user'
import type { UserProfile } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  /** 用户昵称(登录/注册时写入;刷新后由 fetchProfile 恢复) */
  const nickname = ref('')
  /** 完整用户资料缓存(等级、连续天数等,个人中心复用) */
  const profile = ref<UserProfile | null>(null)
  /** 是否已登录(以 token 存在为准,与路由守卫逻辑一致) */
  const isLoggedIn = ref(!!getToken())

  /** 写入登录态(登录/注册成功后调用;token 已由 api 层持久化到 localStorage) */
  function setAuth(user: { nickname: string }): void {
    nickname.value = user.nickname
    isLoggedIn.value = true
  }

  /** 拉取用户资料(页面刷新后恢复昵称;失效 token 由 request 层统一 401 跳登录) */
  async function fetchProfile(): Promise<UserProfile> {
    const data = await getUserProfile()
    profile.value = data
    nickname.value = data.nickname
    return data
  }

  /** 登出:清 token 与内存态 */
  function logout(): void {
    clearToken()
    nickname.value = ''
    profile.value = null
    isLoggedIn.value = false
  }

  return { nickname, profile, isLoggedIn, setAuth, fetchProfile, logout }
})