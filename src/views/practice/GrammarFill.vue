<!-- 语法填空页(任务书 10.10):桌面 文章区在上 + 填空列表在下;手机点文章空格弹底部输入框
     每空有提示词或无提示词;输入框 size="large";提交后逐空显示对错并可展开解析
     大小写敏感为服务端生效的判分偏好(submit 不携带,见任务书 12.3 注) -->
<template>
  <div class="grammar-fill-page">
    <PracticeHeader
      title="语法填空"
      :source="source"
      :answering="state === 'answer'"
      :elapsed="elapsed"
      :submitting="submitting"
      @back="onBack"
      @update:source="onSourceChange"
      @submit="onSubmit"
    />

    <main class="grammar-fill-page__main">
      <!-- 列表态 -->
      <div v-if="state === 'source' || state === 'list'" class="grammar-fill-page__list">
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
      <div v-else-if="currentItem" class="grammar-fill-page__answer">
        <!-- 文章区(桌面与手机均在上;手机上点空位弹底部输入框) -->
        <div class="answer__passage">
          <FillPassage
            :passage="currentItem.content.passage"
            :answers="answers"
            :active-id="activeBlankId"
            :graded="state === 'review'"
            :details="gradedDetails"
            @select="onBlankSelect"
          />
        </div>

        <!-- 填空列表:桌面双列,手机单列 -->
        <div class="answer__blanks">
          <FillBlank
            v-for="blank in currentItem.content.blanks"
            :id="blank.id"
            :key="blank.id"
            :hint="blank.hint"
            :model-value="answers[blank.id] ?? ''"
            :graded="state === 'review'"
            :detail="detailOf(blank.id)"
            :active="activeBlankId === blank.id"
            @update:model-value="onInput(blank.id, $event)"
          />
        </div>

        <n-alert v-if="state === 'review'" type="success" class="answer__result">
          已批改:得分 {{ resultScore }}/{{ resultTotalScore }}
        </n-alert>
      </div>
    </main>

    <!-- 手机:点击空格弹出的底部输入框(10.10;内联实现,遵守目录组件约定) -->
    <n-drawer v-model:show="inputDrawerShow" placement="bottom" display-directive="show">
      <div class="input-drawer">
        <p class="input-drawer__title">
          空 {{ activeBlankId }}
          <span v-if="activeBlankHint" class="input-drawer__hint">({{ activeBlankHint }})</span>
          <span v-else class="input-drawer__hint">无提示词</span>
        </p>
        <n-input
          v-model:value="draftValue"
          size="large"
          placeholder="输入答案"
          @keydown.enter="confirmDrawer"
        />
        <n-button type="primary" block class="input-drawer__confirm" @click="confirmDrawer">确认</n-button>
      </div>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 状态机与完形填空一致;作答键:空位 id → 填空文本(与 12.3 契约一致)
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAlert, NButton, NDrawer, NInput, useMessage } from 'naive-ui'
import PracticeHeader from '@/components/practice/shared/PracticeHeader.vue'
import PracticeSourceBar from '@/components/practice/shared/PracticeSourceBar.vue'
import PracticeListPage from '@/components/practice/shared/PracticeListPage.vue'
import AiGeneratingView from '@/components/common/AiGeneratingView.vue'
import PendingView from '@/components/common/PendingView.vue'
import FillPassage from '@/components/practice/grammar-fill/FillPassage.vue'
import FillBlank from '@/components/practice/grammar-fill/FillBlank.vue'
import { generatePractice, getPracticeDetail, getPracticeList, submitPractice } from '@/api/practice'
import { getSubmissionDetail } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useCountdown } from '@/composables/useCountdown'
import { usePracticeStore } from '@/stores/practice'
import type { PracticeItem, PracticeListItem, SourceType, SubmitDetail } from '@/types/practice'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { isMobile } = useDevice()
const practiceStore = usePracticeStore()

/** 练习时限:任务书未规定单篇时长,暂定 15 分钟并在超时自动提交;后端契约可覆盖 */
const LIMIT_SECONDS = 15 * 60
const { elapsed, remaining, start, reset } = useCountdown(LIMIT_SECONDS)

