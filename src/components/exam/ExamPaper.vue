<!-- 整卷答题(任务书 10.12):顶部 5 个部分跳转,中间答题区,底部答题卡
     答题卡全局编号规则(11.7):阅读 1-15、匹配 16-20、完形 21-35、语法 36-45、作文单独
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
      <!-- 阅读部分:3 篇,每篇 5 题(题号全局连续 1-15),按篇渲染文章与题目 -->
      <template v-if="readingPapers">
        <template v-for="paperItem in readingPapers" :key="paperItem.id">
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
              :model-value="objective[q.id] ?? ''"
              :graded="false"
              @update:model-value="onPick(q.id, $event)"
            />
          </div>
        </template>
      </template>

      <!-- 五选五部分:选项池 + 文章槽位(全局编号 16-20,文章占位保持局部编号展示) -->
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
              :answer-key="objective[15 + (seg.id ?? 0)]"
              :active="activeBlankId === seg.id"
              :graded="false"
              @click="onBlankClick(seg.id ?? 0)"
              @remove="onBlankRemove(seg.id ?? 0)"
            />
          </template>
        </div>
      </template>

      <!-- 完形填空部分:文章 + 单题(全局编号 21-35) -->
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
            :model-value="objective[20 + blank.id] ?? ''"
            :graded="false"
            :active="activeLocalBlankId === blank.id"
            @update:model-value="onPick(20 + blank.id, $event)"
          />
        </div>
      </template>

      <!-- 语法填空部分:文章 + 填空(全局编号 36-45) -->
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
            :model-value="objective[35 + blank.id] ?? ''"
            :graded="false"
            :active="activeLocalBlankId === blank.id"
            @update:model-value="onPick(35 + blank.id, $event)"
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

    <!-- 底部答题卡:1-45 全局编号,点击跳到对应部分(10.12) -->
    <div class="exam-paper__sheet">
      <AnswerSheet :count="45" :answers="objective" :details="[]" @select="onSheetSelect" />
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
// 作答存储:objective(全局编号 1-45 → 选项 key/填空文本)+ writing(作文正文)
// 对外通过 getPayload() 暴露交卷数据(父页面在交卷时调用)
import { computed, ref } from 'vue'
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
import type { ExamPaper } from '@/types/exam'

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

/** 五选五文章片段与已用选项 */
const matchingSegments = computed(() => (matchingContent.value ? parsePassage(matchingContent.value.passage) : []))
const usedMatchingKeys = computed(() =>
  Object.entries(objective.value)
    .filter(([k]) => Number(k) >= 16 && Number(k) <= 20)
    .map(([, v]) => v)
)

/** 完形/语法填空:把全局作答映射为局部编号(文章组件按局部 [[n]] 查询) */
const clozeAnswersLocal = computed(() =>
  Object.fromEntries(Object.entries(objective.value).map(([k, v]) => [Number(k) - 20, v])) as Record<number, string>
)
const grammarFillAnswersLocal = computed(() =>
  Object.fromEntries(Object.entries(objective.value).map(([k, v]) => [Number(k) - 35, v])) as Record<number, string>
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

/** 某部分题量(作文不计数;阅读为 3 篇题数合计) */
function countInPart(part: number): number {
  const section = props.paper.sections.find((s) => s.part === part)
  if (!section) return 0
  const content = section.content
  if (section.type === 'reading' && Array.isArray(content)) {
    return content.reduce((sum, p) => sum + p.questions.length, 0)
  }
  if (Array.isArray(content)) return 0
  if (content.type === 'matching') return content.blanks.length
  if (content.type === 'cloze') return content.blanks.length
  if (content.type === 'grammar-fill') return content.blanks.length
  return 0
}

/** 某部分已答数 */
function answeredInPart(part: number): number {
  const base: Record<number, [number, number]> = {
    1: [1, 15],
    2: [16, 20],
    3: [21, 35],
    4: [36, 45],
  }
  const range = base[part]
  if (!range) return 0
  return Object.entries(objective.value).filter(([k, v]) => {
    const n = Number(k)
    return n >= range[0] && n <= range[1] && v !== ''
  }).length
}

/** 单选/填空作答 */
function onPick(id: number, value: string): void {
  objective.value = { ...objective.value, [id]: value }
}

/** 五选五:点空位(10.8:已填移除;有选中选项填入;手机弹面板) */
function onBlankClick(localId: number): void {
  const globalId = 15 + localId
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
    onPick(15 + activeBlankId.value, key)
    activeBlankId.value = null
    return
  }
  activeOptionKey.value = key
}

/** 五选五:手机面板选择 */
function onPickerPick(key: string): void {
  if (activeBlankId.value !== null) {
    onPick(15 + activeBlankId.value, key)
  }
  activeBlankId.value = null
  activeOptionKey.value = null
}

/** 五选五:移除已填 */
function onBlankRemove(localId: number): void {
  const next = { ...objective.value }
  delete next[15 + localId]
  objective.value = next
}

/** 答题卡点击:跳到对应部分(10.12) */
function onSheetSelect(globalId: number): void {
  if (globalId <= 15) activePart.value = 1
  else if (globalId <= 20) activePart.value = 2
  else if (globalId <= 35) activePart.value = 3
  else activePart.value = 4
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
  for (let id = 1; id <= 45; id++) {
    if (!objective.value[id]) count++
  }
  return count
}

defineExpose({ getPayload, unansweredCount })
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

.exam-paper__passage {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  line-height: 2.2;
  color: var(--lc-text-1);
  overflow-wrap: break-word;
  overflow-x: hidden;
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