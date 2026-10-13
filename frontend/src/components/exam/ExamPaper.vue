<!-- 整卷答题(任务书 10.12):顶部 5 个部分跳转,中间答题区,底部答题卡
     答题卡编号规则(任务书 11.7):全局编号 = 前序各部分题量累加 + 本节题序,
     偏移量按 paper.sections 实时推导(不再写死 15/20/35/45),与后端 ExamPaperValidator 同一口径
     作答统一以全局编号存储;各部分复用对应题型的组件渲染 -->
<template>
  <div class="exam-paper">
    <!-- 顶部 5 个部分跳转(显示已答进度) -->
    <div class="exam-paper__parts">
      <n-button
        v-for="section in paper.sections"
        :key="section.part"
        size="small"
        :type="activePart === section.part ? 'primary' : 'default'"
        ghost
        @click="activePart = section.part"
      >
        {{ partName(section.type) }}
        <template v-if="section.type !== 'writing'"> {{ answeredInPart(section.part) }}/{{ countInPart(section.part) }}</template>
      </n-button>
    </div>

    <!-- 答题区:按当前部分渲染 -->
    <div class="exam-paper__body">
      <!-- 阅读部分:3 篇,每篇 5 题(卷面存本节题序,答题卡按前序累加换算) -->
      <template v-if="readingPapers">
        <div v-for="paperItem in readingPapers" :key="paperItem.id" class="exam-paper__reading-paper">
          <div class="exam-paper__passage">
            <PassageRenderer
              :passage="paperItem.passage"
              :font-size="16"
              :eye-protect="false"
              @favorite-sentence="onFavoriteSentence"
            />
          </div>
          <div class="exam-paper__questions">
            <QuestionItem
              v-for="q in paperItem.questions"
              :key="q.id"
              :question="q"
              :model-value="objective[readingOffset + q.id] ?? ''"
              :graded="false"
              @update:model-value="onPick(readingOffset + q.id, $event)"
            />
          </div>
        </div>
      </template>

      <!-- 五选五部分:选项池 + 文章槽位(答题卡编号 = matchingOffset + 局部题号,文章占位保持局部编号展示) -->
      <template v-else-if="activeSection?.type === 'matching' && matchingContent">
        <OptionPool
          :options="matchingContent.options"
          :used-keys="usedMatchingKeys"
          :active-key="activeOptionKey"
          @pick="onOptionClick"
        />
        <div class="exam-paper__passage">
          <template v-for="(seg, index) in matchingSegments" :key="index">
            <template v-if="seg.type === 'text'">{{ seg.content }}</template>
            <BlankSlot
              v-else
              :id="seg.id ?? 0"
              :answer-key="objective[matchingOffset + (seg.id ?? 0)]"
              :active="activeBlankId === seg.id"
              :graded="false"
              @click="onBlankClick(seg.id ?? 0)"
              @remove="onBlankRemove(seg.id ?? 0)"
            />
          </template>
        </div>
      </template>

      <!-- 完形填空部分:文章 + 单题(答题卡编号 = clozeOffset + 局部题号) -->
      <template v-else-if="activeSection?.type === 'cloze' && clozeContent">
        <div class="exam-paper__passage">
          <ClozePassage
            :passage="clozeContent.passage"
            :answers="clozeAnswersLocal"
            :active-id="activeLocalBlankId"
            :graded="false"
            :details="[]"
            @select="activeLocalBlankId = $event"
          />
        </div>
        <div class="exam-paper__questions is-grid">
          <ClozeQuestion
            v-for="blank in clozeContent.blanks"
            :id="blank.id"
            :key="blank.id"
            :options="blank.options"
            :model-value="objective[clozeOffset + blank.id] ?? ''"
            :graded="false"
            :active="activeLocalBlankId === blank.id"
            @update:model-value="onPick(clozeOffset + blank.id, $event)"
          />
        </div>
      </template>

      <!-- 语法填空部分:文章 + 填空(答题卡编号 = grammarOffset + 局部题号) -->
      <template v-else-if="activeSection?.type === 'grammar-fill' && grammarFillContent">
        <div class="exam-paper__passage">
          <FillPassage
            :passage="grammarFillContent.passage"
            :answers="grammarFillAnswersLocal"
            :active-id="activeLocalBlankId"
            :graded="false"
            :details="[]"
            @select="activeLocalBlankId = $event"
          />
        </div>
        <div class="exam-paper__questions is-grid">
          <FillBlank
            v-for="blank in grammarFillContent.blanks"
            :id="blank.id"
            :key="blank.id"
            :hint="blank.hint"
            :model-value="objective[grammarOffset + blank.id] ?? ''"
            :graded="false"
            :active="activeLocalBlankId === blank.id"
            @update:model-value="onPick(grammarOffset + blank.id, $event)"
          />
        </div>
      </template>

      <!-- 作文部分:题目 + 编辑区(单独提交,不计入答题卡) -->
      <template v-else-if="activeSection?.type === 'writing' && writingContent">
        <div class="exam-paper__writing">
          <WritingPrompt
            :prompt="writingContent.prompt"
            :requirements="writingContent.requirements"
            :word-limit="writingContent.wordLimit"
            :scoring-dimensions="writingContent.scoringDimensions"
          />
          <WritingEditor v-model="writing" :word-limit="writingContent.wordLimit" />
        </div>
      </template>
    </div>

    <!-- 底部答题卡:1~客观题总数 全局编号,点击跳到对应部分(10.12) -->
    <div class="exam-paper__sheet">
      <AnswerSheet :count="totalObjectiveCount" :answers="objective" :details="[]" @select="onSheetSelect" />
      <n-button size="small" :type="activePart === 5 ? 'primary' : 'default'" ghost @click="activePart = 5">
        作文(单独)
      </n-button>
    </div>

    <!-- 手机:五选五点击空位弹出的选项面板(复用题型组件) -->
    <OptionPickerDrawer
      v-model:show="pickerShow"
      :options="matchingContent?.options ?? []"
      :used-keys="usedMatchingKeys"
      @pick="onPickerPick"
    />
  </div>
