// 背单词域 mock handlers(任务书 12.5)
// 词书数据为演示量级,分页通过循环填充模拟大词库;真实词汇数据由后端契约提供
import { http } from 'msw'
import { currentUser, delay, fail, ok } from './auth'
import { userSettings } from './user'
import { wordStore } from '../data/words'
import type { VocabBook } from '@/types/vocab'

/** 词书定义(「我的词书」卡片展示 3 本) */
const vocabBooks: VocabBook[] = [
  { id: 'book-syllabus', name: '广东专插本考纲词汇', total: 3000, learned: 128 },
  { id: 'book-high', name: '高频核心词汇', total: 800, learned: 64 },
  { id: 'book-core', name: '核心进阶词汇', total: 1200, learned: 40 },
]

/** 今日背单词完成计数(与首页今日任务联动) */
export let vocabDoneCount = 0

/** 按词书取本地演示词集 */
function wordsOfBook(bookId: string) {
  if (bookId === 'book-high') return wordStore.filter((w) => w.level === 'high')
  if (bookId === 'book-core') return wordStore.filter((w) => w.level === 'core')
  if (bookId === 'book-syllabus') return wordStore
  return null
}

/** 循环虚拟分页:演示小数据模拟大词库分页(不足一页时循环复用) */
function paginateCycle<T>(base: T[], total: number, page: number, size: number) {
  const safeBase = base.length > 0 ? base : [base[0]] // 兜底:至少一个元素维持只读类型
  const start = ((page - 1) * size) % Math.max(base.length, 1)
  const list: T[] = []
  const begin = (page - 1) * size
  for (let i = 0; i < size && begin + i < total; i++) {
    list.push(safeBase[(start + i) % Math.max(base.length, 1)] as T)
  }
  return { list, total, page, size }
}

export const vocabHandlers = [
  /** 词书列表(12.5) */
  http.get('/api/vocab/books', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    return ok(vocabBooks)
  }),

  /** 词书单词分页(12.5) */
  http.get('/api/vocab/book/:bookId/words', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const base = wordsOfBook(String(params.bookId))
    if (!base) return fail(404, '词书不存在')
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 20)
    const book = vocabBooks.find((b) => b.id === params.bookId)!
    return ok(paginateCycle(base, book.total, page, size))
  }),

  /** 记忆自评提交:unknown/fuzzy/known,完成计数 +1(12.5) */
  http.post('/api/vocab/review', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    vocabDoneCount++
    return ok(null)
  }),

  /** 今日背单词任务:目标读用户设置(12.5) */
  http.get('/api/vocab/task/today', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    return ok({
      newCount: 8,
      reviewCount: 36,
      doneCount: vocabDoneCount,
      target: userSettings.dailyWordGoal,
    })
  }),

  /** 到期复习单词分页(12.5) */
  http.get('/api/vocab/task/due', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? 1)
    const size = Number(url.searchParams.get('size') ?? 20)
    const due = wordStore.filter((w) => w.level !== 'cognitive')
    return ok(paginateCycle(due, 36, page, size))
  }),
]