// 学语法 store(任务书 3 章目录项):目录树、当前章节、扁平叶子序列与上下节导航
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getGrammarSection, getGrammarTree } from '@/api/grammar'
import type { GrammarNode, GrammarSection } from '@/types/grammar'

export const useGrammarStore = defineStore('grammar', () => {
  /** 目录树(按考点组织,10.14) */
  const tree = ref<GrammarNode[]>([])
  /** 当前章节内容(id/title/markdown) */
  const currentSection = ref<GrammarSection | null>(null)
  /** 内容加载中 */
  const loading = ref(false)

  /** 扁平叶子序列(手机顶部 Tab 与上一节/下一节导航用) */
  const leaves = computed(() => {
    const result: GrammarNode[] = []
    const walk = (nodes: GrammarNode[]): void => {
      for (const node of nodes) {
        if (node.children?.length) walk(node.children)
        else result.push(node)
      }
    }
    walk(tree.value)
    return result
  })

  /** 当前章节在叶子序列中的下标(用于上下节禁用判断) */
  const currentIndex = computed(() => leaves.value.findIndex((n) => n.id === currentSection.value?.id))

  /** 初始化:拉目录树;若尚无选中章节则默认选中第一个 */
  async function init(): Promise<void> {
    tree.value = await getGrammarTree()
    if (!currentSection.value && leaves.value.length > 0) {
      await selectSection(leaves.value[0].id)
    }
  }

  /** 选择章节:拉取 markdown 内容(12.6) */
  async function selectSection(id: string): Promise<void> {
    loading.value = true
    try {
      currentSection.value = await getGrammarSection(id)
    } finally {
      loading.value = false
    }
  }

  /** 上一节(存在时) */
  async function selectPrev(): Promise<void> {
    const index = currentIndex.value
    if (index > 0) await selectSection(leaves.value[index - 1].id)
  }

  /** 下一节(存在时) */
  async function selectNext(): Promise<void> {
    const index = currentIndex.value
    if (index >= 0 && index < leaves.value.length - 1) await selectSection(leaves.value[index + 1].id)
  }

  return { tree, currentSection, loading, leaves, currentIndex, init, selectSection, selectPrev, selectNext }
})