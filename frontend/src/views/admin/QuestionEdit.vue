<!-- 后台题目录入/编辑(A2,A8 起改为表单权威)
     提交以表单为准:保存时把表单值覆盖到"库里原始 content/answer_key"的副本上,
     表单没建模的键(含 content 的 id/type/source 与将来的校核标记)原样带回,不会被静默洗掉。
     下方的「高级」面板默认只是提交内容的预览;管理员在里面动过手,才改以面板为准。 -->
<template>
  <div class="qedit">
    <div class="qedit__head">
      <h2 class="qedit__title">{{ isEdit ? `编辑题目 · ${form.id}` : '新建题目' }}</h2>
      <n-space size="small">
        <n-button size="small" @click="router.push('/admin/questions')">返回列表</n-button>
        <n-tooltip v-if="!isEdit" trigger="hover">
          <template #trigger>
            <n-button size="small" type="info" ghost :loading="generating" :disabled="generating" @click="onGenerateDraft">
              AI 生成一版
            </n-button>
          </template>
          让 AI 出一道新题的草稿填进下面的表单。草稿<b>不入库</b>：改完点「创建题目」才落库、才对学生可见。
        </n-tooltip>
        <n-button size="small" :loading="reviewing" @click="onReview">
          AI 校核本题
        </n-button>
        <n-button size="small" type="primary" :loading="saving" @click="onSave">
          {{ isEdit ? '保存修改' : '创建题目' }}
        </n-button>
      </n-space>
    </div>

    <n-alert v-if="reviewing" type="info" :show-icon="false" class="qedit__notice">
      AI 正在分批校核本题（每批最多 5 个编号，一批约 15~40 秒；10 空的语法填空约需 1 分钟）。
      这一步只出建议，不会改动任何数据。
    </n-alert>

    <n-alert v-else-if="generating" type="info" :show-icon="false" class="qedit__notice">
      AI 正在出题，通常 10~40 秒，最长约 2 分钟（超时或结构不合格会自动重试一次）。这期间可以继续，但请先别点保存。
    </n-alert>

    <n-alert v-else-if="draftInfo" type="success" :show-icon="false" class="qedit__notice">
      <div class="qedit__draftbar">
        <span>
          已填入 AI 草稿（模型 <code>{{ draftInfo.model }}</code>{{ formatDraftTime(draftInfo.generatedAt) ? ' · ' + formatDraftTime(draftInfo.generatedAt) : '' }}），
          <b>尚未保存</b>。来源已记为 AI；改成真题/模拟题也可以，点「创建题目」才入库上架。
        </span>
        <n-button size="tiny" quaternary type="error" @click="onDiscardDraft">丢弃草稿</n-button>
      </div>
    </n-alert>

    <n-alert v-if="manualJson" type="warning" :show-icon="false" class="qedit__notice">
      你已在「高级」面板里手工编辑 JSON —— 本次提交以该面板为准，上方表单的改动不会再自动同步进去。
      想回到"以表单为准"，点面板里的「从表单重新生成」。
    </n-alert>

    <n-alert v-if="saveError" type="error" closable class="qedit__notice" @close="saveError = null">
      <div class="qedit__error">
        <strong>服务端拒绝了这次保存：</strong>{{ saveError }}
        <span v-if="errorRowIndex !== null">（已定位到下方第 {{ errorRowIndex + 1 }} 行）</span>
      </div>
    </n-alert>

    <p v-if="!manualJson && passthroughKeys.length" class="qedit__kept">
      本题面还带着表单没有建模的字段（<code>{{ passthroughKeys.join('、') }}</code>），保存时按原样保留。
    </p>

    <!-- 通用字段 -->
    <n-card size="small" title="基本信息" class="qedit__card">
      <div class="qedit__grid">
        <div class="qedit__field">
          <label>题型</label>
          <n-select
            v-model:value="form.type"
            size="small"
            :options="typeOptions"
            :disabled="isEdit"
            @update:value="onTypeChange"
          />
        </div>
        <div class="qedit__field">
          <label>来源</label>
          <n-select v-model:value="form.sourceType" size="small" :options="sourceOptions" />
        </div>
        <div class="qedit__field">
          <label>难度 1~5</label>
          <n-input-number v-model:value="form.difficulty" size="small" :min="1" :max="5" style="width: 100%" />
        </div>
        <div class="qedit__field">
          <label>上架状态</label>
          <n-select v-model:value="form.status" size="small" :options="statusOptions" />
        </div>
        <div class="qedit__field qedit__field--wide">
          <label>标题(留空自动生成)</label>
          <n-input v-model:value="form.title" size="small" placeholder="如:完形填空 · 2024 省模考一" />
        </div>
        <div class="qedit__field qedit__field--wide">
          <label>标签(逗号分隔)</label>
          <n-input v-model:value="form.tagsText" size="small" placeholder="词义辨析,上下文" />
        </div>
      </div>
    </n-card>

    <!-- 作文专用 -->
    <n-card v-if="form.type === 'writing'" size="small" title="作文题" class="qedit__card">
      <label class="qedit__label">写作题目与情境(prompt)</label>
      <n-input v-model:value="form.prompt" type="textarea" :autosize="{ minRows: 3 }" />
      <label class="qedit__label">要点要求(每行一条)</label>
      <n-input v-model:value="form.requirementsText" type="textarea" :autosize="{ minRows: 2 }" />
      <div class="qedit__row2">
        <div>
          <label class="qedit__label">字数要求</label>
          <n-input-number v-model:value="form.wordLimit" size="small" :min="1" style="width: 100%" />
        </div>
        <div>
          <label class="qedit__label">评分维度(逗号分隔)</label>
          <n-input v-model:value="form.dimensionsText" size="small" />
        </div>
      </div>
      <label class="qedit__label">参考范文(可选,写入 answer_key)</label>
      <n-input v-model:value="form.sampleAnswer" type="textarea" :autosize="{ minRows: 3 }" />
    </n-card>

    <!-- 其余题型:文章 + 行编辑 -->
    <n-card v-else size="small" :title="form.type === 'reading' ? '文章与题目' : '文章与空位'" class="qedit__card">
      <label class="qedit__label">
        文章正文
        <span v-if="needsBlankMark" class="qedit__hint">
          空位请用 [[1]] [[2]] … 占位,编号需与下方行号一致
        </span>
      </label>
      <n-input v-model:value="form.passage" type="textarea" :autosize="{ minRows: 4 }" />

      <!-- 五选五的候选句(顶层 options) -->
      <template v-if="form.type === 'matching'">
        <label class="qedit__label">候选句(每行一条,格式:<code>A 句子内容</code>)</label>
        <n-input v-model:value="form.optionsText" type="textarea" :autosize="{ minRows: 5 }" />
      </template>

      <div
        v-for="(row, index) in form.rows"
        :key="index"
        class="qedit__item"
        :class="{ 'qedit__item--error': errorRowIndex === index }"
      >
        <div class="qedit__item-head">
          <n-tag size="small" :bordered="false" type="info">编号 {{ rowIds[index] }}</n-tag>
          <span class="qedit__item-title">{{ rowTitle(index) }}</span>
          <n-button size="tiny" quaternary type="error" @click="form.rows.splice(index, 1)">删除本行</n-button>
        </div>

        <template v-if="form.type === 'reading'">
          <label class="qedit__label">题干</label>
          <n-input v-model:value="row.stem" size="small" />
        </template>
        <template v-else-if="form.type === 'grammar-fill'">
          <label class="qedit__label">提示词(可空,如 <code>(work)</code>)</label>
          <n-input v-model:value="row.hint" size="small" />
        </template>

        <!-- 选项(阅读四选一 / 完形四选一) -->
        <template v-if="form.type === 'reading' || form.type === 'cloze'">
          <div v-for="option in row.options" :key="option.key" class="qedit__option">
            <span class="qedit__option-key">{{ option.key }}</span>
            <n-input v-model:value="option.text" size="small" placeholder="选项文本" />
          </div>
        </template>

        <label class="qedit__label">
          {{ form.type === 'grammar-fill' ? '标准答案' : '正确选项 key' }}
        </label>
        <n-input v-model:value="row.answer" size="small" :placeholder="form.type === 'grammar-fill' ? 'working' : 'B'" />

        <!-- 可接受变体:判分正确性的瓶颈,独立成区块而不是藏在答案旁边的小格
             (A8 依据:题库 30 个空只有 2 个填了真正有效的变体,真题卷 10 个空一个没填,
              而比对是纯字符串 —— 少填一个写法,学生就可能答对却判错) -->
        <div v-if="form.type === 'grammar-fill'" class="qedit__alt">
          <label class="qedit__label qedit__label--strong">可接受变体（逗号分隔）</label>
          <n-input v-model:value="row.alternativesText" size="small" placeholder="如 worked, had worked" />
          <p class="qedit__alt-note">
            除标准答案外，判分还会接受这里写下的写法。这一空可能存在别的正确答案（同义写法、两种时态都说得通）就填上；
            只影响这一空，不会改动标准答案。大小写差异已由「答题设置」里的大小写开关兜住，不必在此重复。
          </p>
        </div>
        <p v-else-if="form.type !== 'reading'" class="qedit__alt-note">
          本题型按字符串精确比对（阅读/完形/五选五比的是选项 key），暂不支持可接受变体。
        </p>

        <label class="qedit__label">解析</label>
        <n-input v-model:value="row.explanation" size="small" />
      </div>

      <n-button size="small" dashed block @click="addRow">
        + 添加{{ form.type === 'reading' ? '一道小题' : '一个空位' }}
      </n-button>
    </n-card>

    <!-- 高级:提交内容的 JSON 视图。默认只是"将要提交什么"的预览(表单为权威);
         管理员在这里动过手才反客为主,界面顶部会给出对应的提示。 -->
    <n-collapse class="qedit__card">
      <n-collapse-item :title="advancedTitle" name="advanced">
        <div class="qedit__json-actions">
          <n-button size="tiny" @click="syncFromForm">从表单重新生成</n-button>
          <span class="qedit__hint">服务端会做五道校验:结构、答案防呆、编号集合一致、答案合法、占位符一致</span>
        </div>
        <label class="qedit__label">content(下发内容,禁止含 answer/explanation/sampleAnswer)</label>
        <n-input v-model:value="contentJson" type="textarea" :autosize="{ minRows: 6 }" @update:value="manualJson = true" />
        <label class="qedit__label">answer_key(答案与解析)</label>
        <n-input v-model:value="answerKeyJson" type="textarea" :autosize="{ minRows: 4 }" @update:value="manualJson = true" />
      </n-collapse-item>
    </n-collapse>

    <!-- AI 校核建议抽屉(阶段 A10):逐编号列建议,可采纳的只有"可接受变体"和"解析"两类;
         答案风险只提醒不自动改 —— 答案键影响此后所有学生,必须经人。全程不写库。 -->
    <n-drawer v-model:show="reviewShow" :width="isMobile ? '100%' : 620" placement="right">
      <n-drawer-content :title="`AI 校核建议 · ${reviewConcerns} 项提醒`" closable :native-scrollbar="false">
        <p v-if="review?.summary" class="rv__summary">{{ review.summary }}</p>
        <p v-if="review?.droppedCount" class="rv__dropped">
          模型另有 {{ review.droppedCount }} 条建议对不上当前编号,已忽略(通常是它自己编了题号)。
        </p>
        <p v-if="!reviewActionable.length" class="rv__empty">
          {{ review?.items?.length ? '模型回报本题各编号均无问题，没有可采纳的建议。' : '没有可采纳的建议。' }}
        </p>

        <p v-if="reviewActionable.length" class="rv__counts">
          {{ reviewActionable.length }} 个编号有建议或提醒，其余 {{ reviewQuietCount }} 个编号校核后无问题（未列出）。
        </p>
        <div v-for="item in reviewActionable" :key="item.id" class="rv__item">
          <div class="rv__head">
            <n-tag size="small" :bordered="false" type="info">编号 {{ item.id }}</n-tag>
            <span v-if="rowIndexByNumber(item.id) < 0" class="rv__orphan">该编号已不在当前表单中,无法采纳</span>
            <n-tag v-if="item.answerRisk && item.answerRisk !== 'ok'" size="small" type="error" :bordered="false">
              {{ item.answerRisk === 'notUnique' ? '答案可能不唯一' : '答案可能选错' }}
            </n-tag>
            <n-tag v-if="item.explanationRisk && item.explanationRisk !== 'ok'" size="small" type="warning" :bordered="false">
              {{ item.explanationRisk === 'mismatch' ? '解析与答案不一致' : '解析过简' }}
            </n-tag>
          </div>
          <p v-if="item.note" class="rv__note">{{ item.note }}</p>

          <div v-if="item.alternatives?.length" class="rv__row">
            <span class="rv__label">建议的可接受写法</span>
            <n-tag v-for="value in item.alternatives" :key="value" size="small" :bordered="false">{{ value }}</n-tag>
            <n-button
              size="tiny"
              type="primary"
              ghost
              :disabled="adopted[`alt-${item.id}`] || rowIndexByNumber(item.id) < 0"
              @click="adoptAlternatives(item)"
            >
              {{ adopted[`alt-${item.id}`] ? '已采纳' : '采纳' }}
            </n-button>
          </div>

          <div v-if="item.explanation" class="rv__row rv__row--expl">
            <span class="rv__label">建议解析</span>
            <p class="rv__text">{{ item.explanation }}</p>
            <n-button
              size="tiny"
              type="primary"
              ghost
              :disabled="adopted[`expl-${item.id}`] || rowIndexByNumber(item.id) < 0"
              @click="adoptExplanation(item)"
            >
              {{ adopted[`expl-${item.id}`] ? '已采纳' : '采纳(替换该行为准)' }}
            </n-button>
          </div>

          <p v-if="item.answerRisk && item.answerRisk !== 'ok'" class="rv__hint">
            答案不由 AI 改动：请自己核对题面后在表单里改，或给这个空补上可接受写法。
          </p>
        </div>

        <template #footer>
          <div class="rv__footer">
            <span class="rv__count">已采纳 {{ adoptedCount }} 项 · 仍需点「{{ isEdit ? '保存修改' : '创建题目' }}」才对学生生效</span>
            <n-space size="small">
              <n-button size="small" :disabled="!reviewConcerns" @click="adoptAllSafe">采纳全部安全项</n-button>
              <n-button size="small" @click="reviewShow = false">关闭</n-button>
            </n-space>
          </div>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 路由参数 id 存在即为编辑模式:GET /api/admin/questions/{id} 反填表单
