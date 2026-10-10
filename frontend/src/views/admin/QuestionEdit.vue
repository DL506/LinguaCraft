<!-- 后台题目录入/编辑(阶段 A2):分题型结构化表单 + 原始 JSON 面板
     同步规则:表单改动实时生成下方 JSON;若手工编辑 JSON,则以 JSON 为准提交(表单可能不同步,界面会提示) -->
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
      你已手工编辑 JSON,上方表单可能与 JSON 不一致 —— 提交时以 JSON 面板为准。
    </n-alert>

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

      <div v-for="(row, index) in form.rows" :key="index" class="qedit__item">
        <div class="qedit__item-head">
          <n-tag size="small" :bordered="false" type="info">{{ index + 1 }}</n-tag>
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

        <div class="qedit__row2">
          <div>
            <label class="qedit__label">
              {{ form.type === 'grammar-fill' ? '标准答案' : '正确选项 key' }}
            </label>
            <n-input v-model:value="row.answer" size="small" :placeholder="form.type === 'grammar-fill' ? 'working' : 'B'" />
          </div>
          <div v-if="form.type === 'grammar-fill'">
            <label class="qedit__label">可接受变体(逗号分隔)</label>
            <n-input v-model:value="row.alternativesText" size="small" />
          </div>
        </div>
        <label class="qedit__label">解析</label>
        <n-input v-model:value="row.explanation" size="small" />
      </div>

      <n-button size="small" dashed block @click="addRow">
        + 添加{{ form.type === 'reading' ? '一道小题' : '一个空位' }}
      </n-button>
    </n-card>

    <!-- 原始 JSON 面板(复杂结构兜底;提交时以此为准) -->
    <n-card size="small" title="提交内容(原始 JSON,可直接编辑)" class="qedit__card">
      <div class="qedit__json-actions">
        <n-button size="tiny" @click="syncFromForm">从表单重新生成</n-button>
        <span class="qedit__hint">服务端会做五道校验:结构、答案防呆、编号集合一致、答案合法、占位符一致</span>
      </div>
      <label class="qedit__label">content(下发内容,禁止含 answer/explanation/sampleAnswer)</label>
      <n-input v-model:value="contentJson" type="textarea" :autosize="{ minRows: 6 }" @update:value="manualJson = true" />
      <label class="qedit__label">answer_key(答案与解析)</label>
      <n-input v-model:value="answerKeyJson" type="textarea" :autosize="{ minRows: 4 }" @update:value="manualJson = true" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
// 路由参数 id 存在即为编辑模式:GET /api/admin/questions/{id} 反填表单
// 保存:新建 POST /api/admin/questions;更新 PUT /api/admin/questions/{id}
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAlert, NButton, NCard, NInput, NInputNumber, NSpace, NSelect, NTag, useMessage } from 'naive-ui'
import {
  createAdminQuestion,
  getAdminQuestion,
  updateAdminQuestion,
} from '@/api/admin'
import type { AdminQuestionPayload, AdminSourceType } from '@/types/admin'
import type { PracticeType } from '@/types/practice'

/** 行模型:阅读小题或各类空位(编号由数组顺序决定,与前端契约一致) */
interface RowModel {
  stem: string
  hint: string
  options: { key: string; text: string }[]
  answer: string
  alternativesText: string
  explanation: string
}

const route = useRoute()
const router = useRouter()
const message = useMessage()

/** 编辑目标 ID(新建时为空串) */
const editId = computed(() => String(route.params.id ?? ''))
const isEdit = computed(() => editId.value !== '')

const saving = ref(false)
const manualJson = ref(false)
const contentJson = ref('{}')
const answerKeyJson = ref('{}')

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

