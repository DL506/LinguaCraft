// 真题模考域 mock handlers(任务书 12.4)
// 注意:GET /exam/list(静态段)必须先于 GET /exam/:id 声明,规避路径歧义(12.8)
// 批改流转:AI 模式交卷后客观题立即出分(partially_graded),作文延时后批完(graded);
// 人工模式交卷后 pending,客观题延时后先出分,作文再延时后批完(演示验收 27/29/30)
import { http } from 'msw'
import { currentUser, delay, fail, ok } from './auth'
import { userSettings } from './user'
import { examStore } from '../data/exam'
import { stripExamPaper } from '../utils/stripAnswer'
import type { ExamListItem, ExamReport, ExamSubmitRequest } from '@/types/exam'
import type { GradingStatus, PracticeType, SubmitDetail } from '@/types/practice'

/** 客观题延时出分阈值(人工模式演示) */
const OBJECTIVE_GRADE_DELAY_MS = 60 * 1000
/** 作文批改阈值(两种模式统一,作文一律人工批改) */
const WRITING_GRADE_DELAY_MS = 3 * 60 * 1000

/** 模考提交内部记录 */
interface ExamSubmissionRecord {
  submissionId: string
  examId: string
  objectiveAnswers: Record<number, string>
  writing?: { content: string }
  gradingPreference: 'ai' | 'manual'
  status: GradingStatus
  createdAt: number
  duration: number
  /** 客观题明细(判分即生成,状态到达前不下发) */
  details: SubmitDetail[]
  objectiveScore: number
  correctCount: number
  sectionScores: Record<number, number>
}

/** 模考提交内存表 */
const examSubmissions: ExamSubmissionRecord[] = []
let examSubSeq = 1

/** 语法填空单空判分(大小写敏感度读用户设置,12.3 注) */
function judgeBlank(
  userText: string,
  answer: string,
  alternates: string[] | undefined,
  caseSensitive: boolean
): boolean {
  const candidates = [answer, ...(alternates ?? [])]
  const input = (userText ?? '').trim()
  return candidates.some((c) => (caseSensitive ? c === input : c.toLowerCase() === input.toLowerCase()))
}

/** 对整卷客观题判分:答题卡全局编号 = 前序 section 题量累加 + 本节题序(11.7 编号映射规则) */
function gradeExamObjective(
  exam: (typeof examStore)[number],
  objective: Record<number, string>
): {
  details: SubmitDetail[]
  objectiveScore: number
  correctCount: number
  sectionScores: Record<number, number>
} {
  const details: SubmitDetail[] = []
  let correctCount = 0
  const sectionScores: Record<number, number> = {}
  const caseSensitive = userSettings.grammarFillCaseSensitive

  for (const section of exam.sections) {
    let partScore = 0
    if (section.type === 'reading' && section.content.type === 'reading') {
      const per = 2 // 15 题 30 分
      for (const q of section.content.questions) {
        const userAnswer = objective[q.id] ?? ''
        const isCorrect = userAnswer === q.answer
        if (isCorrect) {
          correctCount++
          partScore += per
        }
        details.push({ id: q.id, userAnswer, correctAnswer: q.answer, isCorrect, explanation: q.explanation })
      }
    } else if (section.type === 'matching' && section.content.type === 'matching') {
      const per = 2 // 5 题 10 分
      for (const b of section.content.blanks) {
        const globalId = 15 + b.id // 匹配段起始 16
        const userAnswer = objective[globalId] ?? ''
        const isCorrect = userAnswer === b.answer
        if (isCorrect) {
          correctCount++
          partScore += per
        }
        details.push({ id: globalId, userAnswer, correctAnswer: b.answer, isCorrect, explanation: b.explanation })
      }
    } else if (section.type === 'cloze' && section.content.type === 'cloze') {
      const per = 2 // 15 题 30 分
      for (const b of section.content.blanks) {
        const globalId = 20 + b.id // 完形段起始 21
        const userAnswer = objective[globalId] ?? ''
        const isCorrect = userAnswer === b.answer
        if (isCorrect) {
          correctCount++
          partScore += per
        }
        details.push({ id: globalId, userAnswer, correctAnswer: b.answer, isCorrect, explanation: b.explanation })
      }
    } else if (section.type === 'grammar-fill' && section.content.type === 'grammar-fill') {
      const per = 1.5 // 10 题 15 分
      for (const b of section.content.blanks) {
        const globalId = 35 + b.id // 语法填空段起始 36
        const userAnswer = objective[globalId] ?? ''
        const isCorrect = judgeBlank(userAnswer, b.answer, b.acceptAlternatives, caseSensitive)
        if (isCorrect) {
          correctCount++
          partScore += per
        }
        details.push({ id: globalId, userAnswer, correctAnswer: b.answer, isCorrect, explanation: b.explanation })
      }
    }
    sectionScores[section.part] = Math.round(partScore * 10) / 10
  }

  // 客观总分 = 各 section 得分之和(0~85)
  const objectiveScore = Object.values(sectionScores).reduce((sum, s) => sum + s, 0)
  return { details, objectiveScore, correctCount, sectionScores }
}

/** 按交卷后经过时长推进记录状态(供 report 与后续查询调用) */
function refreshExamStatus(rec: ExamSubmissionRecord): void {
  const elapsed = Date.now() - rec.createdAt
  if (rec.status === 'pending' && elapsed >= OBJECTIVE_GRADE_DELAY_MS) {
    rec.status = 'partially_graded'
  }
  if (rec.status !== 'graded' && elapsed >= WRITING_GRADE_DELAY_MS) {
    rec.status = 'graded'
  }
}

