// 个人中心域 API 封装(任务书 12.7)
import { del, get, post, put } from './request'
import type { PageResult } from '@/types/api'
import type { PracticeType, SubmitResult, WritingSubmitResult } from '@/types/practice'
import type {
  FavoriteItem,
  MistakeItem,
  SubmissionListItem,
  UserProfile,
  UserSettings,
  UserStats,
} from '@/types/user'

/** 用户资料 */
export function getUserProfile(): Promise<UserProfile> {
  return get<UserProfile>('/user/profile')
}

/** 学习统计 */
export function getUserStats(): Promise<UserStats> {
  return get<UserStats>('/user/stats')
}

/** 用户设置(themeMode 不在本接口,见任务书 5.3) */
export function getUserSettings(): Promise<UserSettings> {
  return get<UserSettings>('/user/settings')
}

/** 更新设置:未提供的字段保持不变 */
export function updateUserSettings(payload: Partial<UserSettings>): Promise<null> {
  return put<null>('/user/settings', payload)
}

/** 提交记录列表:status 筛选(缺省 all) */
export function getSubmissions(
  params: { status?: 'all' | 'graded' | 'partially_graded' | 'pending'; page?: number; size?: number } = {}
): Promise<PageResult<SubmissionListItem>> {
  const statusPart = params.status ? `&status=${params.status}` : ''
  return get<PageResult<SubmissionListItem>>(
    `/user/submissions?page=${params.page ?? 1}&size=${params.size ?? 10}${statusPart}`
  )
}

/** 提交详情:写作题返回 WritingSubmitResult */
export function getSubmissionDetail(id: string): Promise<SubmitResult | WritingSubmitResult> {
  return get<SubmitResult | WritingSubmitResult>(`/user/submissions/${id}`)
}

/** 错题本列表:来源 + 题型筛选 */
export function getMistakes(
  params: { source?: 'real' | 'teacher' | 'ai'; type?: PracticeType; page?: number; size?: number } = {}
): Promise<PageResult<MistakeItem>> {
  const sourcePart = params.source ? `&source=${params.source}` : ''
  const typePart = params.type ? `&type=${params.type}` : ''
  return get<PageResult<MistakeItem>>(
    `/user/mistakes?page=${params.page ?? 1}&size=${params.size ?? 10}${sourcePart}${typePart}`
  )
}

/** 删除错题 */
export function deleteMistake(id: string): Promise<null> {
  return del<null>(`/user/mistakes/${id}`)
}

/** 收藏列表 */
export function getFavorites(params: { page?: number; size?: number } = {}): Promise<PageResult<FavoriteItem>> {
  return get<PageResult<FavoriteItem>>(`/user/favorites?page=${params.page ?? 1}&size=${params.size ?? 10}`)
}

/** 新增收藏(单词/句子) */
export function addFavorite(payload: {
  type: 'word' | 'sentence'
  content: string
  refId?: string
}): Promise<FavoriteItem> {
  return post<FavoriteItem>('/user/favorites', payload)
}

/** 删除收藏 */
export function deleteFavorite(id: string): Promise<null> {
  return del<null>(`/user/favorites/${id}`)
}