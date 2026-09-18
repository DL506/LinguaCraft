<!-- 完形填空页(任务书 10.9):桌面 文章区在上 + 题目列表在下 + 答题卡;手机 文章/题目 Tab 切换
     点文章空位:桌面滚动定位题目,手机弹底部选择面板;复用 P4 通用答题组件与状态机 -->
<template>
  <div class="cloze-page">
    <PracticeHeader
      title="完形填空"
      :source="source"
      :answering="state === 'answer'"
      :elapsed="elapsed"
      :submitting="submitting"
      @back="onBack"
      @update:source="onSourceChange"
      @submit="onSubmit"
    />

    <main class="cloze-page__main">
      <!-- 列表态 -->
      <div v-if="state === 'source' || state === 'list'" class="cloze-page__list">
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
      <div v-else-if="currentItem" class="cloze-page__answer">
        <!-- 手机:文章 / 题目 Tab 切换(10.9) -->
        <n-tabs v-if="!isDesktop" v-model:value="mobileTab" type="line">
          <n-tab-pane name="passage" tab="文章">
            <div class="answer__passage">
              <ClozePassage
                :passage="currentItem.content.passage"
                :answers="answers"
                :active-id="activeBlankId"
                :graded="state === 'review'"
                :details="gradedDetails"
                @select="onBlankSelect"
              />
            </div>
          </n-tab-pane>
          <n-tab-pane name="questions" tab="题目">
            <div class="answer__questions">
              <ClozeQuestion
                v-for="blank in currentItem.content.blanks"
                :id="blank.id"
                :key="blank.id"
                :options="blank.options"
                :model-value="answers[blank.id] ?? ''"
                :graded="state === 'review'"
                :detail="detailOf(blank.id)"
                :active="activeBlankId === blank.id"
                @update:model-value="onPick(blank.id, $event)"
              />
            </div>
          </n-tab-pane>
        </n-tabs>

        <!-- 桌面:文章区在上,题目列表在下,答题卡在底部(10.9) -->
        <template v-else>
          <div class="answer__passage">
            <ClozePassage
              :passage="currentItem.content.passage"
              :answers="answers"
              :active-id="activeBlankId"
              :graded="state === 'review'"
              :details="gradedDetails"
              @select="onBlankSelect"
            />
          </div>

          <div class="answer__questions-wrapper">
            <div class="answer__questions">
              <ClozeQuestion
                v-for="blank in currentItem.content.blanks"
                :id="blank.id"
                :key="blank.id"
                :options="blank.options"
                :model-value="answers[blank.id] ?? ''"
                :graded="state === 'review'"
                :detail="detailOf(blank.id)"
                :active="activeBlankId === blank.id"
                @update:model-value="onPick(blank.id, $event)"
              />
            </div>
          </div>
        </template>

        <!-- 答题卡:桌面在底部(10.9);移动端 Tab 底部同样可用 -->
        <div class="answer__sheet">
          <AnswerSheet
            :count="sheetCount"
            :answers="answers"
            :details="gradedDetails"
            @select="onSheetSelect"
          />
        </div>

        <n-alert v-if="state === 'review'" type="success" class="answer__result">
          已批改:得分 {{ resultScore }}/{{ resultTotalScore }}
        </n-alert>
      </div>
    </main>

    <!-- 手机:点击空位弹出的底部选项面板(10.9;内联实现,遵守任务书目录组件约定) -->
    <n-drawer v-model:show="pickerShow" placement="bottom" display-directive="show">
      <div class="picker">
        <p class="picker__title">选择选项</p>
        <div
          v-for="opt in activeBlankOptions"
          :key="opt.key"
          class="picker__item"
          @click="onPickerPick(opt.key)"
        >
          <span class="picker__key">{{ opt.key }}</span>
          <span class="picker__text">{{ opt.text }}</span>
        </div>
      </div>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 状态机与阅读理解/五选五一致;作答键:空位 id → 选项 key(与 12.3 契约一致)
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAlert, NDrawer, NTabPane, NTabs, useMessage } from 'naive-ui'
import PracticeHeader from '@/components/practice/shared/PracticeHeader.vue'
import PracticeSourceBar from '@/components/practice/shared/PracticeSourceBar.vue'
import PracticeListPage from '@/components/practice/shared/PracticeListPage.vue'
import AiGeneratingView from '@/components/common/AiGeneratingView.vue'
import PendingView from '@/components/common/PendingView.vue'
import ClozePassage from '@/components/practice/cloze/ClozePassage.vue'
import ClozeQuestion from '@/components/practice/cloze/ClozeQuestion.vue'
import AnswerSheet from '@/components/practice/reading/AnswerSheet.vue'
import { generatePractice, getPracticeDetail, getPracticeList, submitPractice } from '@/api/practice'
import { getSubmissionDetail } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useCountdown } from '@/composables/useCountdown'
import { usePracticeStore } from '@/stores/practice'
import type { PracticeItem, PracticeListItem, SourceType, SubmitDetail } from '@/types/practice'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { isDesktop, isMobile } = useDevice()
const practiceStore = usePracticeStore()

/** 练习时限:任务书未规定单篇时长,暂定 15 分钟并在超时自动提交;后端契约可覆盖 */
const LIMIT_SECONDS = 15 * 60
const { elapsed, remaining, start, reset } = useCountdown(LIMIT_SECONDS)