/** 构建成绩报告(12.4):含各部分得分、薄弱项、历年对比 */
function buildReport(rec: ExamSubmissionRecord, exam: (typeof examStore)[number]) {
  refreshExamStatus(rec)
  const writingGraded = rec.status === 'graded'
  const writingScore = writingGraded ? 11 : 0
  const score = Math.round((rec.objectiveScore + writingScore) * 10) / 10

  const typeName: Record<PracticeType, string> = {
    reading: '阅读理解',
    matching: '五选五',
    cloze: '完形填空',
    'grammar-fill': '语法填空',
    writing: '写作',
  }
  const sections = exam.sections.map((s) => ({
    part: s.part,
    type: s.type,
    score: s.type === 'writing' ? writingScore : (rec.sectionScores[s.part] ?? 0),
    total: s.score,
  }))
  const weakPoints = sections
    .filter((s) => s.total > 0 && s.score / s.total < 0.6)
    .map((s) => `${typeName[s.type]}得分率 ${Math.round((s.score / s.total) * 100)}%,建议加强专项练习`)
  if (weakPoints.length === 0) weakPoints.push('整体表现不错,保持练习节奏')

  const history = [
    { year: 2021, score: 62 },
    { year: 2022, score: 68 },
    { year: 2023, score: 71 },
    { year: 2024, score: 75 },
    { year: 2025, score: 73 },
  ]
  if (writingGraded) history.push({ year: exam.year, score })

  const report: ExamReport = {
    score: writingGraded ? score : rec.objectiveScore,
    totalScore: exam.totalScore,
    sections,
    weakPoints,
    compareWithHistory: history.sort((a, b) => a.year - b.year),
    objectiveGraded: rec.status !== 'pending',
    writingGraded,
    writingScore: writingGraded ? writingScore : undefined,
  }
  return report
}

export const examHandlers = [
  /** 模考列表:按年份倒序,支持年份筛选与分页;静态段先于动态段声明(12.4、12.8) */
  http.get('/api/exam/list', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const url = new URL(request.url)
    const year = url.searchParams.get('year')
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 10)
    const matched = examStore
      .filter((e) => !year || e.year === Number(year))
      .map((e): ExamListItem => {
        const latest = examSubmissions
          .filter((s) => s.examId === e.id)
          .sort((a, b) => b.createdAt - a.createdAt)[0]
        return {
          id: e.id,
          year: e.year,
          title: e.title,
          totalScore: e.totalScore,
          duration: e.duration,
          lastScore: latest ? buildReport(latest, e).score : undefined,
          lastSubmittedAt: latest?.createdAt ? new Date(latest.createdAt).toISOString() : undefined,
        }
      })
    const start = (page - 1) * size
    return ok({ list: matched.slice(start, start + size), total: matched.length, page, size })
  }),

  /** 整卷详情:不含答案与解析(12.4、验收 35) */
  http.get('/api/exam/:id', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const exam = examStore.find((e) => e.id === params.id)
    if (!exam) return fail(404, '试卷不存在')
    return ok(stripExamPaper(exam))
  }),

  /** 交卷:客观题统一判分;状态按批改偏好分流(12.4) */
  http.post('/api/exam/:id/submit', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const exam = examStore.find((e) => e.id === params.id)
    if (!exam) return fail(404, '试卷不存在')
    const body = (await request.json()) as ExamSubmitRequest
    const preference = body.gradingMethod ?? userSettings.gradingMethod
    const { details, objectiveScore, correctCount, sectionScores } = gradeExamObjective(
      exam,
      body.objective ?? {}
    )

    const rec: ExamSubmissionRecord = {
      submissionId: `exam-sub-${examSubSeq++}`,
      examId: exam.id,
      objectiveAnswers: body.objective ?? {},
      writing: body.writing,
      gradingPreference: preference,
      // AI:客观立即出分;人工:整卷待批改(验收 29/30)
      status: preference === 'ai' ? 'partially_graded' : 'pending',
      createdAt: Date.now(),
      duration: body.duration,
      details,
      objectiveScore,
      correctCount,
      sectionScores,
    }
    examSubmissions.unshift(rec)

    // 整卷交卷响应统一 SubmitResult;type 字段以 reading 占位(整卷无单一题型,报告见 report 接口)
    const submitResult: {
      submissionId: string
      type: PracticeType
      status: GradingStatus
      gradingMethod: 'ai' | 'auto'
      objectiveGraded: boolean
      writingGraded: boolean
      score?: number
      totalScore: number
      correctCount?: number
      totalCount: number
      estimatedTime?: string
    } = {
      submissionId: rec.submissionId,
      type: 'reading',
      status: rec.status,
      gradingMethod: preference === 'ai' ? 'ai' : 'auto',
      objectiveGraded: rec.status !== 'pending',
      writingGraded: false,
      score: rec.status !== 'pending' ? rec.objectiveScore : undefined,
      totalScore: exam.totalScore,
      correctCount: rec.status !== 'pending' ? correctCount : undefined,
      totalCount: 45,
      estimatedTime: rec.status === 'pending' ? '预计 24 小时内出结果' : undefined,
    }
    return ok(submitResult)
  }),

  /** 成绩报告:按当前批改进度返回(12.4);partially_graded 时作文显示待批改 */
  http.get('/api/exam/submission/:submissionId/report', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const rec = examSubmissions.find((s) => s.submissionId === params.submissionId)
    if (!rec) return fail(404, '提交记录不存在')
    const exam = examStore.find((e) => e.id === rec.examId)
    if (!exam) return fail(404, '试卷不存在')
    return ok(buildReport(rec, exam))
  }),
]