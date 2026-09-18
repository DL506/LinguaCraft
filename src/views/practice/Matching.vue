<!-- 五选五页(任务书 10.8):三端点选(无拖拽),支持两种选择顺序;复用 P4 通用答题组件与状态机 -->
<template>
  <div class="matching-page">
    <PracticeHeader
      title="五选五"
      :source="source"
      :answering="state === 'answer'"
      :elapsed="elapsed"
      :submitting="submitting"
      @back="onBack"
      @update:source="onSourceChange"
      @submit="onSubmit"
    />

    <main class="matching-page__main">
      <!-- 列表态 -->
      <div v-if="state === 'source' || state === 'list'" class="matching-page__list">
        <PracticeSourceBar
          :filter-chips="filterChips"
          :selected="filterSelected"
          @update:selected="filterSelected = $event"
        />
        <PracticeListPage :list="displayList" @start="onStart" @review="onReview" />
      </div>

      <!-- AI 生成态 -->
      <AiGeneratingView v-else-if="state === 'ai'" :failed="aiFailed" @cancel="onAiCancel" @retry="generateAi" />

      <!-- 待批改态 -->
      <PendingView
        v-else-if="state === 'pending'"
        :estimated-time="estimatedTime"
        :refreshing="pendingRefreshing"
        @back="state = 'list'"
        @refresh="refreshPending"
      />

      <!-- 答题 / 批改态 -->
      <div v-else-if="currentItem" class="matching-page__answer">
        <div class="answer">
          <!-- 选项区(10.8):点击选择,已用置灰 -->
          <OptionPool
            :options="currentItem.content.options"
            :used-keys="usedKeys"
            :active-key="activeOptionKey"
            @pick="onOptionClick"
          />

          <!-- 文章区:文本 + 空位槽位(占位符统一 [[n]]) -->
          <div class="answer__passage">
            <template v-for="(seg, index) in segments" :key="index">
              <template v-if="seg.type === 'text'">{{ seg.content }}</template>
              <BlankSlot
                v-else
                :id="seg.id ?? 0"
                :answer-key="answers[seg.id ?? 0]"
                :active="activeBlankId === seg.id"
                :graded="state === 'review'"
                :detail="detailOf(seg.id ?? 0)"
                @click="onBlankClick(seg.id ?? 0)"
                @remove="onBlankRemove(seg.id ?? 0)"
              />
            </template>
          </div>
        </div>

        <!-- 批改结果横幅 -->
        <n-alert v-if="state === 'review'" type="success" class="answer__result">
          已批改:得分 {{ resultScore }}/{{ resultTotalScore }}
        </n-alert>
      </div>
    </main>

    <!-- 手机:点击空格弹出的选项选择面板(10.8) -->
    <OptionPickerDrawer
      v-model:show="pickerShow"
      :options="currentItem?.content.options ?? []"
      :used-keys="usedKeys"
      @pick="onPickerPick"
    />
  </div>
</template>

<script setup lang="ts">
// 状态机与阅读理解一致:source/list → answer → review/pending
// 交互核心(任务书 10.8):两种顺序——先点空格再点选项 / 先点选项再点空格;已填点空格可移除
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAlert, useMessage } from 'naive-ui'
import PracticeHeader from '@/components/practice/shared/PracticeHeader.vue'
import PracticeSourceBar from '@/components/practice/shared/PracticeSourceBar.vue'
import PracticeListPage from '@/components/practice/shared/PracticeListPage.vue'
import AiGeneratingView from '@/components/common/AiGeneratingView.vue'
import PendingView from '@/components/common/PendingView.vue'
import OptionPool from '@/components/practice/matching/OptionPool.vue'
import BlankSlot from '@/components/practice/matching/BlankSlot.vue'
import OptionPickerDrawer from '@/components/practice/matching/OptionPickerDrawer.vue'
import { generatePractice, getPracticeDetail, getPracticeList, submitPractice } from '@/api/practice'
import { getSubmissionDetail } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useCountdown } from '@/composables/useCountdown'
import { usePlaceholder } from '@/composables/usePlaceholder'
import { usePracticeStore } from '@/stores/practice'
import type { PracticeItem, PracticeListItem, SourceType, SubmitDetail } from '@/types/practice'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { isMobile } = useDevice()
const practiceStore = usePracticeStore()
const { parsePassage } = usePlaceholder()

/** 练习时限:任务书未规定单篇时长,暂定 15 分钟并在超时自动提交;后端契约可覆盖 */
const LIMIT_SECONDS = 15 * 60
const { elapsed, remaining, start, reset } = useCountdown(LIMIT_SECONDS)

const state = ref<'source' | 'list' | 'ai' | 'answer' | 'pending' | 'review'>('source')
const source = ref<SourceType>('real')
const filterSelected = ref('全部')
const list = ref<PracticeListItem[]>([])
const currentItem = ref<PracticeItem<'matching'> | null>(null)
/** 作答:空格 id → 选项 key */
const answers = ref<Record<number, string>>({})
/** 待匹配状态(任务书 10.8 参考实现) */
const activeBlankId = ref<number | null>(null)
const activeOptionKey = ref<string | null>(null)
/** 手机选择面板 */
const pickerShow = ref(false)

const gradedDetails = ref<SubmitDetail[]>([])
const submitting = ref(false)
const pendingRefreshing = ref(false)
const estimatedTime = ref('')
const pendingSubmissionId = ref('')
const aiFailed = ref(false)
let aiAborted = false

/** 占位符解析:文章 → 文本/空位片段 */
const segments = computed(() => (currentItem.value ? parsePassage(currentItem.value.content.passage) : []))

/** 已被使用的选项(置灰) */
const usedKeys = computed(() => Object.values(answers.value))

