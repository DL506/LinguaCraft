<!-- 作文页(任务书 10.11):桌面 左题目 + 右写作区;手机 上下布局 + 字数统计固定底部
     字数实时统计(词汇数)、草稿自动保存(localStorage)、提交后按 status 进入批改态(评分维度+范文+总评)或 pending 态
     注:10.11 未规定超时自动提交,本页仅计时展示,不自动提交 -->
<template>
  <div class="writing-page">
    <PracticeHeader
      title="作文"
      :source="source"
      :answering="state === 'answer'"
      :elapsed="elapsed"
      :submitting="submitting"
      @back="onBack"
      @update:source="onSourceChange"
      @submit="onSubmit"
    />

    <main class="writing-page__main">
      <!-- 列表态 -->
      <div v-if="state === 'source' || state === 'list'" class="writing-page__list">
        <PracticeSourceBar
          :filter-chips="filterChips"
          :selected="filterSelected"
          @update:selected="filterSelected = $event"
        />
        <PracticeListPage :list="displayList" @start="onStart" @review="onReview" />
      </div>

      <!-- AI 生成态 -->
      <AiGeneratingView v-else-if="state === 'ai'" :failed="aiFailed" @cancel="onAiCancel" @retry="generateAi" />

      <!-- 待批改态(作文一律人工批改,提交后进入) -->
      <PendingView
        v-else-if="state === 'pending'"
        :estimated-time="estimatedTime"
        :refreshing="pendingRefreshing"
        @back="state = 'list'"
        @refresh="refreshPending"
      />

      <!-- 答题态:桌面左题目右写作区;手机上下 -->
      <div v-else-if="state === 'answer' && currentItem" class="writing-page__answer">
        <div class="answer">
          <WritingPrompt
            :prompt="currentItem.content.prompt"
            :requirements="currentItem.content.requirements"
            :word-limit="currentItem.content.wordLimit"
            :scoring-dimensions="currentItem.content.scoringDimensions"
          />
          <WritingEditor v-model="content" :word-limit="currentItem.content.wordLimit" />
        </div>
      </div>

      <!-- 批改态:得分 + 评分维度 + 范文 + 总评(WritingSubmitResult,10.11) -->
      <div v-else-if="state === 'review' && currentItem" class="writing-page__review">
        <n-alert type="success">已批改:得分 {{ resultScore }}/{{ resultTotalScore }}</n-alert>

        <div v-if="gradedDimensions.length > 0" class="review-card">
          <h3 class="review-card__title">评分维度</h3>
          <div v-for="dim in gradedDimensions" :key="dim.name" class="dimension">
            <span class="dimension__name">{{ dim.name }}</span>
            <span class="dimension__score">{{ dim.score }}</span>
            <span class="dimension__comment">{{ dim.comment }}</span>
          </div>
        </div>

        <div v-if="gradedSample" class="review-card">
          <h3 class="review-card__title">范文</h3>
          <p class="review-card__sample">{{ gradedSample }}</p>
        </div>

        <div v-if="gradedComment" class="review-card">
          <h3 class="review-card__title">总评</h3>
          <p class="review-card__comment">{{ gradedComment }}</p>
        </div>

        <div class="writing-page__review-actions">
          <n-button @click="state = 'list'">返回列表</n-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
// 状态机与前几种题型一致;作答键:{ content: string }(与 12.3 契约一致)
// 草稿自动保存:localStorage 键 lc-writing-draft-<题目 id>,提交成功后清除
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAlert, NButton, useMessage } from 'naive-ui'
import PracticeHeader from '@/components/practice/shared/PracticeHeader.vue'
import PracticeSourceBar from '@/components/practice/shared/PracticeSourceBar.vue'
import PracticeListPage from '@/components/practice/shared/PracticeListPage.vue'
import AiGeneratingView from '@/components/common/AiGeneratingView.vue'
import PendingView from '@/components/common/PendingView.vue'
import WritingPrompt from '@/components/practice/writing/WritingPrompt.vue'
import WritingEditor from '@/components/practice/writing/WritingEditor.vue'
import { generatePractice, getPracticeDetail, getPracticeList, submitPractice } from '@/api/practice'
import { getSubmissionDetail } from '@/api/user'
import { useCountdown } from '@/composables/useCountdown'
import { usePracticeStore } from '@/stores/practice'
import type { PracticeItem, PracticeListItem, SourceType, WritingSubmitResult } from '@/types/practice'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const practiceStore = usePracticeStore()

