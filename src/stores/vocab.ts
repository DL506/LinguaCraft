// 背单词 store(任务书 3 章目录项):词书列表、学习模式、今日任务、学习队列与游标
// 队列策略:保持至少 3 个前瞻词;队尾按模式继续分页拉取(新学=词书分页;复习=due 队列)
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getBookWords, getVocabBooks, getVocabDue, getVocabTodayTask, submitVocabReview } from '@/api/vocab'
import type { VocabBook, VocabTodayTask, Word } from '@/types/vocab'

/** 学习模式:新学 / 复习(10.13 顶部模式切换) */
export type VocabMode = 'new' | 'review'

/** 每次向服务端拉取的词量 */
const PAGE_SIZE = 20
/** 前瞻词保有量 */
const LOOKAHEAD = 3

export const useVocabStore = defineStore('vocab', () => {
  /** 词书列表 */
  const books = ref<VocabBook[]>([])
  /** 当前词书 id(默认内置考纲词书) */
  const currentBookId = ref('book-syllabus')
  /** 学习模式 */
  const mode = ref<VocabMode>('new')
  /** 今日任务进度(新学/复习/已完成/目标) */
  const todayTask = ref<VocabTodayTask>({ newCount: 0, reviewCount: 0, doneCount: 0, target: 0 })

  /** 学习队列与游标 */
  const queue = ref<Word[]>([])
  const cursor = ref(0)
  const page = ref(1)
  const isFetching = ref(false)

  /** 当前单词(游标处;null 表示队列耗尽,展示完成态) */
  const currentWord = computed(() => queue.value[cursor.value] ?? null)

  /** 当前词书对象 */
  const currentBook = computed(() => books.value.find((b) => b.id === currentBookId.value) ?? null)

  /** 初始化:词书列表 + 今日任务 */
  async function init(): Promise<void> {
    const [bookList, task] = await Promise.all([getVocabBooks(), getVocabTodayTask()])
    books.value = bookList
    todayTask.value = task
  }

  /** 拉取下一页单词(按当前模式) */
  async function fetchNextPage(): Promise<void> {
    if (isFetching.value) return
    isFetching.value = true
    try {
      const res =
        mode.value === 'new'
          ? await getBookWords(currentBookId.value, page.value, PAGE_SIZE)
          : await getVocabDue(page.value, PAGE_SIZE)
      if (res.list.length === 0) return
      queue.value.push(...res.list)
      page.value += 1
    } finally {
      isFetching.value = false
    }
  }

  /** 保证前瞻词充足 */
  async function ensureQueue(): Promise<void> {
    while (queue.value.length - cursor.value < LOOKAHEAD) {
      const before = queue.value.length
      await fetchNextPage()
      // 队列长度未增长则终止,避免死循环(mock 循环分页时不会触发)
      if (queue.value.length === before) break
    }
  }

  /** 重置队列(切换词书/模式时调用) */
  async function resetQueue(): Promise<void> {
    queue.value = []
    cursor.value = 0
    page.value = 1
    await ensureQueue()
  }

  /** 推进到下一词 */
  function advance(): void {
    cursor.value += 1
    void ensureQueue()
  }

  /** 评分提交:unknown/fuzzy/known → 推进并刷新今日进度(10.13 三按钮) */
  async function rateWord(rating: 'unknown' | 'fuzzy' | 'known'): Promise<void> {
    const word = currentWord.value
    if (!word) return
    await submitVocabReview({ wordId: word.id, rating })
    advance()
    // 刷新今日进度(服务端计数 +1)
    todayTask.value = await getVocabTodayTask()
  }

  /** 跳过:不提交评分,直接推进(手机左滑跳过) */
  function skipWord(): void {
    advance()
  }

  /** 切换词书 */
  async function setBook(bookId: string): Promise<void> {
    if (currentBookId.value === bookId) return
    currentBookId.value = bookId
    await resetQueue()
  }

  /** 切换模式 */
  async function setMode(next: VocabMode): Promise<void> {
    if (mode.value === next) return
    mode.value = next
    await resetQueue()
  }

  return {
    books,
    currentBookId,
    currentBook,
    mode,
    todayTask,
    queue,
    cursor,
    currentWord,
    init,
    resetQueue,
    ensureQueue,
    rateWord,
    skipWord,
    setBook,
    setMode,
  }
})