const state = ref<'source' | 'list' | 'ai' | 'answer' | 'pending' | 'review'>('source')
const source = ref<SourceType>('real')
/** 列表筛选:真题按年份,老师出题按老师名 */
const filterSelected = ref('全部')
const list = ref<PracticeListItem[]>([])
const currentItem = ref<PracticeItem<'grammar-fill'> | null>(null)
/** 作答:空位 id → 填空文本 */
const answers = ref<Record<number, string>>({})
/** 当前选中空位(文章侧高亮 + 填空高亮 + 手机输入框) */
const activeBlankId = ref<number | null>(null)
/** 手机输入抽屉 */
const inputDrawerShow = ref(false)
const draftValue = ref('')

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

/** 当前选中空位的提示词(手机抽屉标题用) */
const activeBlankHint = computed(() => {
  if (activeBlankId.value === null) return ''
  return currentItem.value?.content.blanks.find((b) => b.id === activeBlankId.value)?.hint ?? ''
})

/** 批改得分(正确数/总空数) */
const resultScore = computed(() => `${gradedDetails.value.filter((d) => d.isCorrect).length}`)
const resultTotalScore = computed(() => `${currentItem.value?.content.blanks.length ?? 0}`)

function detailOf(id: number): SubmitDetail | undefined {
  if (state.value !== 'review') return undefined
  return gradedDetails.value.find((d) => d.id === id)
}

async function fetchList(): Promise<void> {
  const res = await getPracticeList('grammar-fill', { source: source.value === 'ai' ? 'real' : source.value })
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
    const [item] = await Promise.all([generatePractice('grammar-fill'), minWait])
    if (aiAborted) return
    startAnswer(item as PracticeItem<'grammar-fill'>)
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
  const detail = (await getPracticeDetail('grammar-fill', id)) as PracticeItem<'grammar-fill'>
  startAnswer(detail)
}

function startAnswer(item: PracticeItem<'grammar-fill'>): void {
  currentItem.value = item
  answers.value = {}
  gradedDetails.value = []
  activeBlankId.value = null
  reset(LIMIT_SECONDS)
  state.value = 'answer'
  start()
}

async function onReview(item: PracticeListItem): Promise<void> {
  const detail = (await getPracticeDetail('grammar-fill', item.id)) as PracticeItem<'grammar-fill'>
  currentItem.value = detail
  if (item.submissionId) {
    await loadSubmissionView(item.submissionId)
  }
}

async function loadSubmissionView(submissionId: string): Promise<void> {
  const sub = await getSubmissionDetail(submissionId)
  if (sub.type !== 'grammar-fill') return
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

/** 文章侧点击空位:桌面滚动定位到对应填空;手机弹底部输入框(10.10) */
function onBlankSelect(id: number): void {
  if (state.value === 'review') return
  activeBlankId.value = id
  if (isMobile.value) {
    draftValue.value = answers.value[id] ?? ''
    inputDrawerShow.value = true
    return
  }
  document.getElementById(`fill-q-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

/** 手机抽屉确认:回填文本 */
function confirmDrawer(): void {
  if (activeBlankId.value !== null) {
    answers.value = { ...answers.value, [activeBlankId.value]: draftValue.value }
  }
  inputDrawerShow.value = false
}

/** 填空列表输入 */
function onInput(id: number, value: string): void {
  answers.value = { ...answers.value, [id]: value }
}

async function onSubmit(): Promise<void> {
  if (!currentItem.value) return
  submitting.value = true
  try {
    const result = await submitPractice('grammar-fill', currentItem.value.id, {
      answers: answers.value,
      duration: elapsed.value,
    })
    if (result.type !== 'grammar-fill') return
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
    if (incoming.type === 'grammar-fill') {
      startAnswer(incoming as PracticeItem<'grammar-fill'>)
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
.grammar-fill-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.grammar-fill-page__main {
  min-width: 0;
}

.grammar-fill-page__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.grammar-fill-page__answer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.answer__passage {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  /* 断词兜底 + 横向兜底,防溢出 */
  overflow-wrap: break-word;
  overflow-x: hidden;
}

/* 桌面:填空列表两列;手机单列 */
.answer__blanks {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 4px 12px;
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

@media (max-width: 767px) {
  .answer__blanks {
    grid-template-columns: minmax(0, 1fr);
  }
}

.answer__result {
  max-width: 400px;
}

.input-drawer {
  padding: 16px;
}

.input-drawer__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 12px;
}

.input-drawer__hint {
  font-size: 13px;
  font-weight: 400;
  color: var(--lc-text-2);
}

.input-drawer__confirm {
  margin-top: 12px;
}
</style>