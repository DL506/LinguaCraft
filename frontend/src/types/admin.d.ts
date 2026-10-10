// 后台管理端类型(阶段 A1 起):与后端 /api/admin/** 响应一一对应
import type { PracticeType } from './practice'

/** 管理端权限探针响应(GET /api/admin/ping) */
export interface AdminPing {
  /** 当前管理员 ID(后端 Long 经全局序列化为字符串) */
  userId: string
  /** 角色:能拿到响应即为 admin */
  role: 'student' | 'admin'
  /** 服务器当前时间(ISO 本地日期时间) */
  serverTime: string
}

/** 题目来源类型(管理端可录入三种) */
export type AdminSourceType = 'real' | 'teacher' | 'ai'

/** 管理端题库列表项(GET /api/admin/questions) */
export interface AdminQuestionListItem {
  id: string
  type: PracticeType
  title: string
  sourceType: AdminSourceType
  difficulty: number
  /** 1 上架 / 0 下架 */
  status: number
  hasAnswer: boolean
  totalScore: number
  perQuestionScore: number
  /** 被练习提交引用次数(>0 不可删除) */
  refSubmissionCount: number
  updatedAt?: string
}

/** 管理端题目详情(GET /api/admin/questions/{id},含答案) */
export interface AdminQuestionDetail {
  id: string
  type: PracticeType
  title: string
  sourceType: AdminSourceType
  /** 来源 meta(year/region/teacherName 等) */
  sourceMeta?: Record<string, unknown>
  difficulty: number
  tags?: string[]
  status: number
  hasAnswer: boolean
  totalScore: number
  perQuestionScore: number
  /** 下发内容(不含答案) */
  content: Record<string, unknown>
  /** 答案与解析(仅管理端可见) */
  answerKey?: Record<string, unknown>
  createdAt?: string
  updatedAt?: string
}

/** 题目新建/更新请求(POST/PUT /api/admin/questions[/{id}]) */
export interface AdminQuestionPayload {
  /** 新建时可留空,由服务端生成 */
  id?: string
  type: PracticeType
  sourceType: AdminSourceType
  title?: string
  difficulty?: number
  tags?: string[]
  status?: number
  sourceMeta?: Record<string, unknown>
  content: Record<string, unknown>
  answerKey?: Record<string, unknown>
}
