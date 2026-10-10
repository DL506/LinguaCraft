// 后台管理端 API 封装(阶段 A1 起):全部走 /api/admin/**
// 说明:管理端接口刻意不注册 MSW handler —— main.ts 配置 onUnhandledRequest: 'bypass',
//       未匹配路径直连真实后端,因此后台功能只在接真实后端时可用(演示 mock 模式下面向的是学生端)。
import { del, get, patch, post, put } from './request'
import type {
  AdminPing,
  AdminDashboard,
  AdminExamSubmissionDetail,
  AdminExamSubmissionListItem,
  AdminQuestionDetail,
  AdminQuestionListItem,
  AdminQuestionPayload,
  AdminSubmissionDetail,
  AdminSubmissionListItem,
  AdminUserDetail,
  AdminUserListItem,
  AdminVocabBook,
  AdminWord,
  ExamWritingGradePayload,
  ManualGradePayload,
  VocabBookPayload,
  WordPayload,
} from '@/types/admin'
import type { PageResult } from '@/types/api'
import type { PracticeType } from '@/types/practice'

/** 权限探针:验证管理员令牌能打通 /api/admin/**(学生令牌会得到 code 403) */
export function getAdminPing(): Promise<AdminPing> {
  return get<AdminPing>('/admin/ping')
}

/** 运营看板概览(纯计数聚合;「今日」与学生端统计同为 created_at 口径) */
export function getAdminDashboard(): Promise<AdminDashboard> {
  return get<AdminDashboard>('/admin/dashboard')
}

/** 用户列表(按注册时间倒序) */
export function getAdminUsers(
  params: { keyword?: string; role?: string; enabled?: boolean; page?: number; size?: number } = {}
): Promise<PageResult<AdminUserListItem>> {
  const query = new URLSearchParams()
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.role) query.set('role', params.role)
  if (params.enabled !== undefined && params.enabled !== null) query.set('enabled', String(params.enabled))
  query.set('page', String(params.page ?? 1))
  query.set('size', String(params.size ?? 20))
  return get<PageResult<AdminUserListItem>>(`/admin/users?${query.toString()}`)
}

/** 用户详情(资料 + 学习量小结) */
export function getAdminUser(id: number): Promise<AdminUserDetail> {
  return get<AdminUserDetail>(`/admin/users/${id}`)
}

/** 启用/停用(注意:已签发令牌不受影响,停用最长 2 小时才对旧会话生效) */
export function setAdminUserEnabled(id: number, enabled: boolean): Promise<null> {
  return patch<null>(`/admin/users/${id}/status?enabled=${enabled}`)
}

/** 词书列表(含声明词数与真实词数) */
export function getAdminVocabBooks(): Promise<AdminVocabBook[]> {
  return get<AdminVocabBook[]>('/admin/vocab/books')
}

/** 新建词书 */
export function createAdminVocabBook(payload: VocabBookPayload): Promise<AdminVocabBook> {
  return post<AdminVocabBook>('/admin/vocab/books', payload)
}

/** 更新词书(不会自动改写声明总词数) */
export function updateAdminVocabBook(id: string, payload: VocabBookPayload): Promise<AdminVocabBook> {
  return put<AdminVocabBook>(`/admin/vocab/books/${encodeURIComponent(id)}`, payload)
}

/** 把声明总词数对齐为真实词数(显式操作) */
export function syncAdminVocabBookTotal(id: string): Promise<AdminVocabBook> {
  return patch<AdminVocabBook>(`/admin/vocab/books/${encodeURIComponent(id)}/sync-total`)
}

/** 删除词书(学生端默认词书 book-syllabus 会被拒) */
export function deleteAdminVocabBook(id: string): Promise<null> {
  return del<null>(`/admin/vocab/books/${encodeURIComponent(id)}`)
}

/** 单词列表(按拼写字典序;含引用计数) */
export function getAdminWords(
  params: { level?: string; keyword?: string; page?: number; size?: number } = {}
): Promise<PageResult<AdminWord>> {
  const query = new URLSearchParams()
  if (params.level) query.set('level', params.level)
  if (params.keyword) query.set('keyword', params.keyword)
  query.set('page', String(params.page ?? 1))
  query.set('size', String(params.size ?? 20))
  return get<PageResult<AdminWord>>(`/admin/vocab/words?${query.toString()}`)
}

/** 新建单词(ID 由服务端按最大数字后缀生成) */
export function createAdminWord(payload: WordPayload): Promise<AdminWord> {
  return post<AdminWord>('/admin/vocab/words', payload)
}

/** 更新单词(改 level 即改它属于哪些词书) */
export function updateAdminWord(id: string, payload: WordPayload): Promise<AdminWord> {
  return put<AdminWord>(`/admin/vocab/words/${encodeURIComponent(id)}`, payload)
}

/** 删除单词(有学习记录或收藏时服务端拒绝) */
export function deleteAdminWord(id: string): Promise<null> {
  return del<null>(`/admin/vocab/words/${encodeURIComponent(id)}`)
}

