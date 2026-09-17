<!-- 阅读理解页(任务书 10.7):试题页样板,状态机覆盖 来源选择/列表/AI生成/答题/批改/pending/回看
     桌面左文右题,平板/手机上下 + Tab「文章/题目」;点词查词、选中句子收藏、字号/护眼、答题卡定位、超时自动提交 -->
<template>
  <div class="reading-page">
    <!-- 沉浸头部(任务书 10.6 通用结构):退出 | 题型名 | 来源 | 计时 | 提交 -->
    <PracticeHeader
      title="阅读理解"
      :source="source"
      :answering="state === 'answer'"
      :elapsed="elapsed"
      :submitting="submitting"
      @back="onBack"
      @update:source="onSourceChange"
      @submit="onSubmit"
    />

    <main class="reading-page__main">
      <!-- 列表态:来源筛选栏 + 真题/老师共用列表 -->
      <div v-if="state === 'source' || state === 'list'" class="reading-page__list">
        <PracticeSourceBar
          :filter-chips="filterChips"
          :selected="filterSelected"
          @update:selected="filterSelected = $event"
        />
        <PracticeListPage :list="displayList" @start="onStart" @review="onReview" />
      </div>

      <!-- AI 生成态(10.6):点即生成,loading 后直接答题,失败重试 -->
      <AiGeneratingView
        v-else-if="state === 'ai'"
        :failed="aiFailed"
        @cancel="onAiCancel"
        @retry="generateAi"
      />

      <!-- 待批改态(10.6 PendingView) -->
      <PendingView
        v-else-if="state === 'pending'"
        :estimated-time="estimatedTime"
        :refreshing="pendingRefreshing"
        @back="state = 'list'"
        @refresh="refreshPending"
      />

      <!-- 答题 / 批改态 -->
      <div v-else-if="currentItem" class="reading-page__answer">
        <!-- 桌面:左文章右题目;平板/手机:上下 + Tab -->
        <div class="answer">
          <aside class="answer__passage">
            <div class="answer__toolbar">
              <n-button size="tiny" quaternary @click="fontSize = nextFontSize(fontSize)">Aa 字号</n-button>
              <n-button size="tiny" quaternary :type="eyeProtect ? 'primary' : 'default'" @click="eyeProtect = !eyeProtect">
                🌙 护眼
              </n-button>
            </div>
            <div class="answer__passage-scroll">
              <PassageRenderer
                :passage="currentItem.content.passage"
                :font-size="fontSize"
                :eye-protect="eyeProtect"
                @favorite-sentence="onFavoriteSentence"
              />
            </div>
          </aside>

          <section class="answer__questions">
            <!-- 平板/手机:文章 / 题目 Tab 切换 -->
            <n-tabs v-if="!isDesktop" v-model:value="mobileTab" type="line">
              <n-tab-pane name="passage" tab="文章">
                <div class="answer__mobile-passage">
                  <PassageRenderer
                    :passage="currentItem.content.passage"
                    :font-size="fontSize"
                    :eye-protect="eyeProtect"
                    @favorite-sentence="onFavoriteSentence"
                  />
                </div>
              </n-tab-pane>
              <n-tab-pane name="questions" tab="题目">
                <QuestionList
                  ref="questionListRef"
                  :questions="currentItem.content.questions"
                  :answers="answers"
                  :graded="state === 'review'"
                  :details="gradedDetails"
                  @update:answer="onPick"
                />
              </n-tab-pane>
            </n-tabs>

            <template v-else>
              <QuestionList
                ref="questionListRef"
                :questions="currentItem.content.questions"
                :answers="answers"
                :graded="state === 'review'"
                :details="gradedDetails"
                @update:answer="onPick"
              />
            </template>

            <!-- 答题卡:答题定位 + 批改对错着色(10.7) -->
            <div class="answer__sheet">
              <AnswerSheet
                :count="currentItem.content.questions.length"
                :answers="answers"
                :details="gradedDetails"
                @select="onSheetSelect"
              />
            </div>
          </section>
        </div>

        <!-- 批改态结果横幅 -->
        <n-alert v-if="state === 'review'" type="success" class="answer__result">
          已批改:得分 {{ resultScore }}/{{ resultTotalScore }}
        </n-alert>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
