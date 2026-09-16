// 首页域 API 封装(任务书 12.2)
import { get, post } from './request'
import type { ExamCountdown, HomeTodayTasks } from '@/types/api'
import type { PracticeItem } from '@/types/practice'

/** 今日任务 */
export function getTodayTasks(): Promise<HomeTodayTasks> {
  return get<HomeTodayTasks>('/home/today-tasks')
}

/** 考试倒计时 */
export function getExamCountdown(): Promise<ExamCountdown> {
  return get<ExamCountdown>('/home/exam-countdown')
}

/** 智能练习:随机返回一道题,type 从 item.type 取(任务书 10.5) */
export function smartPractice(): Promise<PracticeItem> {
  return post<PracticeItem>('/practice/smart', {})
}