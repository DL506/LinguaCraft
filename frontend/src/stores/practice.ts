// 刷题 store:P3 仅承载「智能练习/AI 出题 → 题型页」的题目传递(任务书 10.5)
// P4 起扩展各题型的作答态管理
import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { PracticeItem } from '@/types/practice'

export const usePracticeStore = defineStore('practice', () => {
  /** 待作答题目(智能练习返回后暂存,题型页进入时接管) */
  const incomingItem = ref<PracticeItem | null>(null)

  /** 暂存待作答题目 */
  function setIncomingItem(item: PracticeItem): void {
    incomingItem.value = item
  }

  /** 取出并清空(题型页接管后调用,避免重复进入旧题) */
  function takeIncomingItem(): PracticeItem | null {
    const item = incomingItem.value
    incomingItem.value = null
    return item
  }

  return { incomingItem, setIncomingItem, takeIncomingItem }
})