// 刷题域 API 封装:五种题型统一 list/detail/submit + AI 出题(任务书 12.3)
import { get, post } from './request'
import type { PageResult } from '@/types/api'
import type {
  PracticeItem,
  PracticeListItem,
  PracticeSubmitRequest,
  PracticeType,
  SubmitResult,
  WritingSubmitResult,
} from '@/types/practice'

/** 题型来源列表:source 仅接受 real | teacher(任务书 11.1 注) */
export function getPracticeList(
  type: PracticeType,
  params: { source?: 'real' | 'teacher'; page?: number; size?: number } = {}
): Promise<PageResult<PracticeListItem>> {
  const sourcePart = params.source ? `&source=${params.source}` : ''
  return get<PageResult<PracticeListItem>>(
    `/practice/list?type=${type}${sourcePart}&page=${params.page ?? 1}&size=${params.size ?? 10}`
  )
}

/** 题目详情:响应不含答案与解析(验收 35) */
export function getPracticeDetail(type: PracticeType, id: string): Promise<PracticeItem> {
  return get<PracticeItem>(`/practice/detail/${type}/${id}`)
}

/** 提交作答:写作题返回 WritingSubmitResult(12.3 注) */
export function submitPractice(
  type: PracticeType,
  id: string,
  payload: PracticeSubmitRequest
): Promise<SubmitResult | WritingSubmitResult> {
  return post<SubmitResult | WritingSubmitResult>(`/practice/detail/${type}/${id}/submit`, payload)
}

/** AI 出题:点即生成,无参数面板(任务书 10.6) */
export function generatePractice(type: PracticeType): Promise<PracticeItem> {
  return post<PracticeItem>('/practice/generate', { type })
}