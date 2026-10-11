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
        <n-button size="small" type="primary" :loading="saving" @click="onSave">
          {{ isEdit ? '保存修改' : '创建题目' }}
        </n-button>
      </n-space>
    </div>

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
  NInput,
  NInputNumber,
  NSpace,
  NSelect,
  NTag,
  useMessage,
} from 'naive-ui'
import {
  createAdminQuestion,
  getAdminQuestion,
  updateAdminQuestion,
} from '@/api/admin'
import type { AdminQuestionPayload, AdminSourceType } from '@/types/admin'
import type { PracticeType } from '@/types/practice'

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

/** 编辑模式:详情反填表单与 JSON(反填期间不让 watcher 重生成面板,见 A8 的覆盖 bug) */
async function loadForEdit(id: string): Promise<void> {
  const detail = await getAdminQuestion(id)
  hydrating.value = true
  form.id = detail.id
  form.type = detail.type
  form.sourceType = detail.sourceType
  form.title = detail.title ?? ''
  form.difficulty = detail.difficulty ?? 3
  form.status = detail.status ?? 1
  form.tagsText = (detail.tags ?? []).join(',')

  const content = (detail.content ?? {}) as Record<string, unknown>
  const answerKey = (detail.answerKey ?? {}) as Record<string, unknown>
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
  // 反填造成的表单变动会被排进本次 flush 队列,等它跑完再放开同步,否则面板会被表单重写成与库里不一致
  await nextTick()
  hydrating.value = false
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
</style>
