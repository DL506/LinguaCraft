// 学语法域 mock handlers(任务书 12.6)
import { http } from 'msw'
import { currentUser, delay, fail, ok } from './auth'
import { grammarSections, grammarTree } from '../data/grammar-tree'

export const grammarHandlers = [
  /** 语法目录树(12.6) */
  http.get('/api/grammar/tree', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    return ok(grammarTree)
  }),

  /** 语法章节详情:Markdown 内容由前端 markdown-it + highlight.js 渲染(12.6) */
  http.get('/api/grammar/section/:id', async ({ request, params }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const section = grammarSections.find((s) => s.id === params.id)
    if (!section) return fail(404, '章节不存在')
    return ok(section)
  }),
]