const state = ref<'source' | 'list' | 'ai' | 'answer' | 'pending' | 'review'>('source')
const source = ref<SourceType>('real')
/** 列表筛选:真题按年份,模拟题按老师名 */
const filterSelected = ref('全部')
const list = ref<PracticeListItem[]>([])
const currentItem = ref<PracticeItem<'cloze'> | null>(null)
/** 作答:空位 id → 选项 key */
const answers = ref<Record<number, string>>({})
/** 当前选中空位(文章侧高亮 + 题目高亮 + 手机面板) */
const activeBlankId = ref<number | null>(null)
const pickerShow = ref(false)
/** 手机文章/题目 Tab */
const mobileTab = ref('passage')

const gradedDetails = ref<SubmitDetail[]>([])
const submitting = ref(false)
const pendingRefreshing = ref(false)
const estimatedTime = ref('')
const pendingSubmissionId = ref('')
const aiFailed = ref(false)
let aiAborted = false

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

/** 答题卡题号:空位最大值(空位 id 与题序一致) */
const sheetCount = computed(() => currentItem.value?.content.blanks.length ?? 0)

/** 批改得分(正确数/总空数) */
const resultScore = computed(() => `${gradedDetails.value.filter((d) => d.isCorrect).length}`)
const resultTotalScore = computed(() => `${currentItem.value?.content.blanks.length ?? 0}`)

/** 手机面板:当前选中空位的选项列表 */
const activeBlankOptions = computed(() => {
  if (activeBlankId.value === null) return []
  return currentItem.value?.content.blanks.find((b) => b.id === activeBlankId.value)?.options ?? []
})

function detailOf(id: number): SubmitDetail | undefined {
  if (state.value !== 'review') return undefined
  return gradedDetails.value.find((d) => d.id === id)
}

async function fetchList(): Promise<void> {
  const res = await getPracticeList('cloze', { source: source.value === 'ai' ? 'real' : source.value })
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
    const [item] = await Promise.all([generatePractice('cloze'), minWait])
    if (aiAborted) return
    startAnswer(item as PracticeItem<'cloze'>)
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
  const detail = (await getPracticeDetail('cloze', id)) as PracticeItem<'cloze'>
  startAnswer(detail)
}

function startAnswer(item: PracticeItem<'cloze'>): void {
  currentItem.value = item
  answers.value = {}
  gradedDetails.value = []
  activeBlankId.value = null
  mobileTab.value = 'passage'
  reset(LIMIT_SECONDS)
  state.value = 'answer'
  start()
}

async function onReview(item: PracticeListItem): Promise<void> {
  const detail = (await getPracticeDetail('cloze', item.id)) as PracticeItem<'cloze'>
  currentItem.value = detail
  if (item.submissionId) {
    await loadSubmissionView(item.submissionId)
  }
}

async function loadSubmissionView(submissionId: string): Promise<void> {
  const sub = await getSubmissionDetail(submissionId)
  if (sub.type !== 'cloze') return
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

/** 文章侧点击空位:选中高亮;手机弹面板;桌面滚动定位到对应题目(10.9) */
function onBlankSelect(id: number): void {
  if (state.value === 'review') return
  activeBlankId.value = id
  if (isMobile.value) {
    mobileTab.value = 'passage'
    pickerShow.value = true
    return
  }
  // 桌面:滚动定位到该空位对应题目
  document.getElementById(`cloze-q-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

/** 手机面板选择 */
function onPickerPick(key: string): void {
  if (activeBlankId.value !== null) {
    answers.value = { ...answers.value, [activeBlankId.value]: key }
  }
  activeBlankId.value = null
}

/** 题目选项选择 */
function onPick(id: number, key: string): void {
  answers.value = { ...answers.value, [id]: key }
}

/** 答题卡定位 */
function onSheetSelect(id: number): void {
  if (isMobile.value) mobileTab.value = 'questions'
  activeBlankId.value = id
  document.getElementById(`cloze-q-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

async function onSubmit(): Promise<void> {
  if (!currentItem.value) return
  submitting.value = true
  try {
    const result = await submitPractice('cloze', currentItem.value.id, {
      answers: answers.value,
      duration: elapsed.value,
    })
    if (result.type !== 'cloze') return
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
    if (incoming.type === 'cloze') {
      startAnswer(incoming as PracticeItem<'cloze'>)
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
.cloze-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cloze-page__main {
  min-width: 0;
}

.cloze-page__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.cloze-page__answer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.answer__passage {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  /* 断词兜底 + 横向兜底(clip 不创建滚动容器),防溢出 */
  overflow-wrap: break-word;
  overflow-x: clip;
}

.answer__questions-wrapper {
  min-width: 0;
}

/* 桌面:题目列表两列排布 */
.answer__questions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 4px 12px;
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

@media (max-width: 1023px) {
  .answer__questions {
    grid-template-columns: minmax(0, 1fr);
  }
}

.answer__sheet {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.answer__result {
  max-width: 400px;
}

.picker {
  padding: 16px;
}

.picker__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 12px;
}

/* 面板列表项 ≥44px 触达高度(任务书第 16 章) */
.picker__item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-height: 44px;
  padding: 10px 12px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  cursor: pointer;
}

.picker__key {
  font-weight: 700;
  color: var(--lc-primary);
  flex-shrink: 0;
}

.picker__text {
  color: var(--lc-text-1);
  font-size: 13px;
  overflow-wrap: break-word;
}
</style>