// 保存:新建 POST /api/admin/questions;更新 PUT /api/admin/questions/{id}
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NAlert,
  NButton,
  NCard,
  NCollapse,
  NCollapseItem,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputNumber,
  NSpace,
  NSelect,
  NTag,
  NTooltip,
  useMessage,
} from 'naive-ui'
import {
  createAdminQuestion,
  generateAdminAiDraft,
  getAdminQuestion,
  reviewAdminAiQuestion,
  updateAdminQuestion,
} from '@/api/admin'
import type {
  AdminAiReview,
  AdminAiReviewItem,
  AdminQuestionPayload,
  AdminSourceType,
} from '@/types/admin'
import type { PracticeType } from '@/types/practice'
import { useDevice } from '@/composables/useDevice'

/**
 * 行模型:阅读小题或各类空位。
 *
 * <p>三个 `original*` 字段是 passthrough 的依据(A8):编辑已有题时把库里那条 item 原样记住,
 * 保存时以它打底再用表单值覆盖,这样表单没建模的键(如以后 A10 加的校核标记)不会被静默洗掉,
 * 原本的 `id` 编号与 `acceptAlternatives` 的原始形状(null / [] / 缺键)也不会被重排或改写。</p>
 */
interface RowModel {
  stem: string
  hint: string
  options: { key: string; text: string }[]
  answer: string
  alternativesText: string
  explanation: string
  /** 库里 content 对应项原样(未加载到则为 null,新建行也是 null) */
  originalContentItem: Record<string, unknown> | null
  /** 库里 answer_key 对应项原样 */
  originalKeyItem: Record<string, unknown> | null
  /** 加载时由原始 acceptAlternatives 推导出的文本,用于判断"这一格有没有被动过" */
  originalAlternativesText: string
}

