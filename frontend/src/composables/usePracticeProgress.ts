// 作答进度断点续答(扩展契约 E1):进入答题时恢复、作答变更时防抖保存;
// 提交成功后由服务端清除进度(前端不额外处理)。
// 口径:未提交作答进度由后端数据库保存(项目既定决策,前端不做本地草稿)。
import { onBeforeUnmount, watch } from 'vue'
import type { Ref } from 'vue'
import { getPracticeProgress, savePracticeProgress } from '@/api/practice'
import type { PracticeType } from '@/types/practice'

/** 保存防抖间隔(毫秒):避免高频点选触发过多请求 */
const SAVE_DEBOUNCE_MS = 1500

/**
 * 练习作答进度同步(五种题型通用)。
 * 用法:在进入答题态后调用 restoreCurrent();其余由内部 watch 自动完成。
 */
export function usePracticeProgress(options: {
  /** 题型 */
  type: PracticeType
  /** 作答数据引用(客观题 Record<题号,答案>;写作 { content }) */
  answers: Ref<Record<number, string>>
  /** 当前题目 id(未进入答题态时返回 null) */
  itemId: () => string | null
  /** 是否处于答题中(批改/回看态不保存) */
  isAnswering: () => boolean
  /** 已作答耗时(秒) */
  duration: () => number
}) {
  let timer: ReturnType<typeof setTimeout> | undefined

  /** 恢复进度:命中则写入 answers(失败静默,不阻断答题) */
  async function restoreCurrent(): Promise<void> {
    const id = options.itemId()
    if (!id) return
    try {
      const data = await getPracticeProgress(options.type, id)
      if (data?.answers && typeof data.answers === 'object') {
        Object.assign(options.answers.value, data.answers as Record<number, string>)
      }
    } catch {
      // 静默:恢复失败不影响正常作答
    }
  }

  // 作答变更 → 防抖保存(仅答题态)
  watch(
    options.answers,
    () => {
      if (!options.isAnswering()) return
      const id = options.itemId()
      if (!id) return
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        savePracticeProgress(options.type, id, {
          answers: options.answers.value,
          duration: options.duration(),
        }).catch(() => {
          // 静默:保存失败不影响作答(下次变更会重试)
        })
      }, SAVE_DEBOUNCE_MS)
    },
    { deep: true }
  )

  onBeforeUnmount(() => {
    if (timer) clearTimeout(timer)
  })

  return { restoreCurrent }
}