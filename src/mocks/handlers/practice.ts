// 刷题域 mock handlers:五种题型统一 list/detail/submit + AI 出题 + 智能练习(任务书 12.3)
// 判分约定:客观题按答案比对;AI 批改立即 graded,人工批改先 pending、超时流转(见 user.ts)
// 答案不下发:GET 接口一律经 stripAnswer 过滤(设计原则 7、验收 35)
import { http } from 'msw'
import { currentUser, delay, fail, ok } from './auth'
import { addMistake, addSubmission, buildSubmitResult, mockSubmissions, userSettings } from './user'
import { readingStore } from '../data/reading'
import { matchingStore } from '../data/matching'
import { clozeStore } from '../data/cloze'
import { grammarFillStore } from '../data/grammar-fill'
import { writingStore } from '../data/writing'
import { stripPracticeItem } from '../utils/stripAnswer'
import type {
  PracticeListItem,
  PracticeType,
  PracticeSubmitRequest,
  StoredPracticeItem,
  SubmitDetail,
} from '@/types/practice'
import { shuffle } from '@/utils/shuffle'

/** 各题型练习库索引(与 mocks/data 对应) */
const storesByType = {
  reading: readingStore,
  matching: matchingStore,
  cloze: clozeStore,
  'grammar-fill': grammarFillStore,
  writing: writingStore,
} as const

/** AI 出题内存副本:generate 生成的临时题目(id 不在静态题库中,提交时在此查) */
const generatedItems = new Map<string, StoredPracticeItem<PracticeType>>()

/** 各题型总分与单题分值(练习场景;模考分值见 exam.ts) */
const scoreRule: Record<PracticeType, { total: number; perQuestion: number }> = {
  reading: { total: 10, perQuestion: 2 },
  matching: { total: 10, perQuestion: 2 },
  cloze: { total: 20, perQuestion: 2 },
  'grammar-fill': { total: 15, perQuestion: 1.5 },
  writing: { total: 15, perQuestion: 15 },
}

/** 语法填空单空判分:按设置的大小写敏感偏好处理,并接受替代答案(12.3 注) */
function judgeGrammarBlank(
  userText: string,
  answer: string,
  alternates: string[] | undefined,
  caseSensitive: boolean
): boolean {
  const candidates = [answer, ...(alternates ?? [])]
  const input = userText.trim()
  return candidates.some((c) => {
    if (caseSensitive) return c === input
    return c.toLowerCase() === input.toLowerCase()
  })
}

/** 客观题判分:返回明细、得分与正确数(阅读/五选五/完形/语法填空) */
function gradeObjective(
  item: StoredPracticeItem<'reading' | 'matching' | 'cloze' | 'grammar-fill'>,
  answers: Record<number, string>
): { details: SubmitDetail[]; score: number; correctCount: number; totalCount: number } {
  const details: SubmitDetail[] = []
  let correctCount = 0
  const rule = scoreRule[item.content.type]
  const caseSensitive = userSettings.grammarFillCaseSensitive

  if (item.content.type === 'reading') {
    for (const q of item.content.questions) {
      const userAnswer = answers[q.id] ?? ''
      const isCorrect = userAnswer === q.answer
      if (isCorrect) correctCount++
      details.push({
        id: q.id,
        userAnswer,
        correctAnswer: q.answer,
        isCorrect,
        explanation: q.explanation,
      })
    }
  } else if (item.content.type === 'matching' || item.content.type === 'cloze') {
    for (const b of item.content.blanks) {
      const userAnswer = answers[b.id] ?? ''
      const isCorrect = userAnswer === b.answer
      if (isCorrect) correctCount++
      details.push({ id: b.id, userAnswer, correctAnswer: b.answer, isCorrect, explanation: b.explanation })
    }
  } else {
    // grammar-fill:支持提示词与替代答案,大小写敏感度按用户设置
    for (const b of item.content.blanks) {
      const userAnswer = answers[b.id] ?? ''
      const isCorrect = judgeGrammarBlank(userAnswer, b.answer, b.acceptAlternatives, caseSensitive)
      if (isCorrect) correctCount++
      details.push({ id: b.id, userAnswer, correctAnswer: b.answer, isCorrect, explanation: b.explanation })
    }
  }

  return {
    details,
    correctCount,
    totalCount: details.length,
    score: Math.round(correctCount * rule.perQuestion * 10) / 10,
  }
}

/** 根据提交记录刷新列表项的提交状态(取该题最近一次记录,任务书 11.4) */
function withSubmitInfo(item: StoredPracticeItem<PracticeType>): PracticeListItem {
  const rec = mockSubmissions
    .filter((s) => s.questionId === item.id)
    .sort((a, b) => b.createdAt - a.createdAt)[0]
  return {
    id: item.id,
    type: item.content.type,
    source: item.content.source,
    title: item.title,
    difficulty: item.difficulty,
    tags: item.tags,
    submitted: !!rec,
    submissionId: rec?.id,
    submissionStatus: rec?.status,
    score: rec?.score,
    totalScore: rec?.totalScore,
    lastSubmittedAt: rec?.submittedAt,
  }
}