const route = useRoute()
const router = useRouter()
const message = useMessage()
/** 抽屉宽度按端切换(校核建议条目在窄屏需要全宽才看得完) */
const { isMobile } = useDevice()

/** 编辑目标 ID(新建时为空串) */
const editId = computed(() => String(route.params.id ?? ''))
const isEdit = computed(() => editId.value !== '')

const saving = ref(false)
/** true = 管理员在「高级」面板里动过 JSON,此时以面板内容为准提交 */
const manualJson = ref(false)
const contentJson = ref('{}')
const answerKeyJson = ref('{}')

/** 库里原始 content / answer_key(passthrough 打底的依据;新建模式为空对象) */
const originalContent = ref<Record<string, unknown>>({})
const originalAnswerKey = ref<Record<string, unknown>>({})
/** 编辑模式反填期间为 true:此时表单的变动不该触发面板重生成 */
const hydrating = ref(false)
/** 服务端拒绝保存时留下的原文与命中的表单行下标(内联展示,不只在 toast 里一闪而过) */
const saveError = ref<string | null>(null)
const errorRowIndex = ref<number | null>(null)

/** AI 草稿状态(阶段 A9):生成中标志 + 已填入草稿的来源信息,用于提示"这是草稿、还没入库" */
const generating = ref(false)
const draftInfo = ref<{ model: string; generatedAt: string } | null>(null)
/** 草稿带来的来源 meta(模型名与生成时刻),保存时随 sourceType=ai 原样带回 */
const draftSourceMeta = ref<Record<string, unknown> | null>(null)

