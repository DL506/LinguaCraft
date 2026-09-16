// 个人中心域 mock handlers(任务书 12.7)
// 本文件同时承载内存会话 db:设置 / 提交记录 / 错题 / 收藏,供 practice / exam / vocab / home 等 handler 共享
import { http } from 'msw'
import { currentUser, delay, fail, ok } from './auth'
import type {
  FavoriteItem,
  MistakeItem,
  SubmissionListItem,
  UserProfile,
  UserSettings,
  UserStats,
} from '@/types/user'
import type { GradingMethod, GradingStatus, PracticeType, QuestionSource, SubmitDetail } from '@/types/practice'

/** 人工批改出结果延时(毫秒):pending 提交超过该时长后,再次查询详情视为已批改(演示批改流转) */
export const MANUAL_GRADE_DELAY_MS = 3 * 60 * 1000

// ================= 内存会话 db =================

/** 用户设置内存态(themeMode 不入本接口,权威源为 localStorage,任务书 5.3 / 12.7 说明) */
export const userSettings: UserSettings = {
  dailyWordGoal: 50,
  dailyReadingGoal: 1,
  accent: 'us',
  grammarFillCaseSensitive: false,
  gradingMethod: 'ai',
  reminderTime: '08:00',
}

/** 用户资料(静态演示值) */
export const userProfile: UserProfile = {
  id: 'u-demo',
  username: 'demo',
  nickname: '小明',
  level: 5,
  levelName: '青铜学习者',
  continuousDays: 21,
  totalStudyDays: 128,
}

/** 提交记录内部结构(练习单题与模考共用;模考走 exam.ts 的 examSubmissions) */
export interface MockSubmission {
  id: string
  /** 来源题目 id(练习);模考记录不存入本表 */
  questionId: string
  title: string
  type: PracticeType
  source: QuestionSource
  status: GradingStatus
  gradingMethod: GradingMethod
  score?: number
  totalScore?: number
  correctCount?: number
  totalCount?: number
  details?: SubmitDetail[]
  /** 用户作答原文(查询详情时回显用) */
  answers: Record<number, string> | { content: string }
  duration: number
  submittedAt: string
  /** 提交时间戳(毫秒,用于判定人工批改是否超时) */
  createdAt: number
  estimatedTime?: string
  /** 作文人工批改结果(批改完成前为空) */
  writingResult?: {
    dimensions: { name: string; score: number; comment: string }[]
    sampleAnswer?: string
    overallComment?: string
    writingScore?: number
  }
}

/** 提交记录内存表(种子含 1 条已批改阅读 + 2 条待批改,首页待批改提示展示用) */
export const mockSubmissions: MockSubmission[] = [
  {
    id: 'sub-seed-1',
    questionId: 'reading-real-2023-1',
    title: '2023年真题·阅读理解 Passage 1',
    type: 'reading',
    source: { type: 'real', meta: { year: 2023, region: '广东' } },
    status: 'graded',
    gradingMethod: 'ai',
    score: 6,
    totalScore: 10,
    correctCount: 3,
    totalCount: 5,
    details: [
      {
        id: 1,
        userAnswer: 'B',
        correctAnswer: 'B',
        isCorrect: true,
        explanation: '全文对比数字阅读与纸质阅读对学习结果的影响,故选 B。',
      },
      {
        id: 2,
        userAnswer: 'A',
        correctAnswer: 'C',
        isCorrect: false,
        explanation: '第二句明确指出数字阅读者 tend to scan quickly but remember less。',
      },
      {
        id: 3,
        userAnswer: 'B',
        correctAnswer: 'B',
        isCorrect: true,
        explanation: '第三句说明纸质书阅读者 score higher in comprehension tests。',
      },
      {
        id: 4,
        userAnswer: 'D',
        correctAnswer: 'B',
        isCorrect: false,
        explanation: 'scan 意为快速浏览,与 take photos 无关。',
      },
      {
        id: 5,
        userAnswer: 'B',
        correctAnswer: 'B',
        isCorrect: true,
        explanation: '由纸质阅读者得分更高可推出纸质阅读有助于深度学习。',
      },
    ],
    answers: { 1: 'B', 2: 'A', 3: 'B', 4: 'D', 5: 'B' },
    duration: 420,
    submittedAt: '2000-01-03T09:30:00',
    createdAt: Date.now() - 3 * 24 * 3600 * 1000,
  },
  {
    id: 'sub-seed-2',
    questionId: 'writing-real-2023-1',
    title: '2023年真题·写作',
    type: 'writing',
    source: { type: 'real', meta: { year: 2023, region: '广东' } },
    status: 'pending',
    gradingMethod: 'manual',
    estimatedTime: '预计 24 小时内出结果',
    answers: { content: 'Dear Peter, I am glad to hear your plan...' },
    duration: 1500,
    submittedAt: '2000-01-03T10:15:00',
    createdAt: Date.now() - 2 * 24 * 3600 * 1000,
  },
  {
    id: 'sub-seed-3',
    questionId: 'reading-teacher-1',
    title: '王老师·模拟阅读卷 Passage 1',
    type: 'reading',
    source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
    status: 'pending',
    gradingMethod: 'auto',
    estimatedTime: '预计 24 小时内出结果',
    answers: { 1: 'B', 2: 'B', 3: 'B', 4: 'A', 5: 'B' },
    duration: 350,
    submittedAt: '2000-01-03T11:00:00',
    createdAt: Date.now() - 2 * 24 * 3600 * 1000,
  },
]