export const practiceHandlers = [
  /** 题型来源列表:source 仅接受 real | teacher(任务书 11.1 注);缺省返回全部(12.3) */
  http.get('/api/practice/list', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const url = new URL(request.url)
    const type = url.searchParams.get('type') as PracticeType | null
    const source = url.searchParams.get('source')
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 10)
    if (!type || !(type in storesByType)) return fail(1004, '题型参数缺失或不合法')
    if (source && source !== 'real' && source !== 'teacher') return fail(1005, 'source 仅接受 real 或 teacher')

    // 收集匹配来源的全部题目(静态题库 + AI 生成项)
    const typeStores = [
      ...(storesByType[type] as StoredPracticeItem<PracticeType>[]),
      ...[...generatedItems.values()].filter((g) => g.content.type === type),
    ]
    const matched = typeStores.filter((it) => !source || it.content.source.type === source)

    const list: PracticeListItem[] = matched.map((it) => withSubmitInfo(it))
    const start = (page - 1) * size
    return ok({ list: list.slice(start, start + size), total: list.length, page, size })
  }),

  /** 题目详情:GET 一律不含答案与解析(12.3、验收 35) */
  http.get('/api/practice/detail/:type/:id', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const type = params.type as PracticeType
    if (!(type in storesByType)) return fail(1004, '题型参数不合法')
    const item =
      (storesByType[type] as StoredPracticeItem<PracticeType>[]).find((it) => it.id === params.id) ??
      generatedItems.get(String(params.id))
    if (!item) return fail(404, '题目不存在')
    return ok(stripPracticeItem(item as never))
  }),

  /** 提交作答:按题型判分;AI 立即出分(grared),人工批改先 pending(12.3) */
  http.post('/api/practice/detail/:type/:id/submit', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const type = params.type as PracticeType
    if (!(type in storesByType)) return fail(1004, '题型参数不合法')
    const item =
      (storesByType[type] as StoredPracticeItem<PracticeType>[]).find((it) => it.id === params.id) ??
      generatedItems.get(String(params.id))
    if (!item) return fail(404, '题目不存在')

    const body = (await request.json()) as PracticeSubmitRequest
    const gradingPreference = body.gradingMethod ?? userSettings.gradingMethod
    const rule = scoreRule[item.content.type]

    if (item.content.type === 'writing') {
      // 作文一律人工批改(设计原则 6):提交后进入 pending
      const submissionId = addSubmission({
        questionId: item.id,
        title: item.title,
        type: 'writing',
        source: item.content.source,
        status: 'pending',
        gradingMethod: 'manual',
        answers: body.answers,
        duration: body.duration,
        estimatedTime: '预计 24 小时内出结果',
      })
      return ok({
        submissionId,
        type: 'writing',
        status: 'pending',
        gradingMethod: 'manual',
        estimatedTime: '预计 24 小时内出结果',
      })
    }

    // 客观题判分
    const { details, correctCount, totalCount, score } = gradeObjective(
      item as StoredPracticeItem<'reading' | 'matching' | 'cloze' | 'grammar-fill'>,
      (body.answers ?? {}) as Record<number, string>
    )
    const isInstant = gradingPreference === 'ai'
    const status = isInstant ? 'graded' : 'pending'
    const gradingMethod = isInstant ? 'ai' : 'auto'

    // 错题自动入错题本(AI 即时批改时)
    if (isInstant) {
      for (const d of details) {
        if (d.isCorrect) continue
        const stem = pickStem(
          item as StoredPracticeItem<'reading' | 'matching' | 'cloze' | 'grammar-fill'>,
          d.id
        )
        addMistake({
          questionId: item.id,
          type: item.content.type,
          source: item.content.source,
          stem,
          userAnswer: d.userAnswer,
          correctAnswer: d.correctAnswer,
          explanation: d.explanation,
        })
      }
    }

    const submissionId = addSubmission({
      questionId: item.id,
      title: item.title,
      type: item.content.type,
      source: item.content.source,
      status,
      gradingMethod,
      score: isInstant ? score : undefined,
      totalScore: rule.total,
      correctCount: isInstant ? correctCount : undefined,
      totalCount,
      details,
      answers: body.answers,
      duration: body.duration,
      estimatedTime: isInstant ? undefined : '预计 24 小时内出结果',
    })

    const result = buildSubmitResult(mockSubmissions.find((s) => s.id === submissionId)!)
    return ok(result)
  }),

  /** AI 出题:点即生成、无参数面板;mock 从题库随机抽取并改写为 ai 来源(12.3) */
  http.post('/api/practice/generate', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const body = (await request.json()) as { type?: PracticeType }
    const type = body.type
    if (!type || !(type in storesByType)) return fail(1004, '题型参数缺失或不合法')
    const pool = storesByType[type] as StoredPracticeItem<PracticeType>[]
    const source = shuffle(pool)[0]
    const id = `${source.id}-ai-${Date.now()}`
    const generated: StoredPracticeItem<PracticeType> = {
      ...source,
      id,
      content: {
        ...source.content,
        id,
        source: { type: 'ai', meta: { model: 'spring-ai-demo', generatedAt: new Date().toISOString() } },
      },
    } as StoredPracticeItem<PracticeType>
    generatedItems.set(id, generated)
    return ok(stripPracticeItem(generated))
  }),

  /** 智能练习:随机返回一道 PracticeItem,type 从 item.type 取(10.5 快捷入口) */
  http.post('/api/practice/smart', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const allItems = (
      Object.values(storesByType) as StoredPracticeItem<PracticeType>[][]
    ).flat()
    const picked = shuffle(allItems)[0]
    return ok(stripPracticeItem(picked))
  }),
]

/** 从题目中提取题干摘要(错题本展示用) */
function pickStem(item: StoredPracticeItem<'reading' | 'matching' | 'cloze' | 'grammar-fill'>, id: number): string {
  if (item.content.type === 'reading') {
    return item.content.questions.find((q) => q.id === id)?.stem ?? ''
  }
  return `${item.title} 第 ${id} 空`
}