/** 调整角色(只接受 student/admin;不许给自己降权) */
export function setAdminUserRole(id: number, role: string): Promise<null> {
  return patch<null>(`/admin/users/${id}/role?role=${encodeURIComponent(role)}`)
}

/** 题库分页列表(管理端视角:含下架题与引用数) */
export function getAdminQuestions(
  params: { type?: PracticeType; sourceType?: string; status?: number; keyword?: string; page?: number; size?: number } = {}
): Promise<PageResult<AdminQuestionListItem>> {
  const query = new URLSearchParams()
  if (params.type) query.set('type', params.type)
  if (params.sourceType) query.set('sourceType', params.sourceType)
  if (params.status !== undefined && params.status !== null) query.set('status', String(params.status))
  if (params.keyword) query.set('keyword', params.keyword)
  query.set('page', String(params.page ?? 1))
  query.set('size', String(params.size ?? 20))
  return get<PageResult<AdminQuestionListItem>>(`/admin/questions?${query.toString()}`)
}

/** 题目详情(含 answer_key) */
export function getAdminQuestion(id: string): Promise<AdminQuestionDetail> {
  return get<AdminQuestionDetail>(`/admin/questions/${encodeURIComponent(id)}`)
}

/** 新建题目(服务端会做五道结构校验,失败返回 code 400 与定位路径) */
export function createAdminQuestion(payload: AdminQuestionPayload): Promise<AdminQuestionDetail> {
  return post<AdminQuestionDetail>('/admin/questions', payload)
}

/** 更新题目(整条覆盖 content 与 answer_key;题型不可变) */
export function updateAdminQuestion(id: string, payload: AdminQuestionPayload): Promise<AdminQuestionDetail> {
  return put<AdminQuestionDetail>(`/admin/questions/${encodeURIComponent(id)}`, payload)
}

/** 上架/下架 */
export function setAdminQuestionStatus(id: string, status: 0 | 1): Promise<null> {
  return patch<null>(`/admin/questions/${encodeURIComponent(id)}/status?status=${status}`)
}

/** 删除题目(被提交或错题引用时服务端拒绝) */
export function deleteAdminQuestion(id: string): Promise<null> {
  return del<null>(`/admin/questions/${encodeURIComponent(id)}`)
}

/** 批改队列(默认按等待时长正序;method=manual 只看必须人工处理的断链件) */
export function getAdminSubmissions(
  params: { status?: string; type?: PracticeType; method?: string; keyword?: string; page?: number; size?: number } = {}
): Promise<PageResult<AdminSubmissionListItem>> {
  const query = new URLSearchParams()
  if (params.status) query.set('status', params.status)
  if (params.type) query.set('type', params.type)
  if (params.method) query.set('method', params.method)
  if (params.keyword) query.set('keyword', params.keyword)
  query.set('page', String(params.page ?? 1))
  query.set('size', String(params.size ?? 20))
  return get<PageResult<AdminSubmissionListItem>>(`/admin/submissions?${query.toString()}`)
}

/** 判分材料(学生作答 + 题目内容 + 参考答案 + 既有判分) */
export function getAdminSubmission(id: number): Promise<AdminSubmissionDetail> {
  return get<AdminSubmissionDetail>(`/admin/submissions/${id}`)
}

/** 人工判分落库(仅待批改可提交;服务端带 pending 条件防并发覆盖) */
export function gradeAdminSubmission(id: number, payload: ManualGradePayload): Promise<AdminSubmissionDetail> {
  return post<AdminSubmissionDetail>(`/admin/submissions/${id}/grade`, payload)
}

/** 模考批改队列(客观分交卷即算;writingGraded=false 的行才是欠着的) */
export function getAdminExamSubmissions(
  params: { status?: string; keyword?: string; page?: number; size?: number } = {}
): Promise<PageResult<AdminExamSubmissionListItem>> {
  const query = new URLSearchParams()
  if (params.status) query.set('status', params.status)
  if (params.keyword) query.set('keyword', params.keyword)
  query.set('page', String(params.page ?? 1))
  query.set('size', String(params.size ?? 20))
  return get<PageResult<AdminExamSubmissionListItem>>(`/admin/exam-submissions?${query.toString()}`)
}

/** 模考判分材料(作文正文、写作部分题目与满分、客观各部分得分) */
export function getAdminExamSubmission(id: number): Promise<AdminExamSubmissionDetail> {
  return get<AdminExamSubmissionDetail>(`/admin/exam-submissions/${id}`)
}

/** 模考作文人工判分(与 AI 异步批改器互斥,靠 writing_graded_at IS NULL 兜住) */
export function gradeAdminExamWriting(
  id: number,
  payload: ExamWritingGradePayload
): Promise<AdminExamSubmissionDetail> {
  return post<AdminExamSubmissionDetail>(`/admin/exam-submissions/${id}/grade-writing`, payload)
}