/** AI 校核状态(阶段 A10):建议清单只在抽屉与表单之间流动,全程不写库 */
const reviewing = ref(false)
const reviewShow = ref(false)
const review = ref<AdminAiReview | null>(null)
const reviewError = ref<string | null>(null)
/** 已采纳标记,键为 `alt-<编号>` / `expl-<编号>`(整体替换以触发数组/对象响应式) */
const adopted = ref<Record<string, boolean>>({})

const form = reactive({
  id: '',
  type: 'cloze' as PracticeType,
  sourceType: 'teacher' as AdminSourceType,
  title: '',
  difficulty: 3,
  tagsText: '',
  status: 1,
  passage: '',
  optionsText: '',
  prompt: '',
  requirementsText: '',
  wordLimit: 120,
  dimensionsText: '内容要点,语言表达,篇章结构,格式规范',
  sampleAnswer: '',
  rows: [] as RowModel[],
})

const typeOptions = [
  { label: '阅读理解', value: 'reading' },
  { label: '五选五', value: 'matching' },
  { label: '完形填空', value: 'cloze' },
  { label: '语法填空', value: 'grammar-fill' },
  { label: '作文', value: 'writing' },
]

const sourceOptions = [
  { label: '真题 real', value: 'real' },
  { label: '模拟题 teacher', value: 'teacher' },
  { label: 'AI 生成 ai', value: 'ai' },
]

const statusOptions = [
  { label: '上架(学生端可见)', value: 1 },
  { label: '下架(仅后台可见)', value: 0 },
]

/** 需要 [[n]] 占位符的题型 */
const needsBlankMark = computed(() => form.type === 'matching' || form.type === 'cloze' || form.type === 'grammar-fill')

/** 行标题(表单分组提示用) */
function rowTitle(index: number): string {
  if (form.type === 'reading') return `第 ${index + 1} 题`
  if (form.type === 'matching') return `第 ${index + 1} 空(从候选句中选)`
  if (form.type === 'cloze') return `第 ${index + 1} 空`
  return `第 ${index + 1} 空(语法填空)`
}

/** 新建一行:阅读/完形带四个选项,其余留空;新增行没有"原始项",保存时按末尾续编号 */
function newRow(): RowModel {
  const withOptions = form.type === 'reading' || form.type === 'cloze'
  return {
    stem: '',
    hint: '',
    options: withOptions ? ['A', 'B', 'C', 'D'].map((key) => ({ key, text: '' })) : [],
    answer: '',
    alternativesText: '',
    explanation: '',
    originalContentItem: null,
    originalKeyItem: null,
    originalAlternativesText: '',
  }
}

function addRow(): void {
  form.rows.push(newRow())
}

/** 切换题型时重置行结构 */
function onTypeChange(): void {
  form.rows = []
  form.sampleAnswer = ''
  addRow()
  syncFromForm()
}

/** 解析候选句文本("A 句子" 每行一条) */
function parseOptions(text: string): { key: string; text: string }[] {
  return text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const matched = /^([A-Za-z])[\s.\-、）)]+(.*)$/.exec(line)
      return matched ? { key: matched[1].toUpperCase(), text: matched[2].trim() } : { key: '?', text: line }
    })
}

