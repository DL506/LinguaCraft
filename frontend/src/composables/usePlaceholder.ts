// 占位符工具(任务书第 8 章):占位符统一为 [[n]],n 为 1 起的空格编号
// 用于把带占位符的文章拆分为文本/空位片段,供五选五、完形、语法填空渲染

/** 文章片段:文本或空位 */
export interface PassageSegment {
  type: 'text' | 'blank'
  content: string
  /** 空位编号(仅 type=blank 时有效) */
  id?: number
}

export function usePlaceholder() {
  /** 解析文本中的 [[n]] 占位符,返回按原文顺序排列的片段列表 */
  function parsePassage(text: string): PassageSegment[] {
    return text
      .split(/(\[\[\d+\]\])/)
      .map((part) => {
        const matched = part.match(/^\[\[(\d+)\]\]$/)
        if (matched) {
          return { type: 'blank' as const, content: part, id: Number(matched[1]) }
        }
        return { type: 'text' as const, content: part }
      })
      .filter((seg) => seg.content !== '')
  }

  return { parsePassage }
}