// 学语法域 API 封装(任务书 12.6)
import { get } from './request'
import type { GrammarNode, GrammarSection } from '@/types/grammar'

/** 语法目录树 */
export function getGrammarTree(): Promise<GrammarNode[]> {
  return get<GrammarNode[]>('/grammar/tree')
}

/** 语法章节详情(Markdown 文本) */
export function getGrammarSection(id: string): Promise<GrammarSection> {
  return get<GrammarSection>(`/grammar/section/${id}`)
}