</template>

<script setup lang="ts">
// 作答存储:objective(答题卡全局编号 → 选项 key/填空文本)+ writing(作文正文)
// 对外通过 getPayload() 暴露交卷数据(父页面在交卷时调用)
import { computed, ref, watch } from 'vue'
import { NButton, useMessage } from 'naive-ui'
import PassageRenderer from '@/components/practice/reading/PassageRenderer.vue'
import QuestionItem from '@/components/practice/reading/QuestionItem.vue'
import OptionPool from '@/components/practice/matching/OptionPool.vue'
import BlankSlot from '@/components/practice/matching/BlankSlot.vue'
import OptionPickerDrawer from '@/components/practice/matching/OptionPickerDrawer.vue'
import ClozePassage from '@/components/practice/cloze/ClozePassage.vue'
import ClozeQuestion from '@/components/practice/cloze/ClozeQuestion.vue'
import FillPassage from '@/components/practice/grammar-fill/FillPassage.vue'
import FillBlank from '@/components/practice/grammar-fill/FillBlank.vue'
import WritingPrompt from '@/components/practice/writing/WritingPrompt.vue'
import WritingEditor from '@/components/practice/writing/WritingEditor.vue'
import AnswerSheet from '@/components/practice/reading/AnswerSheet.vue'
import { addFavorite } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { usePlaceholder } from '@/composables/usePlaceholder'
import type {
  ClozeQuestionForAnswer,
  GrammarFillQuestionForAnswer,
  MatchingQuestionForAnswer,
  PracticeType,
  ReadingQuestionForAnswer,
  WritingQuestionForAnswer,
} from '@/types/practice'
import type { ExamPaper, ExamSection } from '@/types/exam'

const props = defineProps<{
  /** 整卷(下发版,不含答案) */
  paper: ExamPaper
}>()

const message = useMessage()
const { isMobile } = useDevice()
const { parsePassage } = usePlaceholder()

/** 当前显示的部分(part 1-5) */
const activePart = ref(1)
/** 作答:全局编号 → 选项 key(匹配/完形)或填空文本(语法填空) */
const objective = ref<Record<number, string>>({})
/** 作文正文 */
const writing = ref('')

/** 断点续答(扩展契约 E2):作答变更通知父级(父级防抖保存进度) */
const emit = defineEmits<{ progressChange: [] }>()
watch([objective, writing], () => emit('progressChange'), { deep: true })
/** 五选五交互态(复用 10.8 逻辑) */
const activeBlankId = ref<number | null>(null)
const activeOptionKey = ref<string | null>(null)
const pickerShow = ref(false)
/** 完形/语法填空文章的当前选中空位(局部编号) */
const activeLocalBlankId = ref<number | null>(null)

/** 当前部分 */
const activeSection = computed(() => props.paper.sections.find((s) => s.part === activePart.value))

