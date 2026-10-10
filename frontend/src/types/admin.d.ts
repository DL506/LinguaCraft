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

/** 管理端批改队列列表项(GET /api/admin/submissions) */
export interface AdminSubmissionListItem {
  id: number
  userId: number
  nickname?: string
  type: PracticeType
  title: string
  questionId: string
  /** graded / partially_graded / pending */
  status: string
  /** ai 已出分 / auto 定时任务将自动出分 / manual 必须人工处理 */
  gradingMethod: string
  score?: number
  totalScore: number
  answeredCount: number
  /** 自提交起的等待分钟数 */
  waitingMinutes?: number
  submittedAt: string
  gradedAt?: string
}

/** 管理端批改详情(GET /api/admin/submissions/{id}) */
export interface AdminSubmissionDetail {
  id: number
  userId: number
  nickname?: string
  type: PracticeType
  title: string
  questionId: string
  status: string
  gradingMethod: string
  score?: number
  totalScore: number
  /** 客观题为 { "1": "作答", ... };写作为 { content: "正文" } */
  answers?: Record<string, string>
  questionContent?: Record<string, unknown>
  /** 题库参考答案(为空即无答案题,判定完全依赖人工) */
  answerKey?: Record<string, unknown>
  questionHasAnswer?: boolean
  details?: unknown
  writingResult?: unknown
  estimatedTime?: string
  duration?: number
  waitingMinutes?: number
  submittedAt: string
  gradedAt?: string
}

/** 客观题单题判定(POST /api/admin/submissions/{id}/grade) */
export interface ManualGradeItem {
  id: number
  correctAnswer?: string
  isCorrect: boolean
  explanation?: string
}

/** 作文评分维度 */
export interface ManualGradeDimension {
  name: string
  score: number
  comment?: string
}

/** 人工批改请求体 */
export interface ManualGradePayload {
  items?: ManualGradeItem[]
  /** 留空则由后端按"单题分值 × 正确数"累计 */
  score?: number
  dimensions?: ManualGradeDimension[]
  sampleAnswer?: string
  overallComment?: string
}

/** 管理端模考批改队列列表项(GET /api/admin/exam-submissions) */
export interface AdminExamSubmissionListItem {
  id: number
  userId: number
  nickname?: string
  examId: string
  paperTitle?: string
  year?: number
  status: string
  gradingMethod: string
  objectiveScore?: number
  writingScore?: number
  totalScore: number
  /** false 即作文还欠着,需要人工或 AI 出分 */
  writingGraded: boolean
  /** 学生是否真的写了正文(空白正文不可判) */
  hasWritingContent: boolean
  waitingMinutes?: number
  submittedAt: string
  writingGradedAt?: string
}

/** 管理端模考批改详情(GET /api/admin/exam-submissions/{id}) */
export interface AdminExamSubmissionDetail {
  id: number
  userId: number
  nickname?: string
  examId: string
  paperTitle?: string
  year?: number
  status: string
  gradingMethod: string
  objectiveScore?: number
  writingScore?: number
  totalScore: number
  /** 写作部分满分(取自试卷 section 分值,兜底 15) */
  writingFullScore: number
  writingContent?: string
  /** 试卷写作部分(prompt / requirements / scoringDimensions / wordLimit) */
  writingSection?: Record<string, unknown>
  sectionScores?: Record<string, number>
  writingResult?: Record<string, unknown>
  writingGraded: boolean
  estimatedTime?: string
  duration?: number
  waitingMinutes?: number
  submittedAt: string
  writingGradedAt?: string
}

/** 模考作文人工批改请求体(POST /api/admin/exam-submissions/{id}/grade-writing) */
export interface ExamWritingGradePayload {
  dimensions: ManualGradeDimension[]
  /** 留空按维度合计 */
  score?: number
  sampleAnswer?: string
  overallComment?: string
}
