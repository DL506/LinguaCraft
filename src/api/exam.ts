// 真题模考域 API 封装(任务书 12.4)
import { get, post } from './request'
import type { PageResult } from '@/types/api'
import type { ExamListItem, ExamPaper, ExamReport, ExamSubmitRequest } from '@/types/exam'
import type { SubmitResult } from '@/types/practice'

/** 模考列表:按年份倒序,支持年份筛选 */
export function getExamList(params: { year?: number; page?: number; size?: number } = {}) {
  const yearPart = params.year ? `&year=${params.year}` : ''
  return get<PageResult<ExamListItem>>(
    `/exam/list?page=${params.page ?? 1}&size=${params.size ?? 10}${yearPart}`
  )
}

/** 整卷详情:不含答案与解析(验收 35) */
export function getExamPaper(id: string): Promise<ExamPaper> {
  return get<ExamPaper>(`/exam/${id}`)
}

/** 交卷:status 区分 graded / partially_graded / pending(12.4 注释) */
export function submitExam(id: string, payload: ExamSubmitRequest): Promise<SubmitResult> {
  return post<SubmitResult>(`/exam/${id}/submit`, payload)
}

/** 成绩报告:submissionId 来自交卷响应 */
export function getExamReport(submissionId: string): Promise<ExamReport> {
  return get<ExamReport>(`/exam/submission/${submissionId}/report`)
}