/** 各部分内容(按 part 定位;类型断言基于数据契约约定) */
const sectionContent = computed(() => activeSection.value?.content)
const readingPapers = computed(() => {
  const section = activeSection.value
  if (!section || section.type !== 'reading' || !Array.isArray(section.content)) return null
  return section.content as ReadingQuestionForAnswer[]
})
const matchingContent = computed(() =>
  activeSection.value?.type === 'matching' ? (sectionContent.value as MatchingQuestionForAnswer) : null
)
const clozeContent = computed(() =>
  activeSection.value?.type === 'cloze' ? (sectionContent.value as ClozeQuestionForAnswer) : null
)
const grammarFillContent = computed(() =>
  activeSection.value?.type === 'grammar-fill'
    ? (sectionContent.value as GrammarFillQuestionForAnswer)
    : null
)
const writingContent = computed(() =>
  activeSection.value?.type === 'writing' ? (sectionContent.value as WritingQuestionForAnswer) : null
)

/** 一个部分在答题卡上的编号区间 */
type PartNumbering = { offset: number; count: number; from: number; to: number }

/** 某部分客观题题量(阅读为各篇 questions 合计;写作不占答题卡) */
function countItems(section: ExamSection): number {
  if (section.type === 'writing') {
    return 0
  }
  const content = section.content
  if (section.type === 'reading' && Array.isArray(content)) {
    return content.reduce((sum, p) => sum + p.questions.length, 0)
  }
  if (Array.isArray(content)) {
    return 0
  }
  if (content.type === 'matching') return content.blanks.length
  if (content.type === 'cloze') return content.blanks.length
  if (content.type === 'grammar-fill') return content.blanks.length
  return 0
}

/**
 * 答题卡编号映射(任务书 11.7):按 part 升序累加前序题量。
 *
 * <p>卷面存的是本节内题序(1~n),答题卡与交卷 objective 的 key 都是全局编号;
 * 偏移量必须由数据推导,否则后台一旦增删题目,学生端就会静默串题。</p>
 */
const numberings = computed<Record<number, PartNumbering>>(() => {
  const map: Record<number, PartNumbering> = {}
  let offset = 0
  for (const section of [...props.paper.sections].sort((a, b) => a.part - b.part)) {
    const count = countItems(section)
    map[section.part] = { offset, count, from: offset + 1, to: offset + count }
    offset += count
  }
  return map
})

/** 某题型部分的编号区间(卷内该题型至多一个部分,缺失即空区间) */
function numberingOf(type: PracticeType): PartNumbering {
  const section = props.paper.sections.find((s) => s.type === type)
  return (section && numberings.value[section.part]) || { offset: 0, count: 0, from: 1, to: 0 }
}

/** 各部分的答题卡起始偏移 */
const readingOffset = computed(() => numberingOf('reading').offset)
const matchingOffset = computed(() => numberingOf('matching').offset)
const clozeOffset = computed(() => numberingOf('cloze').offset)
const grammarOffset = computed(() => numberingOf('grammar-fill').offset)

/** 客观题总数(答题卡格数) */
const totalObjectiveCount = computed(() =>
  Object.values(numberings.value).reduce((max, n) => Math.max(max, n.to), 0)
)

/** 五选五文章片段与已用选项 */
const matchingSegments = computed(() => (matchingContent.value ? parsePassage(matchingContent.value.passage) : []))
const usedMatchingKeys = computed(() => {
  const range = numberingOf('matching')
  return Object.entries(objective.value)
    .filter(([k]) => Number(k) >= range.from && Number(k) <= range.to)
    .map(([, v]) => v)
})

/** 完形/语法填空:把全局作答映射为局部编号(文章组件按局部 [[n]] 查询) */
const clozeAnswersLocal = computed<Record<number, string>>(() =>
  Object.fromEntries(Object.entries(objective.value).map(([k, v]) => [Number(k) - clozeOffset.value, v]))
)
const grammarFillAnswersLocal = computed<Record<number, string>>(() =>
  Object.fromEntries(Object.entries(objective.value).map(([k, v]) => [Number(k) - grammarOffset.value, v]))
)

/** 部分名称 */
function partName(type: PracticeType): string {
  const map: Record<PracticeType, string> = {
    reading: '阅读理解',
    matching: '五选五',
    cloze: '完形填空',
    'grammar-fill': '语法填空',
    writing: '作文',
  }
  return map[type]
}

/** 某部分题量(答题卡口径,作文为 0) */
function countInPart(part: number): number {
  return numberings.value[part]?.count ?? 0
}

/** 某部分已答数 */
function answeredInPart(part: number): number {
  const range = numberings.value[part]
  if (!range) {
    return 0
  }
  return Object.entries(objective.value).filter(([k, v]) => {
    const n = Number(k)
    return n >= range.from && n <= range.to && v !== ''
  }).length
}