/** 错题内存表(种子 2 条) */
export const mockMistakes: MistakeItem[] = [
  {
    id: 'mk-seed-1',
    questionId: 'reading-real-2023-1',
    type: 'reading',
    source: { type: 'real', meta: { year: 2023, region: '广东' } },
    stem: 'According to the passage, digital readers tend to ________.',
    userAnswer: 'A',
    correctAnswer: 'C',
    explanation: '第二句明确指出数字阅读者 tend to scan quickly but remember less。',
    createdAt: '2000-01-03T09:30:00',
  },
  {
    id: 'mk-seed-2',
    questionId: 'cloze-teacher-1',
    type: 'cloze',
    source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
    stem: 'But she kept a training diary and recorded every small ________.',
    userAnswer: 'A',
    correctAnswer: 'B',
    explanation: 'recorded every small victory 记录每次小胜利,与训练日记呼应。',
    createdAt: '2000-01-02T20:12:00',
  },
]

/** 收藏内存表(种子 2 条:单词 + 句子) */
export const mockFavorites: FavoriteItem[] = [
  {
    id: 'fv-seed-1',
    type: 'word',
    content: 'abandon',
    refId: 'w-001',
    createdAt: '2000-01-03T08:00:00',
  },
  {
    id: 'fv-seed-2',
    type: 'sentence',
    content: 'Reading is one of the most valuable habits a student can build.',
    refId: 'matching-real-2023-1',
    createdAt: '2000-01-02T19:40:00',
  },
]

// ================= 共享操作(供其他 handler 调用) =================

let submissionSeq = 100
let mistakeSeq = 10
let favoriteSeq = 10

/** 新增提交记录并返回自增 id */
export function addSubmission(rec: Omit<MockSubmission, 'id' | 'createdAt' | 'submittedAt'>): string {
  const id = `${rec.type}-sub-${submissionSeq++}`
  mockSubmissions.unshift({
    ...rec,
    id,
    createdAt: Date.now(),
    submittedAt: new Date().toISOString(),
  })
  return id
}

/** 新增错题记录 */
export function addMistake(item: Omit<MistakeItem, 'id' | 'createdAt'>): void {
  mockMistakes.unshift({ ...item, id: `mk-${mistakeSeq++}`, createdAt: new Date().toISOString() })
}

/** 计算某题型今日提交数量(首页今日任务用) */
export function todaySubmissionsCount(type: PracticeType): number {
  const today = new Date().toDateString()
  return mockSubmissions.filter(
    (s) => s.type === type && new Date(s.createdAt).toDateString() === today
  ).length
}

/** 待批改数量(学习统计 + 首页提示用) */
function pendingCount(): number {
  return mockSubmissions.filter((s) => s.status === 'pending').length
}

/** pending 提交超时后流转:客观题→graded,作文→graded + 人工批改结果(需外部提供范文) */
export function refreshSubmissionStatus(sub: MockSubmission): MockSubmission {
  if (sub.status !== 'pending') return sub
  const elapsed = Date.now() - sub.createdAt
  if (elapsed < MANUAL_GRADE_DELAY_MS) return sub
  sub.status = 'graded'
  return sub
}

/** 由内部记录构建对外 SubmitResult(写作题返回 WritingSubmitResult) */
export function buildSubmitResult(sub: MockSubmission) {
  const base = {
    submissionId: sub.id,
    type: sub.type,
    status: sub.status,
    gradingMethod: sub.gradingMethod,
    score: sub.score,
    totalScore: sub.totalScore,
    correctCount: sub.correctCount,
    totalCount: sub.totalCount,
    details: sub.status === 'graded' ? sub.details : undefined,
    estimatedTime: sub.estimatedTime,
  }
  if (sub.type === 'writing') {
    return {
      ...base,
      dimensions: sub.writingResult?.dimensions,
      sampleAnswer: sub.writingResult?.sampleAnswer,
      overallComment: sub.writingResult?.overallComment,
    }
  }
  return base
}