function splitList(text: string): string[] {
  return text
    .split(/[,，\n]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

/**
 * 表单负责建模的键。不在这些清单里的键(content 的 `id`/`type`/`source` 也算在内,
 * 以及以后 A10 可能加的校核标记)一律从库里原样带回 —— 这就是 A8 的未知字段 passthrough。
 */
const CONTENT_MODELED = ['passage', 'prompt', 'requirements', 'wordLimit', 'scoringDimensions', 'questions', 'blanks', 'options']
const KEY_MODELED = ['sampleAnswer', 'questions', 'blanks']

/** 原始项里的编号;没有或不是数字时返回 null(由续号逻辑补) */
function originalIdOf(original: Record<string, unknown> | null): number | null {
  if (!original || original.id === undefined || original.id === null) return null
  const asNumber = Number(original.id)
  return Number.isInteger(asNumber) ? asNumber : null
}

/**
 * 编号策略:库里原编号照原样用,只有新增行才从当前最大编号往后续(空题库从 1 起)。
 * 旧实现一律按数组顺序重排成 `index + 1`,于是"打开老题什么都不改"也会改写编号。
 */
function resolveRowIds(rows: RowModel[]): number[] {
  const existing = rows.map((row) => originalIdOf(row.originalContentItem ?? row.originalKeyItem))
  let highest = 0
  for (const id of existing) {
    if (id !== null && id > highest) highest = id
  }
  let next = highest
  return existing.map((id) => {
    if (id !== null) return id
    next += 1
    return next
  })
}

/** 顶层合并:原始打底 → 删掉表单已清空/不适用的建模键 → 表单值覆盖 */
function mergeTop(
  original: Record<string, unknown>,
  built: Record<string, unknown>,
  modeled: string[]
): Record<string, unknown> {
  const out: Record<string, unknown> = { ...original }
  for (const key of modeled) {
    if (!(key in built)) delete out[key]
  }
  return { ...out, ...built }
}

/** 单条 item 合并:原始 item 打底,未知键随之带回;`removed` 表示"表单明确清空了它" */
function mergeItem(
  original: Record<string, unknown> | null,
  built: Record<string, unknown>,
  removed: string[] = []
): Record<string, unknown> {
  const base: Record<string, unknown> = { ...(original ?? {}) }
  for (const key of removed) delete base[key]
  return { ...base, ...built }
}

/** 由原始 acceptAlternatives 推导表单文本(null / 缺键 → 空串,[] 同样是空串) */
function alternativesTextOf(item: Record<string, unknown> | null): string {
  const raw = item?.acceptAlternatives
  return Array.isArray(raw) ? raw.map((one) => String(one)).join(',') : ''
}

/** 由表单生成"将要提交"的 content 与 answer_key(已合并库里原始内容) */
function buildPayloads(): { content: Record<string, unknown>; answerKey: Record<string, unknown> } {
  const ids = resolveRowIds(form.rows)

  if (form.type === 'writing') {
    const builtKey: Record<string, unknown> = {}
    if (form.sampleAnswer.trim()) {
      builtKey.sampleAnswer = form.sampleAnswer.trim()
    }
    return {
      content: mergeTop(originalContent.value, {
        prompt: form.prompt,
        requirements: form.requirementsText.split('\n').map((line) => line.trim()).filter(Boolean),
        wordLimit: form.wordLimit,
        scoringDimensions: splitList(form.dimensionsText),
      }, CONTENT_MODELED),
      answerKey: mergeTop(originalAnswerKey.value, builtKey, KEY_MODELED),
    }
  }

  const itemsField = form.type === 'reading' ? 'questions' : 'blanks'
  const builtContent: Record<string, unknown> = { passage: form.passage }
  const builtKey: Record<string, unknown> = {}

  if (form.type === 'matching') {
    builtContent.options = parseOptions(form.optionsText)
  }

  builtContent[itemsField] = form.rows.map((row, index) => {
    const built: Record<string, unknown> = { id: ids[index] }
    const removed: string[] = []
    if (form.type === 'reading') {
      built.stem = row.stem
      built.options = row.options
    } else if (form.type === 'cloze') {
      built.options = row.options
      removed.push('stem', 'hint')
    } else if (form.type === 'matching') {
      removed.push('stem', 'hint', 'options')
    } else {
      // grammar-fill:提示词留空表示"这一空不给提示",要真的删掉而不是传空串
      if (row.hint.trim()) built.hint = row.hint.trim()
      else removed.push('hint')
      removed.push('stem', 'options')
    }
    return mergeItem(row.originalContentItem, built, removed)
  })

  builtKey[itemsField] = form.rows.map((row, index) => {
    const built: Record<string, unknown> = {
      id: ids[index],
      answer: form.type === 'grammar-fill' ? row.answer.trim() : row.answer.trim().toUpperCase(),
      explanation: row.explanation,
    }
    const removed: string[] = []
    if (form.type === 'grammar-fill') {
      const alternatives = splitList(row.alternativesText)
      if (alternatives.length > 0) {
        built.acceptAlternatives = alternatives
      } else if (row.alternativesText !== row.originalAlternativesText) {
        // 只有管理员真的清空过才删键;没动过时原形状(null / [] / 缺键)逐字保留
        removed.push('acceptAlternatives')
      }
    }
    return mergeItem(row.originalKeyItem, built, removed)
  })

  return {
    content: mergeTop(originalContent.value, builtContent, CONTENT_MODELED),
    answerKey: mergeTop(originalAnswerKey.value, builtKey, KEY_MODELED),
  }
}

/** 每行实际会提交的编号(原编号沿用 + 新增行续号);界面据此显示真实题号而不是数组序号 */
const rowIds = computed(() => resolveRowIds(form.rows))

/** 「高级」折叠区标题:一眼看出当前谁做权威(默认表单;管理员动过面板则是面板) */
const advancedTitle = computed(() =>
  manualJson.value
    ? '高级 · 直接编辑提交 JSON（当前以本面板为准）'
    : '高级 · 本次将提交的 JSON（由表单生成，此处为预览）'
)

/**
 * 表单没建模、正被原样带回的顶层键。
 * `id`/`type`/`source` 不计入提示:它们虽然也走 passthrough 带回,但服务端一定会覆写(见 `AdminQuestionService`),
 * 列出来只会让每条题都显示一串噪音,反而看不出"真的有自定义键被保住了"。
 */
const SERVER_OWNED_KEYS = ['id', 'type', 'source']
const passthroughKeys = computed(() =>
  [...Object.keys(originalContent.value), ...Object.keys(originalAnswerKey.value)].filter(
    (key) =>
      !CONTENT_MODELED.includes(key) && !KEY_MODELED.includes(key) && !SERVER_OWNED_KEYS.includes(key)
  )
)

/** 表单 → JSON 面板(面板始终等于"此刻会提交的内容";手工改过 JSON 后可点本按钮回到表单内容) */
function syncFromForm(): void {
  const { content, answerKey } = buildPayloads()
  contentJson.value = JSON.stringify(content, null, 2)
  answerKeyJson.value = JSON.stringify(answerKey, null, 2)
  manualJson.value = false
}

/**
 * 把一份 content / answer_key 灌进表单与高级面板。
 *
 * <p>编辑反填与 AI 草稿填入<b>共用这一条路径</b>,两者只是数据来源不同:
 * 反填来自库里的详情(含 id/type/source 等权威键),草稿来自模型(不含权威键)。
 * 灌完后面板显示的就是"此刻会提交的内容",表单保持权威(A8)。</p>
 *
 * <p>期间用 `hydrating` 挡住 watcher:表单被逐字段赋值会排进本次 flush 队列,
 * 等 `nextTick()` 之后再放开,否则面板会被 watcher 用表单结果重写(就是 A8 修掉的那个覆盖 bug)。</p>
 */
async function hydrateFrom(
  content: Record<string, unknown>,
  answerKey: Record<string, unknown>
): Promise<void> {
  hydrating.value = true
  // 留存原始对象:保存时以此打底,表单表达不出来的键才能被原样带回
  originalContent.value = content
  originalAnswerKey.value = answerKey
  form.passage = String(content.passage ?? '')
  form.prompt = String(content.prompt ?? '')
  form.wordLimit = Number(content.wordLimit ?? 120)
  form.requirementsText = ((content.requirements as string[]) ?? []).join('\n')
  form.dimensionsText = ((content.scoringDimensions as string[]) ?? []).join(',')
  form.sampleAnswer = String(answerKey.sampleAnswer ?? '')
  form.optionsText = ((content.options as { key: string; text: string }[]) ?? [])
    .map((option) => `${option.key} ${option.text}`)
    .join('\n')

  const keyItems = ((answerKey.questions ?? answerKey.blanks ?? []) as Record<string, unknown>[])
  const sourceItems = ((content.questions ?? content.blanks ?? []) as Record<string, unknown>[])
  form.rows = sourceItems.map((item) => {
    const itemId = Number(item.id ?? 0)
    const key = keyItems.find((entry) => Number(entry.id) === itemId) ?? null
    const options = (item.options as { key: string; text: string }[]) ?? []
    return {
      stem: String(item.stem ?? ''),
      hint: String(item.hint ?? ''),
      options: options.map((option) => ({ key: option.key, text: option.text })),
      answer: String(key?.answer ?? ''),
      alternativesText: alternativesTextOf(key),
      explanation: String(key?.explanation ?? ''),
      originalContentItem: item,
      originalKeyItem: key,
      originalAlternativesText: alternativesTextOf(key),
    }
  })

  contentJson.value = JSON.stringify(content, null, 2)
  answerKeyJson.value = JSON.stringify(answerKey, null, 2)
  manualJson.value = false
  await nextTick()
  hydrating.value = false
}

/** 编辑模式:GET 详情 → 基本信息 + 反填 */
async function loadForEdit(id: string): Promise<void> {
  const detail = await getAdminQuestion(id)
  form.id = detail.id
  form.type = detail.type
  form.sourceType = detail.sourceType
  form.title = detail.title ?? ''
  form.difficulty = detail.difficulty ?? 3
  form.status = detail.status ?? 1
  form.tagsText = (detail.tags ?? []).join(',')
  await hydrateFrom((detail.content ?? {}) as Record<string, unknown>,
    (detail.answerKey ?? {}) as Record<string, unknown>)
}

/**
 * AI 生成一版草稿(阶段 A9):POST /admin/questions/ai-draft → 灌进表单。
 *
 * <p>三条硬约束:① 草稿只填表单,不写 JSON 面板(面板只是提交内容预览);
 * ② <b>不落库、不上架</b>,要点「创建题目」才入库(与学生端"点即生成即上架"是两条不同路径);
 * ③ 只在新建模式提供 —— 编辑已有题时整题替换会覆盖人工内容并丢历史对应关系,不给这个按钮。</p>
 */
async function onGenerateDraft(): Promise<void> {
  generating.value = true
  saveError.value = null
  errorRowIndex.value = null
  try {
    const draft = await generateAdminAiDraft(form.type)
    // 题型以服务端返回为准(留空兜底,正常应与请求一致)
    form.type = draft.type ?? form.type
    if (draft.title) form.title = draft.title
    if (draft.difficulty) form.difficulty = draft.difficulty
    if (draft.tags?.length) form.tagsText = draft.tags.join(',')
    // 来源如实标为 AI,并把模型名与生成时刻存下来,保存时随 sourceType=ai 一起带回
    form.sourceType = 'ai'
    draftSourceMeta.value = (draft.sourceMeta as Record<string, unknown>) ?? null
    draftInfo.value = {
      model: String(draft.sourceMeta?.model ?? '未知模型'),
      generatedAt: String(draft.sourceMeta?.generatedAt ?? ''),
    }
    await hydrateFrom((draft.content ?? {}) as Record<string, unknown>,
      (draft.answerKey ?? {}) as Record<string, unknown>)
    message.success('AI 草稿已填入表单,尚未保存 —— 改完点「创建题目」才入库')
  } catch (error) {
    saveError.value = error instanceof Error ? error.message : String(error)
  } finally {
    generating.value = false
  }
}

/** 草稿生成时刻显示成"月-日 时:分"(服务端给的是 ISO 串,拿不到就留空,不猜) */
function formatDraftTime(raw: string): string {
  if (!raw) return ''
  const matched = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(raw)
  return matched ? `${matched[2]}-${matched[3]} ${matched[4]}:${matched[5]}` : ''
}

/**
 * AI 校核本题(阶段 A10):把**表单当前内容**交给服务端出建议,采纳后仍需点保存才落库。
 *
 * <p>三个刻意的设计:</p>
 * <ul>
 *   <li>送检的是 `buildPayloads()` 的结果(与保存时会提交的内容一致),不是库里的旧版本 ——
 *       所以新建题与 AI 草稿在保存前就能校核;</li>
 *   <li>{@code answerRisk} <b>没有采纳按钮</b>:答案键影响此后所有学生,AI 只能提醒,改与不改由人决定;</li>
 *   <li>变体采纳是"并集"而不是"覆盖",避免把管理员已经手填的写法冲掉。</li>
 * </ul>
 */
async function onReview(): Promise<void> {
  const { content, answerKey } = buildPayloads()
  reviewing.value = true
  reviewError.value = null
  review.value = null
  try {
    const result = await reviewAdminAiQuestion({ type: form.type, content, answerKey })
    review.value = result
    adopted.value = {}
    reviewShow.value = true
    if (!result.items?.length) {
      message.info(result.droppedCount
        ? `模型返回的 ${result.droppedCount} 条建议都对不上当前编号,已忽略`
        : '没有发现问题,也没有新的可采纳建议')
    }
  } catch (error) {
    reviewError.value = error instanceof Error ? error.message : String(error)
    message.error(reviewError.value)
  } finally {
    reviewing.value = false
  }
}

/** 按建议编号找表单行下标(编号来自 rowIds,与行上显示的「编号 N」一致) */
function rowIndexByNumber(id: number): number {
  return rowIds.value.indexOf(id)
}

/** 采纳可接受变体:与已有写法取并集,并剔除与该空标准答案相同的项 */
function adoptAlternatives(item: AdminAiReviewItem): void {
  const index = rowIndexByNumber(item.id)
  if (index < 0 || !item.alternatives?.length) return
  const row = form.rows[index]
  const answer = row.answer.trim()
  const merged: string[] = []
  for (const value of [...splitList(row.alternativesText), ...item.alternatives]) {
    const token = value.trim()
    if (token && token !== answer && !merged.includes(token)) {
      merged.push(token)
    }
  }
  row.alternativesText = merged.join(', ')
  adopted.value = { ...adopted.value, [`alt-${item.id}`]: true }
}

/** 采纳解析建议(整条替换,管理员可再手改) */
function adoptExplanation(item: AdminAiReviewItem): void {
  const index = rowIndexByNumber(item.id)
  if (index < 0 || !item.explanation) return
  form.rows[index].explanation = item.explanation
  adopted.value = { ...adopted.value, [`expl-${item.id}`]: true }
}

/** 一键采纳所有安全项(只改变体与解析,绝不碰答案与编号) */
function adoptAllSafe(): void {
  for (const item of review.value?.items ?? []) {
    if (item.alternatives?.length) adoptAlternatives(item)
    if (item.explanation) adoptExplanation(item)
  }
}

/** 有风险提醒或可采纳建议的行数(用于抽屉标题与按钮提示) */
function hasSubstance(item: AdminAiReviewItem): boolean {
  return (item.alternatives?.length ?? 0) > 0
    || Boolean(item.explanation)
    || item.answerRisk === 'mayBeWrong'
    || item.answerRisk === 'notUnique'
    || item.explanationRisk === 'mismatch'
    || item.explanationRisk === 'tooThin'
}
const reviewConcerns = computed(() => (review.value?.items ?? []).filter(hasSubstance).length)

/**
 * 抽屉里真正要列的建议。
 *
 * <p>模型对"没问题"的编号也会回一条全 ok 的记录(实测 10 空题回满 10 条),
 * 原样列出来就是 9 行空白壳 + 1 行有用信息,反而看不到重点 —— 所以只列有内容的,</p>
 */
const reviewActionable = computed(() => (review.value?.items ?? []).filter(hasSubstance))
const reviewQuietCount = computed(() => (review.value?.items?.length ?? 0) - reviewActionable.value.length)

/** 采纳计数(抽屉底部展示"已采纳 x/y",让"还没保存"这件事可核对) */
const adoptedCount = computed(() => Object.values(adopted.value).filter(Boolean).length)

/** 丢弃草稿:回到空白新建态(只清内容字段,不动题型与上下架选择) */
function onDiscardDraft(): void {
  draftInfo.value = null
  draftSourceMeta.value = null
  // 一并清掉"passthrough 打底"的原始对象,否则丢弃草稿后仍会把草稿里的键带回下一次提交
  originalContent.value = {}
  originalAnswerKey.value = {}
  form.title = ''
  form.tagsText = ''
  form.passage = ''
  form.prompt = ''
  form.requirementsText = ''
  form.sampleAnswer = ''
  form.optionsText = ''
  form.rows = [newRow()]
  syncFromForm()
  message.info('已丢弃 AI 草稿')
}

/** 保存前本地预检:答案键是否误填进 content、占位符与行号是否一致 */
function precheck(content: Record<string, unknown>): string | null {
  const text = JSON.stringify(content)
  if (/"(answer|explanation|sampleAnswer|acceptAlternatives)"\s*:/.test(text)) {
    return 'content 中含有答案类字段,请只放在 answer_key 里(服务端也会拒绝)'
  }
  if (needsBlankMark.value) {
    const marks = new Set(
      [...String(content.passage ?? '').matchAll(/\[\[(\d+)]]/g)].map((m) => Number(m[1]))
    )
    const ids = ((content.blanks as { id: number }[]) ?? []).map((blank) => blank.id)
    if (marks.size !== ids.length || !ids.every((id) => marks.has(id))) {
      return `passage 的 [[n]] 占位符 ${[...marks]} 与空位编号 ${ids} 不一致(服务端会拒绝保存)`
    }
  }
  return null
}

/**
 * 从服务端报错文本里取出出错的下标。
 * `QuestionContentValidator` 的路径形如 `answer_key.blanks[3].answer 不能为空`,其中方括号是**数组下标(0 基)**,
 * 不是题目编号 —— 而两个数组都由 `form.rows` 按序生成,所以下标可直接对应到表单行。
 */
function errorRowIndexOf(text: string): number | null {
  const matched = /(?:content|answer_key)\.(?:blanks|questions)\[(\d+)]/.exec(text)
  if (!matched) return null
  const index = Number(matched[1])
  return index >= 0 && index < form.rows.length ? index : null
}

async function onSave(): Promise<void> {
  saveError.value = null
  errorRowIndex.value = null
  let content: Record<string, unknown>
  let answerKey: Record<string, unknown>

  if (manualJson.value) {
    // 管理员在「高级」面板里动过 JSON:此时以面板为准(面板本来也由合并结果起步)
    try {
      content = JSON.parse(contentJson.value) as Record<string, unknown>
      answerKey = answerKeyJson.value.trim() ? (JSON.parse(answerKeyJson.value) as Record<string, unknown>) : {}
    } catch {
      saveError.value = '高级面板里的 JSON 存在语法错误,请修正,或点「从表单重新生成」回到表单内容'
      message.error(saveError.value)
      return
    }
  } else {
    // 表单为权威:提交"库里原始内容打底 + 表单覆盖"的合并结果,并让面板与之一致
    const merged = buildPayloads()
    content = merged.content
    answerKey = merged.answerKey
    contentJson.value = JSON.stringify(content, null, 2)
    answerKeyJson.value = JSON.stringify(answerKey, null, 2)
  }

  const problem = precheck(content)
  if (problem) {
    saveError.value = problem
    message.warning(problem)
    return
  }

  const payload: AdminQuestionPayload = {
    id: isEdit.value ? undefined : form.id || undefined,
    type: form.type,
    sourceType: form.sourceType,
    title: form.title || undefined,
    difficulty: form.difficulty,
    status: form.status,
    tags: splitList(form.tagsText),
    content,
    answerKey,
    // AI 草稿带来的来源信息只在来源仍为 ai 时带回;改成真题/模拟题后由服务端按原口径覆写
    sourceMeta: form.sourceType === 'ai' ? (draftSourceMeta.value ?? undefined) : undefined,
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateAdminQuestion(editId.value, payload)
      message.success('已保存')
    } else {
      const created = await createAdminQuestion(payload)
      message.success(`已创建:${created.id}`)
    }
    router.push('/admin/questions')
  } catch (error) {
    // request 层已经弹过一次 toast,但带路径的校验信息一闪而过就没人能改了 —— 这里把它钉在页面上并定位到行
    const text = error instanceof Error ? error.message : String(error)
    saveError.value = text
    errorRowIndex.value = errorRowIndexOf(text)
  } finally {
    saving.value = false
  }
}

// 表单任何改动都重新生成面板(管理员已手工编辑 JSON 时不覆盖其改动;反填期间挂起)
watch(form, () => {
  if (!manualJson.value && !hydrating.value) syncFromForm()
}, { deep: true })

onMounted(async () => {
  if (isEdit.value) {
    await loadForEdit(editId.value)
  } else {
    addRow()
    syncFromForm()
  }
})
</script>

<style scoped>
.qedit__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.qedit__title {
  margin: 0;
  font-size: 20px;
}

.qedit__notice {
  margin-bottom: 12px;
}

.qedit__card {
  margin-bottom: 12px;
}

.qedit__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 12px;
}