/** 单选/填空作答 */
function onPick(id: number, value: string): void {
  objective.value = { ...objective.value, [id]: value }
}

/** 五选五:点空位(10.8:已填移除;有选中选项填入;手机弹面板) */
function onBlankClick(localId: number): void {
  const globalId = matchingOffset.value + localId
  if (objective.value[globalId]) {
    onBlankRemove(localId)
    return
  }
  if (activeOptionKey.value) {
    onPick(globalId, activeOptionKey.value)
    activeOptionKey.value = null
    return
  }
  activeBlankId.value = localId
  if (isMobile.value) pickerShow.value = true
}

/** 五选五:点选项 */
function onOptionClick(key: string): void {
  if (usedMatchingKeys.value.includes(key)) return
  if (activeBlankId.value !== null) {
    onPick(matchingOffset.value + activeBlankId.value, key)
    activeBlankId.value = null
    return
  }
  activeOptionKey.value = key
}

/** 五选五:手机面板选择 */
function onPickerPick(key: string): void {
  if (activeBlankId.value !== null) {
    onPick(matchingOffset.value + activeBlankId.value, key)
  }
  activeBlankId.value = null
  activeOptionKey.value = null
}

/** 五选五:移除已填 */
function onBlankRemove(localId: number): void {
  const next = { ...objective.value }
  delete next[matchingOffset.value + localId]
  objective.value = next
}

/** 答题卡点击:跳到对应部分(10.12;区间由数据推导) */
function onSheetSelect(globalId: number): void {
  const hit = [...props.paper.sections]
    .sort((a, b) => a.part - b.part)
    .find((section) => {
      const range = numberings.value[section.part]
      return !!range && range.count > 0 && globalId >= range.from && globalId <= range.to
    })
  if (hit) {
    activePart.value = hit.part
  }
}

/** 模考阅读中收藏选中句子(与其他题型一致) */
async function onFavoriteSentence(text: string): Promise<void> {
  try {
    await addFavorite({ type: 'sentence', content: text, refId: props.paper.id })
    message.success('已收藏该句')
  } catch {
    // 错误提示由 request 层统一弹出
  }
}

/** 交卷数据(payload) */
function getPayload(): { objective: Record<number, string>; writing?: { content: string } } {
  return {
    objective: objective.value,
    writing: writing.value.trim() ? { content: writing.value } : undefined,
  }
}

/** 未作答客观题数量(交卷确认用) */
function unansweredCount(): number {
  let count = 0
  for (let id = 1; id <= totalObjectiveCount.value; id++) {
    if (!objective.value[id]) count++
  }
  return count
}

/** 断点续答(扩展契约 E2):恢复进度到本地作答 */
function restoreFromProgress(data: { objective?: Record<number, string>; writing?: { content: string } }): void {
  if (data.objective && typeof data.objective === 'object') {
    objective.value = { ...data.objective }
  }
  if (data.writing?.content) {
    writing.value = data.writing.content
  }
}

/** 断点续答(扩展契约 E2):导出当前作答快照 */
function snapshot(): { objective: Record<number, string>; writing?: { content: string } } {
  return {
    objective: { ...objective.value },
    writing: writing.value ? { content: writing.value } : undefined,
  }
}

defineExpose({ getPayload, unansweredCount, restoreFromProgress, snapshot })
</script>

<style scoped>
.exam-paper {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.exam-paper__parts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.exam-paper__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

/* 阅读部分:与 practice/reading 布局一致——桌面左文章右题目;≤1023px 上下单列 */
.exam-paper__reading-paper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  min-width: 0;
}

@media (max-width: 1023px) {
  .exam-paper__reading-paper {
    grid-template-columns: minmax(0, 1fr);
  }
}

.exam-paper__passage {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  line-height: 2.2;
  color: var(--lc-text-1);
  /* 断词兜底 + 横向兜底(clip 不创建滚动容器) */
  overflow-wrap: break-word;
  overflow-x: clip;
}

.exam-paper__questions {
  padding: 4px 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

/* 完形/语法填空:桌面双列 */
.exam-paper__questions.is-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 4px 12px;
}

@media (max-width: 767px) {
  .exam-paper__questions.is-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

.exam-paper__writing {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: 16px;
  min-width: 0;
}

@media (max-width: 1023px) {
  .exam-paper__writing {
    grid-template-columns: minmax(0, 1fr);
  }
}

.exam-paper__sheet {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  flex-wrap: wrap;
}
</style>