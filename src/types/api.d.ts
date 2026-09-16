// 通用 API 类型:统一响应包装、分页结构、首页接口类型(任务书 12 章)
import type { PracticeType } from './practice'

/** 统一响应包装:所有接口返回 { code, message, data }(任务书 12 章响应格式) */
export interface ApiResponse<T = unknown> {
  /** 业务状态码:0 表示成功,非 0 视为错误 */
  code: number
  /** 提示信息 */
  message: string
  /** 业务数据 */
  data: T
}

/** 分页响应:所有列表接口统一返回该结构(任务书 11.5、验收 22) */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

/** 首页今日任务单条:type 为具体题型或背单词(任务书 12.2) */
export interface HomeTodayTaskItem {
  type: PracticeType | 'vocab'
  /** 任务目标数量 */
  target: number
  /** 已完成数量 */
  done: number
}

/** 首页今日任务响应(12.2) */
export interface HomeTodayTasks {
  items: HomeTodayTaskItem[]
}

/** 首页考试倒计时响应(12.2) */
export interface ExamCountdown {
  /** 考试日期 */
  examDate: string
  /** 剩余天数 */
  daysLeft: number
  /** 备考进度(0-100 整数百分比) */
  progress: number
}