/** 批改得分(正确空数/总数) */
const resultScore = computed(() => `${gradedDetails.value.filter((d) => d.isCorrect).length}`)
const resultTotalScore = computed(() => `${currentItem.value?.content.blanks.length ?? 0}`)

const filterChips = computed(() => ['全部', ...Array.from(new Set(list.value.map((i) => chipOf(i))))])
function chipOf(item: PracticeListItem): string {
  if (item.source.type === 'teacher') return item.source.meta.teacherName ?? item.title
  return item.source.meta.year ? String(item.source.meta.year) : item.title
}

/** 按当前筛选 chip 过滤列表('全部' 不过滤) */
const displayList = computed(() =>
  filterSelected.value === '全部'
    ? list.value
    : list.value.filter((i) => chipOf(i) === filterSelected.value)
)

function detailOf(id: number): SubmitDetail | undefined {
  if (state.value !== 'review') return undefined
  return gradedDetails.value.find((d) => d.id === id)
}

async function fetchList(): Promise<void> {
  const res = await getPracticeList('matching', { source: source.value === 'ai' ? 'real' : source.value })
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
    const [item] = await Promise.all([generatePractice('matching'), minWait])
    if (aiAborted) return
    startAnswer(item as PracticeItem<'matching'>)
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
  const detail = (await getPracticeDetail('matching', id)) as PracticeItem<'matching'>
  startAnswer(detail)
}

function startAnswer(item: PracticeItem<'matching'>): void {
  currentItem.value = item
  answers.value = {}
  gradedDetails.value = []
  activeBlankId.value = null
  activeOptionKey.value = null
  reset(LIMIT_SECONDS)
  state.value = 'answer'
  start()
}

async function onReview(item: PracticeListItem): Promise<void> {
  const detail = (await getPracticeDetail('matching', item.id)) as PracticeItem<'matching'>
  currentItem.value = detail
  if (item.submissionId) {
    await loadSubmissionView(item.submissionId)
  }
}

async function loadSubmissionView(submissionId: string): Promise<void> {
  const sub = await getSubmissionDetail(submissionId)
  if (sub.type !== 'matching') return
  if (sub.status === 'pending') {
    pendingSubmissionId.value = submissionId
    estimatedTime.value = sub.estimatedTime ?? '预计 24 小时内出结果'
    state.value = 'pending'
    return
  }
  const details = sub.details ?? []
  gradedDetails.value = details
  answers.value = Object.fromEntries(details.map((d) => [d.id, d.userAnswer])) as Record<number, string>
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

/** 空格点击(10.8):已填 → 移除;有选中选项 → 填入;否则选中并(手机)弹面板 */
function onBlankClick(blankId: number): void {
  if (state.value === 'review') return
  if (answers.value[blankId]) {
    onBlankRemove(blankId)
    return
  }
  if (activeOptionKey.value) {
    answers.value = { ...answers.value, [blankId]: activeOptionKey.value }
    activeOptionKey.value = null
    return
  }
  activeBlankId.value = blankId
  if (isMobile.value) pickerShow.value = true
}

/** 选项点击(10.8):已用不可选;有选中空格 → 填入;否则选中待匹配 */
function onOptionClick(key: string): void {
  if (state.value === 'review') return
  if (usedKeys.value.includes(key)) return
  if (activeBlankId.value !== null) {
    const blankId = activeBlankId.value
    answers.value = { ...answers.value, [blankId]: key }
    activeBlankId.value = null
    return
  }
  activeOptionKey.value = key
}

/** 手机面板选择:填入选中的空格 */
function onPickerPick(key: string): void {
  if (activeBlankId.value !== null) {
    const blankId = activeBlankId.value
    answers.value = { ...answers.value, [blankId]: key }
  }
  activeBlankId.value = null
  activeOptionKey.value = null
}

/** 移除已填(点空格或移除标记) */
function onBlankRemove(blankId: number): void {
  const next = { ...answers.value }
  delete next[blankId]
  answers.value = next
}

async function onSubmit(): Promise<void> {
  if (!currentItem.value) return
  submitting.value = true
  try {
    const result = await submitPractice('matching', currentItem.value.id, {
      answers: answers.value,
      duration: elapsed.value,
    })
    if (result.type !== 'matching') return
    if (result.status === 'pending') {
      pendingSubmissionId.value = result.submissionId
      estimatedTime.value = result.estimatedTime ?? '预计 24 小时内出结果'
      state.value = 'pending'
    } else {
      gradedDetails.value = result.details ?? []
      state.value = 'review'
      message.success(`提交成功,得分 ${result.score}/${result.totalScore}`)
    }
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

/** 计时超时自动提交 */
watch(remaining, (val) => {
  if (val <= 0 && state.value === 'answer') {
    message.warning('本题时限已到,已为你自动提交')
    onSubmit()
  }
})

onMounted(async () => {
  const queryId = String(route.query.id ?? '')
  if (queryId) {
    await loadAndAnswer(queryId)
    return
  }
  const incoming = practiceStore.takeIncomingItem()
  if (incoming) {
    if (incoming.type === 'matching') {
      startAnswer(incoming as PracticeItem<'matching'>)
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
.matching-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.matching-page__main {
  min-width: 0;
}

.matching-page__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.matching-page__answer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.answer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 允许收缩,防长内容撑爆横向(min-content 溢出惯例) */
  min-width: 0;
}

.answer__passage {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  line-height: 2.2;
  color: var(--lc-text-1);
  /* 断词兜底 + 横向兜底(clip 不创建滚动容器);纵向滚动交给布局内容区 */
  overflow-wrap: break-word;
  overflow-x: clip;
}

.answer__result {
  max-width: 400px;
}
</style>