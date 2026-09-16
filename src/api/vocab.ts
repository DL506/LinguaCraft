// 背单词域 API 封装(任务书 12.5)
import { get, post } from './request'
import type { PageResult } from '@/types/api'
import type { VocabBook, VocabReviewRequest, VocabTodayTask, Word } from '@/types/vocab'

/** 词书列表 */
export function getVocabBooks(): Promise<VocabBook[]> {
  return get<VocabBook[]>('/vocab/books')
}

/** 词书单词分页 */
export function getBookWords(bookId: string, page: number, size: number): Promise<PageResult<Word>> {
  return get<PageResult<Word>>(`/vocab/book/${bookId}/words?page=${page}&size=${size}`)
}

/** 记忆自评(unknown/fuzzy/known) */
export function submitVocabReview(payload: VocabReviewRequest): Promise<null> {
  return post<null>('/vocab/review', payload)
}

/** 今日背单词任务 */
export function getVocabTodayTask(): Promise<VocabTodayTask> {
  return get<VocabTodayTask>('/vocab/task/today')
}

/** 到期复习单词 */
export function getVocabDue(page: number, size: number): Promise<PageResult<Word>> {
  return get<PageResult<Word>>(`/vocab/task/due?page=${page}&size=${size}`)
}