/** 计时仅作展示(10.11 未规定超时自动提交) */
const { elapsed, start, reset } = useCountdown(0)

const state = ref<'source' | 'list' | 'ai' | 'answer' | 'pending' | 'review'>('source')
const source = ref<SourceType>('real')
const filterSelected = ref('全部')
const list = ref<PracticeListItem[]>([])
const currentItem = ref<PracticeItem<'writing'> | null>(null)
/** 作文正文 */
const content = ref('')

/** 批改结果(WritingSubmitResult:维度/范文/总评) */
const gradedResult = ref<WritingSubmitResult | null>(null)
const submitting = ref(false)
const pendingRefreshing = ref(false)
const estimatedTime = ref('')
const pendingSubmissionId = ref('')
const aiFailed = ref(false)
let aiAborted = false

/** 草稿自动保存计时器(防抖) */
let draftTimer: number | undefined

/** 筛选 chip 过滤列表('全部' 不过滤) */
const displayList = computed(() =>
  filterSelected.value === '全部'
    ? list.value
    : list.value.filter((i) => chipOf(i) === filterSelected.value)
)

const filterChips = computed(() => ['全部', ...Array.from(new Set(list.value.map((i) => chipOf(i))))])
function chipOf(item: PracticeListItem): string {
  if (item.source.type === 'teacher') return item.source.meta.teacherName ?? item.title
  return item.source.meta.year ? String(item.source.meta.year) : item.title
}

const resultScore = computed(() => `${gradedResult.value?.score ?? 0}`)
const resultTotalScore = computed(() => `${gradedResult.value?.totalScore ?? 0}`)
const gradedDimensions = computed(() => gradedResult.value?.dimensions ?? [])
const gradedSample = computed(() => gradedResult.value?.sampleAnswer ?? '')
const gradedComment = computed(() => gradedResult.value?.overallComment ?? '')

async function fetchList(): Promise<void> {
  const res = await getPracticeList('writing', { source: source.value === 'ai' ? 'real' : source.value })
  list.value = res.list
}

async function onSourceChange(next: SourceType): Promise<void> {
  source.value = next
  if (next === 'ai') {
    state.value = 'ai'
    aiAborted = false
    generateAi()
    return
  }
  state.value = 'list'
  filterSelected.value = '全部'
  await fetchList()
}

async function generateAi(): Promise<void> {
  aiFailed.value = false
  const minWait = new Promise((resolve) => setTimeout(resolve, 10_000))
  try {
    const [item] = await Promise.all([generatePractice('writing'), minWait])
    if (aiAborted) return
    startAnswer(item as PracticeItem<'writing'>)
  } catch {
    aiFailed.value = true
  }
}

function onAiCancel(): void {
  aiAborted = true
  state.value = 'source'
}

async function onStart(item: PracticeListItem): Promise<void> {
  await loadAndAnswer(item.id)
}

async function loadAndAnswer(id: string): Promise<void> {
  const detail = (await getPracticeDetail('writing', id)) as PracticeItem<'writing'>
  startAnswer(detail)
}

/** 草稿存储键 */
function draftKey(itemId: string): string {
  return `lc-writing-draft-${itemId}`
}

function startAnswer(item: PracticeItem<'writing'>): void {
  currentItem.value = item
  gradedResult.value = null
  // 恢复草稿(10.11 草稿自动保存)
  content.value = localStorage.getItem(draftKey(item.id)) ?? ''
  reset(0)
  state.value = 'answer'
  start()
}

async function onReview(item: PracticeListItem): Promise<void> {
  const detail = (await getPracticeDetail('writing', item.id)) as PracticeItem<'writing'>
  currentItem.value = detail
  if (item.submissionId) {
    await loadSubmissionView(item.submissionId)
  }
}

