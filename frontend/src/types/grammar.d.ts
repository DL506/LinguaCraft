// 学语法域类型:语法目录树与章节内容(任务书 11.12、12.6)

/** 语法目录树节点 */
export interface GrammarNode {
  id: string
  /** 考点名称 */
  name: string
  /** 子考点(可递归) */
  children?: GrammarNode[]
}

/** 语法章节详情(12.6):内容为 Markdown 文本 */
export interface GrammarSection {
  id: string
  /** 章节标题 */
  title: string
  /** Markdown 内容:考点精讲 → 真题示例 → 易错点辨析 → 专项练习(任务书 10.14) */
  markdown: string
}