/** 新建一行:阅读/完形带四个选项,其余留空 */
function newRow(): RowModel {
  const withOptions = form.type === 'reading' || form.type === 'cloze'
  return {
    stem: '',
    hint: '',
    options: withOptions ? ['A', 'B', 'C', 'D'].map((key) => ({ key, text: '' })) : [],
    answer: '',
    alternativesText: '',
    explanation: '',
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

/** 由表单生成 content 与 answer_key 两个对象 */
function buildPayloads(): { content: Record<string, unknown>; answerKey: Record<string, unknown> } {
  const content: Record<string, unknown> = {}
  const answerKey: Record<string, unknown> = {}

  if (form.type === 'writing') {
    content.prompt = form.prompt
    content.requirements = form.requirementsText.split('\n').map((l) => l.trim()).filter(Boolean)
    content.wordLimit = form.wordLimit
    content.scoringDimensions = splitList(form.dimensionsText)
    if (form.sampleAnswer.trim()) {
      answerKey.sampleAnswer = form.sampleAnswer.trim()
    }
    return { content, answerKey }
  }

  content.passage = form.passage
  if (form.type === 'reading') {
    content.questions = form.rows.map((row, index) => ({
      id: index + 1,
      stem: row.stem,
      options: row.options,
    }))
    answerKey.questions = form.rows.map((row, index) => ({
      id: index + 1,
      answer: row.answer.trim().toUpperCase(),
      explanation: row.explanation,
    }))
    return { content, answerKey }
  }

  content.blanks = form.rows.map((row, index) => {
    const blank: Record<string, unknown> = { id: index + 1 }
    if (form.type === 'cloze') blank.options = row.options
    if (form.type === 'grammar-fill' && row.hint.trim()) blank.hint = row.hint.trim()
    return blank
  })
  if (form.type === 'matching') content.options = parseOptions(form.optionsText)

  answerKey.blanks = form.rows.map((row, index) => {
    const item: Record<string, unknown> = {
      id: index + 1,
      answer: form.type === 'grammar-fill' ? row.answer.trim() : row.answer.trim().toUpperCase(),
      explanation: row.explanation,
    }
    const alternatives = splitList(row.alternativesText)
    if (form.type === 'grammar-fill' && alternatives.length > 0) item.acceptAlternatives = alternatives
    return item
  })
  return { content, answerKey }
}

/** 表单 → JSON 面板(单向同步;手工改过 JSON 后可点本按钮回到表单内容) */
function syncFromForm(): void {
  const { content, answerKey } = buildPayloads()
  contentJson.value = JSON.stringify(content, null, 2)
  answerKeyJson.value = JSON.stringify(answerKey, null, 2)
  manualJson.value = false
}

/** 编辑模式:详情反填表单与 JSON */
async function loadForEdit(id: string): Promise<void> {
  const detail = await getAdminQuestion(id)
  form.id = detail.id
  form.type = detail.type
  form.sourceType = detail.sourceType
  form.title = detail.title ?? ''
  form.difficulty = detail.difficulty ?? 3
  form.status = detail.status ?? 1
  form.tagsText = (detail.tags ?? []).join(',')

  const content = (detail.content ?? {}) as Record<string, unknown>
  const answerKey = (detail.answerKey ?? {}) as Record<string, unknown>
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
    const id = Number(item.id ?? 0)
    const key = keyItems.find((entry) => Number(entry.id) === id) ?? {}
    const options = (item.options as { key: string; text: string }[]) ?? []
    return {
      stem: String(item.stem ?? ''),
      hint: String(item.hint ?? ''),
      options: options.map((option) => ({ key: option.key, text: option.text })),
      answer: String(key.answer ?? ''),
      alternativesText: ((key.acceptAlternatives as string[]) ?? []).join(','),
      explanation: String(key.explanation ?? ''),
    }
  })

  contentJson.value = JSON.stringify(content, null, 2)
  answerKeyJson.value = JSON.stringify(answerKey, null, 2)
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

async function onSave(): Promise<void> {
  let content: Record<string, unknown>
  let answerKey: Record<string, unknown>
  try {
    content = JSON.parse(contentJson.value) as Record<string, unknown>
    answerKey = answerKeyJson.value.trim() ? (JSON.parse(answerKeyJson.value) as Record<string, unknown>) : {}
  } catch {
    message.error('JSON 面板存在语法错误,请先修正或点「从表单重新生成」')
    return
  }
  const problem = precheck(content)
  if (problem) {
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
  } catch {
    /* 服务端校验失败的具体原因由 request 层弹出(含定位路径) */
  } finally {
    saving.value = false
  }
}

// 表单任何改动都重新生成 JSON(除非用户已手工编辑,避免覆盖其改动)
watch(form, () => {
  if (!manualJson.value) syncFromForm()
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
</style>