.qedit__field--wide {
  grid-column: span 2;
}

.qedit__field label,
.qedit__label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.qedit__label {
  margin-top: 12px;
}

.qedit__item {
  border: 1px solid var(--lc-border, rgba(128, 128, 128, 0.24));
  border-radius: 8px;
  padding: 12px;
  margin: 12px 0;
}

.qedit__item-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.qedit__item-title {
  flex: 1;
  font-size: 13px;
}

.qedit__option {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.qedit__option-key {
  width: 18px;
  font-weight: 600;
  text-align: center;
}

.qedit__row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.qedit__json-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.qedit__hint {
  font-size: 12px;
  opacity: 0.7;
  font-weight: 400;
}

/* 服务端拒绝保存时命中的那一行:红框 + 顶部保留报错文本,让人知道去改哪里 */
.qedit__item--error {
  border-color: var(--lc-error, #d03050);
  box-shadow: inset 0 0 0 1px rgba(208, 48, 80, 0.35);
}

/* 可接受变体独立成区块(判分正确性相关,不该藏在角落) */
.qedit__alt {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px dashed rgba(96, 149, 235, 0.55);
  border-radius: 8px;
  background: rgba(96, 149, 235, 0.06);
}

.qedit__alt .qedit__label {
  margin-top: 0;
}

.qedit__alt-note {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  opacity: 0.72;
}

.qedit__label--strong {
  font-weight: 600;
  opacity: 0.9;
}

/* passthrough 生效说明:表单表达不出的键仍会被原样保留 */
.qedit__kept {
  margin: 0 0 12px;
  font-size: 12px;
  opacity: 0.72;
}

.qedit__error strong {
  font-weight: 600;
}

/* AI 草稿提示条:文案与「丢弃草稿」按钮同行,窄屏自动换行 */
.qedit__draftbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

/* ===== AI 校核建议抽屉(阶段 A10) ===== */
.rv__summary {
  margin: 0 0 12px;
  font-size: 13px;
}

.rv__counts {
  margin: 0 0 12px;
  font-size: 12px;
  opacity: 0.75;
}

.rv__dropped {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--lc-warning-text, #a06a00);
}

.rv__empty {
  margin: 0;
  font-size: 13px;
  opacity: 0.75;
}

.rv__item {
  border: 1px solid var(--lc-border, rgba(128, 128, 128, 0.24));
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.rv__head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.rv__orphan {
  font-size: 12px;
  color: var(--lc-error, #d03050);
}

.rv__note {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.6;
  opacity: 0.85;
}

.rv__row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.rv__row--expl {
  display: block;
}

.rv__label {
  font-size: 12px;
  opacity: 0.75;
  margin-right: 4px;
}

.rv__text {
  margin: 4px 0 6px;
  font-size: 13px;
  line-height: 1.7;
}

.rv__hint {
  margin: 8px 0 0;
  font-size: 12px;
  opacity: 0.72;
}

.rv__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.rv__count {
  font-size: 12px;
  opacity: 0.8;
}
</style>