/** 内部记录 → 提交列表项(12.7 列表结构) */
function toListItem(sub: MockSubmission): SubmissionListItem {
  return {
    id: sub.id,
    type: sub.type,
    title: sub.title,
    source: sub.source,
    status: sub.status,
    score: sub.score,
    totalScore: sub.totalScore,
    submittedAt: sub.submittedAt,
    estimatedTime: sub.estimatedTime,
  }
}

// ================= handlers =================

export const userHandlers = [
  /** 用户资料(12.7) */
  http.get('/api/user/profile', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    return ok(userProfile)
  }),

  /** 学习统计:种子数据 + 动态待批改数量(12.7) */
  http.get('/api/user/stats', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const stats: UserStats = {
      totalStudyTime: 1250,
      totalWords: 128,
      totalReading: 12,
      accuracy: 72,
      pendingGradingCount: pendingCount(),
      typeAccuracy: { reading: 72, matching: 80, cloze: 55, grammar: 62, writing: 78 },
      scoreTrend: [
        { date: '09-11', reading: 60, matching: 75, cloze: 50, grammar: 55, writing: 70 },
        { date: '09-12', reading: 65, matching: 78, cloze: 52, grammar: 58, writing: 72 },
        { date: '09-13', reading: 68, matching: 76, cloze: 54, grammar: 60, writing: 75 },
        { date: '09-14', reading: 70, matching: 80, cloze: 53, grammar: 61, writing: 74 },
        { date: '09-15', reading: 69, matching: 79, cloze: 55, grammar: 60, writing: 76 },
        { date: '09-16', reading: 71, matching: 81, cloze: 56, grammar: 63, writing: 77 },
        { date: '09-17', reading: 72, matching: 80, cloze: 55, grammar: 62, writing: 78 },
      ],
      sourceAccuracy: { real: 70, teacher: 75, ai: -1 },
    }
    return ok(stats)
  }),

  /** 用户设置(12.7) */
  http.get('/api/user/settings', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    return ok(userSettings)
  }),

  /** 更新设置:部分字段可选,未提供保持不变(12.7) */
  http.put('/api/user/settings', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const body = (await request.json()) as Partial<UserSettings>
    Object.assign(userSettings, body)
    return ok(null)
  }),

  /** 我的提交列表:status 过滤(缺省 all),分页(12.7) */
  http.get('/api/user/submissions', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const url = new URL(request.url)
    const status = url.searchParams.get('status') ?? 'all'
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 10)
    const list = mockSubmissions
      .filter((s) => status === 'all' || s.status === status)
      .map(toListItem)
    const start = (page - 1) * size
    return ok({ list: list.slice(start, start + size), total: list.length, page, size })
  }),

  /** 提交详情:查询最新状态(未批改超时后自动流转为已批改,验收 32) */
  http.get('/api/user/submissions/:id', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const sub = mockSubmissions.find((s) => s.id === params.id)
    if (!sub) return fail(404, '提交记录不存在')
    return ok(buildSubmitResult(refreshSubmissionStatus(sub)))
  }),

  /** 错题本列表:来源 / 题型筛选 + 分页(12.7) */
  http.get('/api/user/mistakes', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const url = new URL(request.url)
    const source = url.searchParams.get('source')
    const type = url.searchParams.get('type')
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 10)
    const list = mockMistakes.filter(
      (m) => (!source || m.source.type === source) && (!type || m.type === type)
    )
    const start = (page - 1) * size
    return ok({ list: list.slice(start, start + size), total: list.length, page, size })
  }),

  /** 删除错题(12.7) */
  http.delete('/api/user/mistakes/:id', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const idx = mockMistakes.findIndex((m) => m.id === params.id)
    if (idx < 0) return fail(404, '错题不存在')
    mockMistakes.splice(idx, 1)
    return ok(null)
  }),

  /** 收藏列表(12.7) */
  http.get('/api/user/favorites', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 10)
    const start = (page - 1) * size
    return ok({ list: mockFavorites.slice(start, start + size), total: mockFavorites.length, page, size })
  }),

  /** 新增收藏(12.7) */
  http.post('/api/user/favorites', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const body = (await request.json()) as { type: 'word' | 'sentence'; content: string; refId?: string }
    const item: FavoriteItem = {
      id: `fv-${favoriteSeq++}`,
      type: body.type,
      content: body.content,
      refId: body.refId,
      createdAt: new Date().toISOString(),
    }
    mockFavorites.unshift(item)
    return ok(item)
  }),

  /** 删除收藏(12.7) */
  http.delete('/api/user/favorites/:id', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const idx = mockFavorites.findIndex((f) => f.id === params.id)
    if (idx < 0) return fail(404, '收藏不存在')
    mockFavorites.splice(idx, 1)
    return ok(null)
  }),
]