// 状态机:source/list(真题或老师列表)→ answer(答题)→ review(批改)/pending(待批改)
// 进入链:错题重做(route.query.id)→ 智能练习(store incomingItem)→ 常规来源列表
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAlert, NButton, NTabPane, NTabs, useMessage } from 'naive-ui'
import PracticeHeader from '@/components/practice/shared/PracticeHeader.vue'
import PracticeSourceBar from '@/components/practice/shared/PracticeSourceBar.vue'
import PracticeListPage from '@/components/practice/shared/PracticeListPage.vue'
import AiGeneratingView from '@/components/common/AiGeneratingView.vue'
import PendingView from '@/components/common/PendingView.vue'
import PassageRenderer from '@/components/practice/reading/PassageRenderer.vue'
import QuestionList from '@/components/practice/reading/QuestionList.vue'
import AnswerSheet from '@/components/practice/reading/AnswerSheet.vue'
import { generatePractice, getPracticeDetail, getPracticeList, submitPractice } from '@/api/practice'
import { getSubmissionDetail } from '@/api/user'
import { addFavorite } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useCountdown } from '@/composables/useCountdown'
import { usePracticeStore } from '@/stores/practice'
import type { PracticeItem, PracticeListItem, SourceType, SubmitDetail } from '@/types/practice'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { isMobile, isDesktop } = useDevice()
const practiceStore = usePracticeStore()

/** 练习时限:任务书未规定单篇时长,暂定 15 分钟并在超时自动提交(10.7);后端契约可覆盖 */
const LIMIT_SECONDS = 15 * 60
const { elapsed, remaining, start, reset } = useCountdown(LIMIT_SECONDS)

const state = ref<'source' | 'list' | 'ai' | 'answer' | 'pending' | 'review'>('source')
const source = ref<SourceType>('real')
/** 列表筛选:真题按年份,老师出题按老师名 */
const filterSelected = ref('全部')
const list = ref<PracticeListItem[]>([])
/** 当前题目(阅读题) */
const currentItem = ref<PracticeItem<'reading'> | null>(null)
/** 作答:题号 → 选项 key */
const answers = ref<Record<number, string>>({})
/** 批改明细(批改/回看态) */
const gradedDetails = ref<SubmitDetail[]>([])
const submitting = ref(false)
const pendingRefreshing = ref(false)
const estimatedTime = ref('')
const pendingSubmissionId = ref('')
/** AI 展开状态 */
const aiFailed = ref(false)
let aiAborted = false
/** 文章显示偏好:字号三档 + 护眼 */
const fontSize = ref(16)
const eyeProtect = ref(false)
/** 平板/手机 Tab */
const mobileTab = ref('passage')
const questionListRef = ref<InstanceType<typeof QuestionList> | null>(null)

/** 批改结果分数(头部横幅) */
const resultScore = computed(() => {
  const total = currentItem.value?.content.questions.length ?? 0
  const correct = gradedDetails.value.filter((d) => d.isCorrect).length
  return total > 0 ? `${correct}` : '0'
})
const resultTotalScore = computed(() => `${currentItem.value?.content.questions.length ?? 0}`)

/** 筛选 chips:真题=年份集合,老师=老师名集合 */
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

/** 字号三档循环 */
function nextFontSize(current: number): number {
  const sizes = [14, 16, 18]
  return sizes[(sizes.indexOf(current) + 1) % sizes.length]
}

/** 拉取列表(真题/老师) */
async function fetchList(): Promise<void> {
  const res = await getPracticeList('reading', { source: source.value === 'ai' ? 'real' : source.value })
  list.value = res.list
}

/** 切换来源:AI 直接进生成;真题/老师进列表(10.6 切换器行为) */
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

/** AI 出题:点即生成,保证至少展示 10s 生成动画,可取消,失败可重试 */
async function generateAi(): Promise<void> {
  aiFailed.value = false
  const minWait = new Promise((resolve) => setTimeout(resolve, 10_000))
  try {
    const [item] = await Promise.all([generatePractice('reading'), minWait])
    if (aiAborted) return
    startAnswer(item as PracticeItem<'reading'>)
  } catch {
    aiFailed.value = true
  }
}

/** 取消 AI 生成:回到来源选择 */
function onAiCancel(): void {
  aiAborted = true
  state.value = 'source'
}

/** 开始作答某题 */
async function onStart(item: PracticeListItem): Promise<void> {
  loadAndAnswer(item.id)
}

/** 加载题目并进入答题态 */
async function loadAndAnswer(id: string): Promise<void> {
  const detail = (await getPracticeDetail('reading', id)) as PracticeItem<'reading'>
  startAnswer(detail)
}

/** 进入答题态并启动计时 */
function startAnswer(item: PracticeItem<'reading'>): void {
  currentItem.value = item
  answers.value = {}
  gradedDetails.value = []
  reset(LIMIT_SECONDS)
  state.value = 'answer'
  start()
}