async function loadSubmissionView(submissionId: string): Promise<void> {
  const sub = await getSubmissionDetail(submissionId)
  if (sub.type !== 'writing') return
  if (sub.status === 'pending') {
    pendingSubmissionId.value = submissionId
    estimatedTime.value = sub.estimatedTime ?? '预计 24 小时内出结果'
    state.value = 'pending'
    return
  }
  gradedResult.value = sub
  state.value = 'review'
}

async function refreshPending(): Promise<void> {
  if (!pendingSubmissionId.value) return
  pendingRefreshing.value = true
  try {
    await loadSubmissionView(pendingSubmissionId.value)
    if (state.value !== 'pending') message.success('已出结果')
    else message.info('仍在批改中,请稍后再查')
  } finally {
    pendingRefreshing.value = false
  }
}

async function onSubmit(): Promise<void> {
  if (!currentItem.value) return
  if (!content.value.trim()) {
    message.warning('请先完成作文再提交')
    return
  }
  submitting.value = true
  try {
    const result = await submitPractice('writing', currentItem.value.id, {
      answers: { content: content.value },
      duration: elapsed.value,
    })
    if (result.type !== 'writing') return
    // 作文一律人工批改:提交后进入 pending(设计原则 6、验收 30)
    pendingSubmissionId.value = result.submissionId
    estimatedTime.value = result.estimatedTime ?? '预计 24 小时内出结果'
    state.value = 'pending'
    // 提交成功清除草稿
    localStorage.removeItem(draftKey(currentItem.value.id))
    content.value = ''
  } catch {
    // 错误提示由 request 层统一弹出
  } finally {
    submitting.value = false
  }
}

function onBack(): void {
  if (state.value !== 'answer' && state.value !== 'pending' && state.value !== 'review') {
    router.back()
    return
  }
  state.value = 'list'
  fetchList()
}

/** 草稿自动保存:输入停止 500ms 后写入 localStorage(10.11) */
watch(content, (val) => {
  if (!currentItem.value || state.value !== 'answer') return
  window.clearTimeout(draftTimer)
  draftTimer = window.setTimeout(() => {
    if (val.trim()) localStorage.setItem(draftKey(currentItem.value!.id), val)
  }, 500)
})

onMounted(async () => {
  const queryId = String(route.query.id ?? '')
  if (queryId) {
    await loadAndAnswer(queryId)
    return
  }
  const incoming = practiceStore.takeIncomingItem()
  if (incoming) {
    if (incoming.type === 'writing') {
      startAnswer(incoming as PracticeItem<'writing'>)
      return
    }
    router.replace(`/practice/${incoming.type}`)
    return
  }
  await fetchList()
  state.value = 'list'
})
</script>

<style scoped>
/* 页面自然流式布局:不占用 100vh、不做内部滚动,由布局层内容区统一滚动 */
.writing-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.writing-page__main {
  min-width: 0;
}

.writing-page__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.writing-page__answer,
.writing-page__review {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

/* 答题区:桌面左题目右写作区(10.11);手机上下 */
.answer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: 16px;
  min-width: 0;
}

@media (max-width: 1023px) {
  .answer {
    grid-template-columns: minmax(0, 1fr);
  }
}

.review-card {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  overflow-wrap: break-word;
}

.review-card__title {
  font-size: 15px;
  color: var(--lc-text-1);
  margin-bottom: 12px;
}

.dimension {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--lc-border);
}

.dimension__name {
  min-width: 72px;
  color: var(--lc-text-1);
  font-size: 14px;
}

.dimension__score {
  min-width: 32px;
  font-weight: 700;
  color: var(--lc-primary);
}

.dimension__comment {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: var(--lc-text-2);
}

.review-card__sample,
.review-card__comment {
  font-size: 14px;
  line-height: 1.9;
  color: var(--lc-text-2);
  white-space: pre-wrap;
}

.writing-page__review-actions {
  display: flex;
  gap: 12px;
}
</style>