/** 查看结果(列表回看,10.6):按最新状态分支 */
async function onReview(item: PracticeListItem): Promise<void> {
  const detail = (await getPracticeDetail('reading', item.id)) as PracticeItem<'reading'>
  currentItem.value = detail
  if (item.submissionId) {
    await loadSubmissionView(item.submissionId)
  }
}

/** 按提交 id 查询最新状态并在题目上重建作答:graded → 批改态,pending → pending 视图 */
async function loadSubmissionView(submissionId: string): Promise<void> {
  const sub = await getSubmissionDetail(submissionId)
  if (sub.type !== 'reading') return
  if (sub.status === 'pending') {
    pendingSubmissionId.value = submissionId
    estimatedTime.value = sub.estimatedTime ?? '预计 24 小时内出结果'
    state.value = 'pending'
    return
  }
  const details = sub.details ?? []
  gradedDetails.value = details
  answers.value = Object.fromEntries(details.map((d) => [d.id, d.userAnswer]))
  state.value = 'review'
}

/** 刷新 pending 状态(10.6 [刷新状态]) */
async function refreshPending(): Promise<void> {
  if (!pendingSubmissionId.value || !currentItem.value) return
  pendingRefreshing.value = true
  try {
    await loadSubmissionView(pendingSubmissionId.value)
    if (state.value !== 'pending') message.success('已出结果')
    else message.info('仍在批改中,请稍后再查')
  } finally {
    pendingRefreshing.value = false
  }
}

/** 作答选择 */
function onPick(id: number, key: string): void {
  answers.value = { ...answers.value, [id]: key }
}

/** 答题卡定位 */
function onSheetSelect(id: number): void {
  if (isMobile.value) mobileTab.value = 'questions'
  questionListRef.value?.scrollTo(id)
}

/** 提交:AI/人工偏好由服务端读用户设置;pending → PendingView,graded → 批改态 */
async function onSubmit(): Promise<void> {
  if (!currentItem.value) return
  submitting.value = true
  try {
    const result = await submitPractice('reading', currentItem.value.id, {
      answers: answers.value,
      duration: elapsed.value,
    })
    if (result.type !== 'reading') return
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

/** 收藏选中句子(10.7 交互) */
async function onFavoriteSentence(text: string): Promise<void> {
  try {
    await addFavorite({ type: 'sentence', content: text, refId: currentItem.value?.id })
    message.success('已收藏该句')
  } catch {
    // 错误提示由 request 层统一弹出
  }
}

/** 退出:有来源列表则回列表,否则返回上一页 */
function onBack(): void {
  if (state.value !== 'answer' && state.value !== 'pending' && state.value !== 'review') {
    router.back()
    return
  }
  state.value = 'list'
  fetchList()
}

/** 计时超时自动提交(10.7) */
watch(remaining, (val) => {
  if (val <= 0 && state.value === 'answer') {
    message.warning('本题时限已到,已为你自动提交')
    onSubmit()
  }
})

onMounted(async () => {
  // 错题重做链:?id=xxx&from=mistakes 直接进入该题答题(任务书第 4 章)
  const queryId = String(route.query.id ?? '')
  if (queryId) {
    await loadAndAnswer(queryId)
    return
  }
  // 智能练习:store 传递题目,直接进入答题
  const incoming = practiceStore.takeIncomingItem()
  if (incoming) {
    if (incoming.type === 'reading') {
      startAnswer(incoming as PracticeItem<'reading'>)
      return
    }
    router.replace(`/practice/${incoming.type}`)
    return
  }
  // 常规入口:真题列表
  await fetchList()
  state.value = 'list'
})
</script>

<style scoped>
/* 页面自然流式布局:不占用 100vh、不做内部滚动,由布局层内容区统一滚动
   (修复:非沉浸时与顶栏/侧栏同处布局壳内,避免高度溢出与双重滚动条) */
.reading-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reading-page__main {
  min-width: 0;
}

.reading-page__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.reading-page__answer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.answer {
  display: grid;
  /* minmax(0, 1fr):1fr 等价 minmax(auto, 1fr),列宽会被长内容撑爆导致横向溢出,需显式允许收缩 */
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  min-height: 0;
}

@media (max-width: 1023px) {
  .answer {
    grid-template-columns: 1fr;
  }
}

.answer__passage {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 320px;
}

.answer__toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.answer__passage-scroll {
  flex: 1;
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  /* 防横向溢出且不创建滚动容器(clip 不强制另一轴为 auto);纵向滚动交给布局内容区 */
  overflow-x: clip;
}

.answer__questions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.answer__sheet {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.answer__result {
  max-width: 400px;
}

.answer__mobile-passage {
  padding: 8px 0;
}
</style>