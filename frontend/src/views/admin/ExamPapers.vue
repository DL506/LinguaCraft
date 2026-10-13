<!-- 后台真题卷管理(阶段 A6-③ 列表/A11 结构化编辑):列表 + 分部分表单编辑 + 上下架 + 引用保护删除
     A11 起不再直接改两段 JSON:卷面存本节题序、答题卡存全局编号,编号由表单按 part 升序累加自动推导,
     从源头消灭"卷面题号与答案编号错位 → 交卷静默判错";未建模字段随库里原始节点带回 -->
<template>
  <div class="papers">
    <div class="papers__head">
      <h2 class="papers__title">真题卷管理</h2>
      <n-button size="small" type="primary" @click="openEditor(null)">新建试卷</n-button>
    </div>

    <div class="papers__filters">
      <n-select
        v-model:value="filterStatus"
        size="small"
        :options="statusOptions"
        clearable
        placeholder="全部状态"
        style="width: 132px"
        @update:value="reload"
      />
      <n-input
        v-model:value="keyword"
        size="small"
        placeholder="标题关键词"
        clearable
        style="width: 220px"
        @keyup.enter="reload"
        @clear="reload"
      />
      <n-button size="small" @click="reload">查询</n-button>
    </div>

    <n-alert type="warning" :show-icon="false" class="papers__notice">
      模考客观题判分<b>按答题卡全局编号配对</b>。编号口径(任务书 11.7):卷面每题只存<b>本节题序</b>,
      答题卡全局编号 = 前序各部分题量累加 + 本节题序;表单已按此自动推导,不需要手填任何编号。
      保存前服务端仍会硬校验(部分号唯一、分值之和等于总分、答案覆盖题量、写作恰好一个且 15 分)。
    </n-alert>

    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="false"
      :scroll-x="1120"
      :row-key="(row: AdminExamPaper) => row.id"
    />

    <div class="papers__pager">
      <n-pagination :page="page" :page-size="PAGE_SIZE" :item-count="total" @update:page="onPageChange" />
      <span class="papers__count">共 {{ total }} 张卷</span>
    </div>

    <n-drawer v-model:show="editorShow" :width="1040" placement="right">
      <n-drawer-content :title="form.id ? `编辑试卷 · ${form.id}` : '新建试卷'" closable :native-scrollbar="false">
        <n-spin :show="editorLoading">
          <div class="papers__row2">
            <div>
              <label class="papers__label">标题</label>
              <n-input v-model:value="form.title" size="small" />
            </div>
            <div>
              <label class="papers__label">年份</label>
              <n-input-number v-model:value="form.year" size="small" :min="2000" style="width: 100%" />
            </div>
          </div>
          <div class="papers__row3">
            <div>
              <label class="papers__label">卷面总分</label>
              <n-input-number v-model:value="form.totalScore" size="small" :min="1" style="width: 100%" />
            </div>
            <div>
              <label class="papers__label">时长(分钟)</label>
              <n-input-number v-model:value="form.duration" size="small" :min="1" style="width: 100%" />
            </div>
            <div>
              <label class="papers__label">上下架</label>
              <n-select v-model:value="form.status" size="small" :options="editorStatusOptions" />
            </div>
          </div>

          <div class="papers__checks">
            <n-tag v-if="!errors.length" size="small" type="success" :bordered="false">
              校验通过 · 客观题 {{ totalCardCount }} 题(答题卡 1~{{ totalCardCount }})
            </n-tag>
            <template v-else>
              <n-tag size="small" type="error" :bordered="false">不能保存:{{ errors.length }} 项</n-tag>
              <ul class="papers__check-list">
                <li v-for="(line, index) in errors.slice(0, 6)" :key="index">{{ line }}</li>
              </ul>
            </template>
            <ul v-if="warnings.length" class="papers__check-list papers__check-list--warn">
              <li v-for="(line, index) in warnings.slice(0, 4)" :key="index">提醒:{{ line }}</li>
            </ul>
          </div>

          <n-tabs v-model:value="activeTab" type="card" size="small" class="papers__tabs">
            <n-tab-pane v-for="section in sections" :key="section.uid" :name="String(section.uid)">
              <template #tab>
                <span class="papers__tab">
                  {{ partName(section.type) }}
                  <n-tag v-if="errorCountOf(section.uid)" size="tiny" type="error" :bordered="false">
                    {{ errorCountOf(section.uid) }}
                  </n-tag>
                </span>
              </template>

              <div class="papers__section-head">
                <div>
                  <label class="papers__label">部分号</label>
                  <n-input-number v-model:value="section.part" size="small" :min="1" style="width: 96px" />
                </div>
                <div>
                  <label class="papers__label">题型</label>
                  <n-select
                    v-model:value="section.type"
                    size="small"
                    :options="typeOptions"
                    style="width: 140px"
                    @update:value="onTypeChange(section)"
                  />
                </div>
                <div>
                  <label class="papers__label">本部分分值</label>
                  <n-input-number v-model:value="section.score" size="small" :min="0.5" :step="0.5" style="width: 110px" />
                </div>
                <div v-if="section.type !== 'writing'" class="papers__section-count">
                  {{ sectionRows(section).length }} 题 · 答题卡
                  {{ cardRange(section).from }}~{{ cardRange(section).to }}
                </div>
                <n-popconfirm @positive-click="removeSection(section)">
                  <template #trigger>
                    <n-button size="tiny" text type="error">删除本部分</n-button>
                  </template>
                  删除后本部分题目与答案都会消失,确认?
                </n-popconfirm>
              </div>

              <!-- 写作:题目 + 要求 + 维度 + 范文 -->
              <template v-if="section.type === 'writing'">
                <label class="papers__label">写作题目 prompt</label>
                <n-input v-model:value="section.prompt" type="textarea" :autosize="{ minRows: 3 }" />
                <label class="papers__label">要点要求(每行一条)</label>
                <n-input v-model:value="section.requirementsText" type="textarea" :autosize="{ minRows: 2 }" />
                <div class="papers__row3">
                  <div>
                    <label class="papers__label">词数上限</label>
                    <n-input-number v-model:value="section.wordLimit" size="small" :min="0" style="width: 100%" />
                  </div>
                  <div class="papers__span2">
                    <label class="papers__label">评分维度(逗号分隔,固定 4 项)</label>
                    <n-input v-model:value="section.dimensionsText" size="small" />
                  </div>
                </div>
                <label class="papers__label">参考答案 sampleAnswer(下发时不判分,仅供复盘)</label>
                <n-input v-model:value="section.sampleAnswer" type="textarea" :autosize="{ minRows: 3 }" />
              </template>

              <!-- 客观题:文章(可多篇) + 每题一行(卷面与答案同排编辑) -->
              <template v-else>
                <div v-if="section.type === 'matching'" class="papers__pool">
                  <label class="papers__label">候选句子池(五选五:每个空选一个 key,句子文本在这里统一填)</label>
                  <div v-for="(opt, oi) in section.options" :key="`pool-${oi}`" class="papers__option">
                    <n-input v-model:value="opt.key" size="tiny" class="papers__option-key" placeholder="key" />
                    <n-input v-model:value="opt.text" size="tiny" placeholder="句子文本" />
                    <n-button size="tiny" text @click="section.options.splice(oi, 1)">×</n-button>
                  </div>
                  <n-button size="tiny" @click="section.options.push({ key: nextPoolKey(section), text: '' })">
                    加候选句
                  </n-button>
                </div>

                <div v-for="(group, gi) in section.groups" :key="group.uid" class="papers__group">
                  <div v-if="section.type === 'reading'" class="papers__group-head">
                    <span>第 {{ gi + 1 }} 篇</span>
                    <n-popconfirm v-if="section.groups.length > 1" @positive-click="section.groups.splice(gi, 1)">
                      <template #trigger><n-button size="tiny" text type="error">删除本篇</n-button></template>
                      删除本篇及其题目?
                    </n-popconfirm>
                  </div>
                  <label class="papers__label">
                    文章正文
                    <span class="papers__label-hint">
                      {{ section.type === 'reading' ? '(空位占位符不适用)' : '(空位用 [[1]] [[2]] … 标注本节题序)' }}
                    </span>
                  </label>
                  <n-input v-model:value="group.passage" type="textarea" :autosize="{ minRows: 4 }" />

                  <div v-for="row in group.rows" :key="row.uid" class="papers__item">
                    <div class="papers__item-head">
                      <n-tag size="tiny" :bordered="false">题序 {{ numberOf(row).local }}</n-tag>
                      <n-tag size="tiny" type="info" :bordered="false">卡号 {{ numberOf(row).card }}</n-tag>
                      <n-button
                        v-if="group.rows.length > 1 || section.type === 'reading'"
                        size="tiny"
                        text
                        type="error"
                        @click="removeRow(section, row)"
                      >
                        删除本题
                      </n-button>
                    </div>
                    <n-input
                      v-if="section.type === 'reading'"
                      v-model:value="row.stem"
                      size="small"
                      placeholder="题干(英文)"
                    />
                    <div v-if="hasOptions(section.type)" class="papers__options">
                      <div v-for="(opt, oi) in row.options" :key="`opt-${oi}`" class="papers__option">
                        <n-input v-model:value="opt.key" size="tiny" class="papers__option-key" placeholder="key" />
                        <n-input v-model:value="opt.text" size="tiny" placeholder="选项文本" />
                        <n-button size="tiny" text @click="row.options.splice(oi, 1)">×</n-button>
                      </div>
                      <n-button size="tiny" @click="row.options.push({ key: nextOptionKey(row), text: '' })">
                        加选项
                      </n-button>
                    </div>
                    <div class="papers__answer">
                      <n-select
                        v-if="section.type === 'matching'"
                        v-model:value="row.answer"
                        size="tiny"
                        placeholder="选项"
                        :options="poolKeyOptions(section)"
                        style="width: 104px"
                      />
                      <n-select
                        v-else-if="hasOptions(section.type)"
                        v-model:value="row.answer"
                        size="tiny"
                        placeholder="正确答案"
                        :options="optionKeyOptions(row)"
                        style="width: 104px"
                      />
                      <n-input
                        v-else
                        v-model:value="row.answer"
                        size="tiny"
                        placeholder="答案文本"
                        style="width: 168px"
                      />
                      <n-input
                        v-if="section.type === 'grammar-fill'"
                        v-model:value="row.hint"
                        size="tiny"
                        placeholder="提示词,如 (work)"
                        style="width: 140px"
                      />
                      <n-input v-model:value="row.explanation" size="tiny" placeholder="中文解析" class="papers__explain" />
                    </div>
                    <div v-if="section.type === 'grammar-fill'" class="papers__alt">
                      <label class="papers__label papers__label--inline">可接受变体(逗号分隔)</label>
                      <n-input v-model:value="row.variantsText" size="tiny" />
                    </div>
                  </div>
                </div>

                <div class="papers__group-actions">
                  <n-button size="tiny" @click="addRows(section, 1)">加一题</n-button>
                  <n-button v-if="section.type === 'reading'" size="tiny" @click="addPassage(section)">加一篇</n-button>
                </div>
              </template>
            </n-tab-pane>
          </n-tabs>

          <div class="papers__add">
            <n-button size="small" @click="addSection">新增部分</n-button>
            <n-collapse class="papers__preview">
              <n-collapse-item title="预览将要提交的 JSON(编号已自动推导)" name="preview">
                <n-code :code="previewText" language="json" :word-wrap="true" class="papers__code" />
              </n-collapse-item>
            </n-collapse>
          </div>

          <div class="papers__actions">
            <n-button size="small" @click="editorShow = false">取消</n-button>
            <n-button size="small" type="primary" :loading="saving" :disabled="errors.length > 0" @click="onSave">
              校验并保存
            </n-button>
          </div>
        </n-spin>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/exam-papers;POST/PUT 整卷;PATCH status;DELETE(引用保护)
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NAlert,
  NButton,
  NCollapse,
  NCollapseItem,
  NCode,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputNumber,
  NPagination,
  NPopconfirm,
  NSpace,
  NSpin,
  NSelect,
  NTabPane,
  NTabs,
  NTag,
  useMessage,
  type DataTableColumns,
  type SelectOption,
} from 'naive-ui'
import {
  createAdminExamPaper,
  deleteAdminExamPaper,
  getAdminExamPaper,
  getAdminExamPapers,
  setAdminExamPaperStatus,
  updateAdminExamPaper,
} from '@/api/admin'
import type { AdminExamPaper, ExamPaperPayload } from '@/types/admin'
import type { PracticeType } from '@/types/practice'

const message = useMessage()
const PAGE_SIZE = 20

const list = ref<AdminExamPaper[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const filterStatus = ref<number | null>(null)
const keyword = ref('')

const editorShow = ref(false)
const editorLoading = ref(false)
const saving = ref(false)
const form = reactive<{
  id: string | null
  title: string
  year: number
  totalScore: number
  duration: number
  status: number
}>({
  id: null,
  title: '',
  year: new Date().getFullYear(),
  totalScore: 100,
  duration: 120,
  status: 1,
})

/** 一个选项(卷面 options 里的一项;池子与单题共用) */
interface OptionRow {
  key: string
  text: string
}

/**
 * 一行题目 = 卷面项 + 答案项同排编辑。
 * 编号不存字段,由 `rowNumbers` 按 part 升序累加推导;库里原节点整份留着做 passthrough。
 */
interface ItemRow {
  uid: number
  originalContent: Record<string, unknown> | null
  originalKey: Record<string, unknown> | null
  stem: string
  options: OptionRow[]
  answer: string
  hint: string
  variantsText: string
  explanation: string
}

/** 一篇文章(阅读为多篇;其余题型固定一篇) */
interface GroupModel {
  uid: number
  original: Record<string, unknown> | null
  passage: string
  rows: ItemRow[]
}

/** 一个部分 */
interface SectionModel {
  uid: number
  original: Record<string, unknown> | null
  originalKey: Record<string, unknown> | null
  part: number
  type: PracticeType
  score: number
  groups: GroupModel[]
  /** 仅五选五:候选句子池 */
  options: OptionRow[]
  prompt: string
  requirementsText: string
  wordLimit: number
  dimensionsText: string
  sampleAnswer: string
}

const sections = ref<SectionModel[]>([])
const activeTab = ref('')
let uidSeq = 1
const nextUid = () => uidSeq++

/** 题库里默认给足的选项数(阅读/完形为 4 个) */
const DEFAULT_OPTION_KEYS = ['A', 'B', 'C', 'D']

/**
 * 表单建模的键:重建时这些键以表单为准(未填即删),其余原样带回。
 * 与后端 `ExamPaperValidator` 无关,只影响"能不能把库里结构拼回去"。
 */
const GROUP_MODELED = ['passage', 'questions', 'blanks']
const CONTENT_ITEM_MODELED = ['id', 'stem', 'options', 'hint']
const KEY_ITEM_MODELED = ['id', 'answer', 'explanation', 'acceptAlternatives']
const SECTION_CONTENT_MODELED = ['part', 'type', 'score', 'content']
const SECTION_KEY_MODELED = ['part', 'type', 'score', 'items', 'sampleAnswer']
const WRITING_MODELED = ['prompt', 'requirements', 'wordLimit', 'scoringDimensions']

const statusOptions = [
  { label: '已上架', value: 1 },
  { label: '已下架', value: 0 },
]

const editorStatusOptions = [
  { label: '上架(学生可考)', value: 1 },
  { label: '下架(仅后台可见)', value: 0 },
]

const typeOptions: SelectOption[] = [
  { label: '阅读理解', value: 'reading' },
  { label: '五选五', value: 'matching' },
  { label: '完形填空', value: 'cloze' },
  { label: '语法填空', value: 'grammar-fill' },
  { label: '作文', value: 'writing' },
]

function partName(type: PracticeType): string {
  return { reading: '阅读理解', matching: '五选五', cloze: '完形填空', 'grammar-fill': '语法填空', writing: '作文' }[type]
}

/** 带选项的题型(阅读题与完形空) */
function hasOptions(type: PracticeType): boolean {
  return type === 'reading' || type === 'cloze'
}

function asObject(value: unknown): Record<string, unknown> | null {
  return value && typeof value === 'object' && !Array.isArray(value) ? (value as Record<string, unknown>) : null
}

function asArray(value: unknown): Record<string, unknown>[] {
  return Array.isArray(value) ? (value as Record<string, unknown>[]) : []
}

/** 字符串数组(写作 requirements / scoringDimensions 这类);非数组一律当没有 */
function listOf(value: unknown): string[] {
  return Array.isArray(value) ? value.map((one) => String(one)) : []
}

function text(value: unknown): string {
  return value === null || value === undefined ? '' : String(value)
}

/** 逗号分隔的变体列表 <-> 输入框文本 */
function alternativesTextOf(value: unknown): string {
  return Array.isArray(value) ? value.map((one) => String(one)).join(',') : ''
}

function splitList(textValue: string): string[] {
  return textValue
    .split(/[,，\n]/)
    .map((one) => one.trim())
    .filter(Boolean)
}

function optionRowsOf(value: unknown): OptionRow[] {
  return asArray(value).map((opt) => ({ key: text(opt.key), text: text(opt.text) }))
}

function nextOptionKey(row: ItemRow): string {
  const used = row.options.map((opt) => opt.key.toUpperCase())
  for (const key of ['A', 'B', 'C', 'D', 'E', 'F', 'G']) {
    if (!used.includes(key)) return key
  }
  return String(row.options.length + 1)
}

function nextPoolKey(section: SectionModel): string {
  const used = section.options.map((opt) => opt.key.toUpperCase())
  for (const key of ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']) {
    if (!used.includes(key)) return key
  }
  return String(section.options.length + 1)
}

function emptyRow(): ItemRow {
  return {
    uid: nextUid(),
    originalContent: null,
    originalKey: null,
    stem: '',
    options: DEFAULT_OPTION_KEYS.map((key) => ({ key, text: '' })),
    answer: '',
    hint: '',
    variantsText: '',
    explanation: '',
  }
}

function makeRow(contentItem: Record<string, unknown> | null, keyItem: Record<string, unknown> | null): ItemRow {
  const options = optionRowsOf(contentItem?.options)
  return {
    uid: nextUid(),
    originalContent: contentItem,
    originalKey: keyItem,
    stem: text(contentItem?.stem),
    options: options.length ? options : DEFAULT_OPTION_KEYS.map((key) => ({ key, text: '' })),
    answer: text(keyItem?.answer),
    hint: text(contentItem?.hint),
    variantsText: alternativesTextOf(keyItem?.acceptAlternatives),
    explanation: text(keyItem?.explanation),
  }
}

function emptyGroup(): GroupModel {
  return { uid: nextUid(), original: null, passage: '', rows: [emptyRow()] }
}

/** 五选五默认候选句池(A~E 五句各用一次) */
function defaultPool(): OptionRow[] {
  return ['A', 'B', 'C', 'D', 'E'].map((key) => ({ key, text: '' }))
}

function emptySection(part: number, type: PracticeType = 'reading'): SectionModel {
  const writing = type === 'writing'
  return {
    uid: nextUid(),
    original: null,
    originalKey: null,
    part,
    type,
    score: writing ? 15 : 10,
    groups: writing ? [] : [emptyGroup()],
    options: type === 'matching' ? defaultPool() : [],
    prompt: '',
    requirementsText: '',
    wordLimit: 120,
    dimensionsText: '内容要点,语言表达,篇章结构,格式规范',
    sampleAnswer: '',
  }
}

/** 本部分的题目行(阅读为各篇合计) */
function sectionRows(section: SectionModel): ItemRow[] {
  return section.groups.flatMap((group) => group.rows)
}

function addRows(section: SectionModel, count: number): void {
  const target = section.groups[section.groups.length - 1] ?? emptyGroup()
  if (!section.groups.includes(target)) section.groups.push(target)
  for (let i = 0; i < count; i++) target.rows.push(emptyRow())
  if (section.type === 'matching' && !section.options.length) {
    section.options = defaultPool()
  }
}

function addPassage(section: SectionModel): void {
  section.groups.push({ uid: nextUid(), original: null, passage: '', rows: Array.from({ length: 5 }, emptyRow) })
}

function removeRow(section: SectionModel, row: ItemRow): void {
  for (const group of section.groups) {
    const index = group.rows.indexOf(row)
    if (index >= 0) {
      group.rows.splice(index, 1)
      break
    }
  }
  if (section.type !== 'reading' && !sectionRows(section).length) {
    addRows(section, 1)
  }
}

function addSection(): void {
  const part = sections.value.reduce((max, section) => Math.max(max, section.part), 0) + 1
  const section = emptySection(part)
  sections.value.push(section)
  activeTab.value = String(section.uid)
}

function removeSection(section: SectionModel): void {
  const index = sections.value.indexOf(section)
  if (index >= 0) sections.value.splice(index, 1)
  if (activeTab.value === String(section.uid)) {
    activeTab.value = sections.value[0] ? String(sections.value[0].uid) : ''
  }
}

/** 换题型时把行结构收敛到目标题型需要的形态 */
function onTypeChange(section: SectionModel): void {
  if (section.type === 'writing') {
    section.groups = []
    section.options = []
    return
  }
  if (!section.groups.length) section.groups = [emptyGroup()]
  if (section.type !== 'reading' && section.groups.length > 1) {
    section.groups = [section.groups[0]]
    message.warning('非阅读部分只保留第一篇的文章与题目')
  }
  if (section.type === 'matching' && !section.options.length) {
    section.options = defaultPool()
  }
  if (!hasOptions(section.type)) {
    for (const row of sectionRows(section)) row.options = []
  } else {
    for (const row of sectionRows(section)) {
      if (!row.options.length) row.options = DEFAULT_OPTION_KEYS.map((key) => ({ key, text: '' }))
    }
  }
}

/** part 升序(与后端 validateNumbering 同口径) */
const orderedSections = computed(() => [...sections.value].sort((a, b) => a.part - b.part))

/** 每题的本节题序与答题卡全局编号 */
const rowNumbers = computed(() => {
  const map = new Map<number, { local: number; card: number }>()
  let offset = 0
  for (const section of orderedSections.value) {
    if (section.type === 'writing') continue
    let local = 0
    for (const row of sectionRows(section)) {
      local += 1
      map.set(row.uid, { local, card: offset + local })
    }
    offset += local
  }
  return map
})

function numberOf(row: ItemRow): { local: number; card: number } {
  return rowNumbers.value.get(row.uid) ?? { local: 0, card: 0 }
}

const totalCardCount = computed(() =>
  [...rowNumbers.value.values()].reduce((max, n) => Math.max(max, n.card), 0)
)

function cardRange(section: SectionModel): { from: number; to: number } {
  const rows = sectionRows(section)
  if (!rows.length) return { from: 0, to: 0 }
  return { from: numberOf(rows[0]).card, to: numberOf(rows[rows.length - 1]).card }
}

function optionKeyOptions(row: ItemRow): SelectOption[] {
  return row.options.filter((opt) => opt.key).map((opt) => ({ label: opt.key, value: opt.key }))
}

function poolKeyOptions(section: SectionModel): SelectOption[] {
  return section.options.filter((opt) => opt.key).map((opt) => ({ label: `${opt.key} ${opt.text.slice(0, 12)}`, value: opt.key }))
}

/**
 * 按 part 升序累加题量,得到每个部分的起始偏移(答题卡编号 = 偏移 + 本节题序)。
 *
 * @returns uid -> 起始偏移与题量
 */
function offsetsBySection(): Map<number, { offset: number; count: number }> {
  const map = new Map<number, { offset: number; count: number }>()
  let offset = 0
  for (const section of orderedSections.value) {
    const count = section.type === 'writing' ? 0 : sectionRows(section).length
    map.set(section.uid, { offset, count })
    offset += count
  }
  return map
}

/**
 * 客户端镜像的服务端四条硬规则 + 几处会被静默判错的坑。
 *
 * <p>这里只是"保存前红绿灯",服务端 `ExamPaperValidator` 仍是唯一权威;
 * 编号在这里由表单构造保证,所以不再出现"卷面与答案编号不一致"那一类错误。</p>
 */
const checks = computed(() => {
  const errors: string[] = []
  const warnings: string[] = []
  const perSection = new Map<number, string[]>()
  const push = (uid: number, line: string) => {
    errors.push(line)
    if (uid) perSection.set(uid, [...(perSection.get(uid) ?? []), line])
  }

  if (!sections.value.length) {
    errors.push('至少需要一个部分')
  }
  const seenParts = new Set<number>()
  let writing = 0
  let sum = 0
  for (const section of sections.value) {
    sum += Number(section.score) || 0
    if (!Number.isInteger(section.part) || section.part < 1) {
      push(0, `部分号必须是正整数(当前 ${section.part})`)
    } else if (seenParts.has(section.part)) {
      push(section.uid, `第 ${section.part} 部分重复出现`)
    }
    seenParts.add(section.part)

    if (section.type === 'writing') {
      writing += 1
      if (Number(section.score) !== 15) {
        push(section.uid, `写作部分分值必须为 15(异步批改器按此封顶),当前 ${section.score}`)
      }
      if (!section.prompt.trim()) push(section.uid, `第 ${section.part} 部分缺少写作题目 prompt`)
      if (!splitList(section.dimensionsText).length) {
        push(section.uid, `第 ${section.part} 部分缺少评分维度 scoringDimensions`)
      }
      continue
    }

    const rows = sectionRows(section)
    if (!rows.length) {
      push(section.uid, `第 ${section.part} 部分没有题目`)
      continue
    }
    if (!Number(section.score)) push(section.uid, `第 ${section.part} 部分的分值必须为正数`)
    if (!section.groups.every((group) => group.passage.trim())) {
      push(section.uid, `第 ${section.part} 部分有文章正文为空`)
    }
    for (const row of rows) {
      const number = numberOf(row)
      const label = `第 ${section.part} 部分题序 ${number.local}(卡号 ${number.card})`
      if (!row.answer.trim()) push(section.uid, `${label} 的答案为空`)
      if (hasOptions(section.type)) {
        if (row.options.length < 2) push(section.uid, `${label} 的选项不足 2 个`)
        if (row.answer.trim() && !row.options.some((opt) => opt.key === row.answer)) {
          push(section.uid, `${label} 的正确答案 ${row.answer} 不在选项里,学生会答对却判错`)
        }
        if (section.type === 'reading' && !row.stem.trim()) push(section.uid, `${label} 缺少题干`)
        if (row.options.some((opt) => !opt.text.trim())) push(section.uid, `${label} 有空选项文本`)
      }
      if (section.type === 'matching') {
        if (row.answer.trim() && !section.options.some((opt) => opt.key === row.answer)) {
          push(section.uid, `${label} 选的 ${row.answer} 不在候选句子池里`)
        }
      }
      if (!row.explanation.trim()) warnings.push(`${label} 没有中文解析`)
    }
    if (section.type === 'matching') {
      const used = rows.map((row) => row.answer).filter(Boolean)
      if (new Set(used).size !== used.length) warnings.push(`第 ${section.part} 部分有候选句被重复使用(五选五应各用一次)`)
    }
    const placeholders = (section.groups[0]?.passage.match(/\[\[(\d+)\]\]/g) ?? []).length
    if (section.type !== 'reading' && placeholders && placeholders !== rows.length) {
      warnings.push(`第 ${section.part} 部分文章里有 ${placeholders} 个 [[n]] 占位,题目却有 ${rows.length} 道`)
    }
  }
  if (writing !== 1) errors.push(`整卷必须恰好包含一个写作部分,当前 ${writing} 个`)
  if (Math.abs(sum - Number(form.totalScore)) > 1e-9) {
    errors.push(`各部分分值之和 ${trim(sum)} 与卷面总分 ${trim(Number(form.totalScore))} 不一致`)
  }
  return { errors, warnings, perSection }
})

const errors = computed(() => checks.value.errors)
const warnings = computed(() => checks.value.warnings)

function errorCountOf(uid: number): number {
  return checks.value.perSection.get(uid)?.length ?? 0
}

function trim(value: number): string {
  return String(Number(value.toFixed(2)))
}

/**
 * 保留未建模字段的合并:建模键以表单为准(未产出即删除),其余键原样带回。
 *
 * @param original 库里原始节点
 * @param built 表单产出的键
 * @param modeled 由表单负责的键名
 */
function mergePreserve(
  original: Record<string, unknown> | null,
  built: Record<string, unknown>,
  modeled: string[]
): Record<string, unknown> {
  const out: Record<string, unknown> = { ...(original ?? {}) }
  for (const key of modeled) {
    if (!(key in built)) delete out[key]
  }
  return { ...out, ...built }
}

/** 由表单产出将提交的 content 与 answer_key(编号自动推导) */
function buildPayloads(): { content: Record<string, unknown>; answerKey: Record<string, unknown> } {
  const contentSections: Record<string, unknown>[] = []
  const keySections: Record<string, unknown>[] = []
  const offsets = offsetsBySection()

  for (const section of orderedSections.value) {
    const offset = offsets.get(section.uid)?.offset ?? 0

    if (section.type === 'writing') {
      contentSections.push(
        mergePreserve(section.original, {
          part: section.part,
          type: section.type,
          score: section.score,
          content: mergePreserve(asObject(section.original?.content), {
            prompt: section.prompt,
            requirements: section.requirementsText.split('\n').map((line) => line.trim()).filter(Boolean),
            wordLimit: section.wordLimit,
            scoringDimensions: splitList(section.dimensionsText),
          }, WRITING_MODELED),
        }, SECTION_CONTENT_MODELED)
      )
      keySections.push(
        mergePreserve(section.originalKey, {
          part: section.part,
          type: section.type,
          score: section.score,
          ...(section.sampleAnswer.trim() ? { sampleAnswer: section.sampleAnswer.trim() } : {}),
        }, SECTION_KEY_MODELED)
      )
      continue
    }

    // 题序:阅读跨篇连续(第 1 篇 1-5、第 2 篇 6-10…),其余题型就是行下标
    const items: Record<string, unknown>[] = []
    let local = 0
    if (section.type === 'reading') {
      const builtGroups = section.groups.map((group) => {
        const questions = group.rows.map((row) => {
          local += 1
          items.push(buildKeyItem(row, offset + local))
          return buildContentItem(row, section, local)
        })
        return mergePreserve(group.original, {
          ...(group.passage ? { passage: group.passage } : {}),
          questions,
        }, GROUP_MODELED)
      })
      contentSections.push(
        mergePreserve(section.original, {
          part: section.part,
          type: section.type,
          score: section.score,
          content: builtGroups,
        }, SECTION_CONTENT_MODELED)
      )
    } else {
      const group = section.groups[0] ?? emptyGroup()
      const contentItems = group.rows.map((row) => {
        local += 1
        items.push(buildKeyItem(row, offset + local))
        return buildContentItem(row, section, local)
      })
      contentSections.push(
        mergePreserve(section.original, {
          part: section.part,
          type: section.type,
          score: section.score,
          content: mergePreserve(group.original, {
            ...(group.passage ? { passage: group.passage } : {}),
            ...(section.type === 'matching'
              ? { options: section.options.filter((opt) => opt.key).map((opt) => ({ key: opt.key, text: opt.text })) }
              : {}),
            blanks: contentItems,
          }, GROUP_MODELED),
        }, SECTION_CONTENT_MODELED)
      )
    }

    keySections.push(
      mergePreserve(section.originalKey, {
        part: section.part,
        type: section.type,
        score: section.score,
        items,
      }, SECTION_KEY_MODELED)
    )
  }

  return { content: { sections: contentSections }, answerKey: { sections: keySections } }
}

/** 卷面单题 */
function buildContentItem(row: ItemRow, section: SectionModel, local: number): Record<string, unknown> {
  const built: Record<string, unknown> = { id: local }
  if (section.type === 'reading') {
    if (row.stem.trim()) built.stem = row.stem
    built.options = row.options.filter((opt) => opt.key).map((opt) => ({ key: opt.key, text: opt.text }))
  } else if (section.type === 'cloze') {
    built.options = row.options.filter((opt) => opt.key).map((opt) => ({ key: opt.key, text: opt.text }))
  } else if (section.type === 'grammar-fill' && row.hint.trim()) {
    built.hint = row.hint.trim()
  }
  return mergePreserve(row.originalContent, built, CONTENT_ITEM_MODELED)
}

/** 答案单题(全局编号) */
function buildKeyItem(row: ItemRow, card: number): Record<string, unknown> {
  const built: Record<string, unknown> = { id: card }
  if (row.answer.trim()) built.answer = row.answer.trim()
  if (row.explanation.trim()) built.explanation = row.explanation.trim()
  const variants = splitList(row.variantsText)
  if (variants.length) built.acceptAlternatives = variants
  return mergePreserve(row.originalKey, built, KEY_ITEM_MODELED)
}

/** 供"预览"折叠面板展示的实际提交体(构建失败时回显原因) */
const previewText = computed(() => {
  try {
    const built = buildPayloads()
    return JSON.stringify({ content: built.content, answerKey: built.answerKey }, null, 1)
  } catch (e) {
    return `构建失败:${(e as Error).message}`
  }
})

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getAdminExamPapers({
      status: filterStatus.value ?? undefined,
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      size: PAGE_SIZE,
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reload(): void {
  page.value = 1
  void fetchList()
}

function onPageChange(next: number): void {
  page.value = next
  void fetchList()
}

const columns: DataTableColumns<AdminExamPaper> = [
  { title: 'ID', key: 'id', width: 140, ellipsis: { tooltip: true } },
  { title: '年份', key: 'year', width: 80 },
  { title: '标题', key: 'title', minWidth: 240, ellipsis: { tooltip: true } },
  {
    title: '分值',
    key: 'totalScore',
    width: 160,
    render: (row) => {
      const mismatch = Number(row.sectionScoreSum) !== Number(row.totalScore)
      return h(
        NTag,
        { size: 'small', bordered: false, type: mismatch ? 'error' : 'success' },
        // 合计与总分不一致说明库里已有脏卷,保存时会被校验器拦住
        { default: () => `${row.totalScore}(部分合计 ${row.sectionScoreSum})` }
      )
    },
  },
  { title: '部分', key: 'sectionCount', width: 72 },
  { title: '客观题卡', key: 'objectiveItemCount', width: 96 },
  { title: '时长', key: 'duration', width: 84, render: (row) => `${row.duration ?? '-'} 分` },
  {
    title: '交卷',
    key: 'submissionCount',
    width: 80,
    render: (row) =>
      h(
        NTag,
        { size: 'small', bordered: false, type: row.submissionCount > 0 ? 'warning' : 'default' },
        { default: () => String(row.submissionCount) }
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 88,
    render: (row) =>
      h(
        NTag,
        { size: 'small', bordered: false, type: row.status === 1 ? 'info' : 'default' },
        { default: () => (row.status === 1 ? '上架' : '下架') }
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 230,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 6 }, {
        default: () => [
          h(
            NButton,
            { size: 'tiny', tertiary: true, type: 'primary', onClick: () => openEditor(row.id) },
            { default: () => '编辑' }
          ),
          h(
            NButton,
            { size: 'tiny', tertiary: true, onClick: () => onToggleStatus(row) },
            { default: () => (row.status === 1 ? '下架' : '上架') }
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => onDelete(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  { size: 'tiny', tertiary: true, type: 'error', disabled: row.submissionCount > 0 },
                  { default: () => '删除' }
                ),
              default: () =>
                row.submissionCount > 0
                  ? `已被 ${row.submissionCount} 次交卷引用,历史报告要用这张卷,请改用下架`
                  : '确认删除该试卷?',
            }
          ),
        ],
      }),
  },
]

/** 把库里两份 JSON 灌进表单(编号只读展示,编辑后由表单重新推导) */
function hydrate(content: Record<string, unknown> | null, answerKey: Record<string, unknown> | null): void {
  const keySections = new Map<number, Record<string, unknown>>()
  for (const raw of asArray(answerKey?.sections)) {
    keySections.set(Number(raw.part), raw)
  }
  const built: SectionModel[] = []
  for (const raw of asArray(content?.sections)) {
    const type = (text(raw.type) || 'reading') as PracticeType
    const keySection = keySections.get(Number(raw.part)) ?? null
    const section: SectionModel = {
      uid: nextUid(),
      original: raw,
      originalKey: keySection,
      part: Number(raw.part) || built.length + 1,
      type,
      score: Number(raw.score) || 0,
      groups: [],
      options: [],
      prompt: '',
      requirementsText: '',
      wordLimit: 120,
      dimensionsText: '内容要点,语言表达,篇章结构,格式规范',
      sampleAnswer: text(keySection?.sampleAnswer),
    }
    // 答案项与卷面项按"本节题序"逐一对齐(卷面存题序,答案存卡号)
    const items = asArray(keySection?.items)
    let cursor = 0
    const takeItem = (): Record<string, unknown> | null => items[cursor++] ?? null

    if (type === 'writing') {
      const body = asObject(raw.content) ?? {}
      section.prompt = text(body.prompt)
      section.requirementsText = listOf(body.requirements).join('\n')
      section.wordLimit = Number(body.wordLimit) || 120
      section.dimensionsText = listOf(body.scoringDimensions).join(',')
    } else if (type === 'reading') {
      for (const passage of asArray(raw.content)) {
        section.groups.push({
          uid: nextUid(),
          original: passage,
          passage: text(passage.passage),
          rows: asArray(passage.questions).map((item) => makeRow(item, takeItem())),
        })
      }
      if (!section.groups.length) section.groups = [emptyGroup()]
    } else {
      const body = asObject(raw.content) ?? {}
      if (type === 'matching') section.options = optionRowsOf(body.options)
      const rows = asArray(body.blanks).map((item) => makeRow(item, takeItem()))
      section.groups = [{ uid: nextUid(), original: body, passage: text(body.passage), rows: rows.length ? rows : [emptyRow()] }]
    }
    built.push(section)
  }
  sections.value = built.sort((a, b) => a.part - b.part)
  activeTab.value = sections.value[0] ? String(sections.value[0].uid) : ''
}

async function openEditor(id: string | null): Promise<void> {
  editorShow.value = true
  editorLoading.value = true
  try {
    if (id) {
      const detail = await getAdminExamPaper(id)
      form.id = detail.id
      form.title = detail.title
      form.year = detail.year
      form.totalScore = Number(detail.totalScore)
      form.duration = detail.duration ?? 120
      form.status = detail.status
      hydrate(asObject(detail.content), asObject(detail.answerKey))
    } else {
      form.id = null
      form.title = ''
      form.year = new Date().getFullYear()
      form.totalScore = 100
      form.duration = 120
      form.status = 1
      sections.value = [emptySection(1, 'reading'), emptySection(2, 'writing')]
      activeTab.value = String(sections.value[0].uid)
    }
  } finally {
    editorLoading.value = false
  }
}

async function onSave(): Promise<void> {
  if (errors.value.length) {
    message.error(`还有 ${errors.value.length} 项不通过,先处理红灯`)
    return
  }
  if (!form.title.trim()) {
    message.warning('标题不能为空')
    return
  }
  const built = buildPayloads()
  const payload: ExamPaperPayload = {
    year: form.year,
    title: form.title.trim(),
    totalScore: form.totalScore,
    duration: form.duration,
    status: form.status,
    content: built.content,
    answerKey: built.answerKey,
  }
  saving.value = true
  try {
    if (form.id) {
      await updateAdminExamPaper(form.id, payload)
      message.success('试卷已保存')
    } else {
      const created = await createAdminExamPaper(payload)
      message.success(`试卷已创建:${created.id}`)
    }
    editorShow.value = false
    void fetchList()
  } catch {
    /* 服务端校验失败原因(哪一部分编号不一致等)由 request 层弹出 */
  } finally {
    saving.value = false
  }
}

async function onToggleStatus(row: AdminExamPaper): Promise<void> {
  const next = row.status === 1 ? 0 : 1
  try {
    await setAdminExamPaperStatus(row.id, next)
    message.success(next === 1 ? '已上架' : '已下架(历史成绩报告仍可读)')
    void fetchList()
  } catch {
    /* 已提示 */
  }
}

async function onDelete(row: AdminExamPaper): Promise<void> {
  try {
    await deleteAdminExamPaper(row.id)
    message.success('已删除')
    void fetchList()
  } catch {
    /* 已提示 */
  }
}

onMounted(fetchList)
</script>

<style scoped>
.papers__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.papers__title {
  margin: 0;
  font-size: 20px;
}

.papers__filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.papers__notice {
  margin-bottom: 12px;
}

.papers__pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.papers__count {
  font-size: 12px;
  opacity: 0.7;
}

.papers__label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin: 12px 0 4px;
}

.papers__label--inline {
  display: inline;
  margin: 0 8px 0 0;
}

.papers__label-hint {
  opacity: 0.6;
}

.papers__row2 {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

.papers__row3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
}

.papers__span2 {
  grid-column: span 2;
}

.papers__checks {
  margin-top: 12px;
  padding: 8px 10px;
  border-radius: var(--lc-radius-md);
  background-color: var(--lc-bg-soft, rgba(0, 0, 0, 0.03));
}

.papers__check-list {
  margin: 6px 0 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--lc-danger, #d03050);
}

.papers__check-list--warn {
  color: var(--lc-warning, #f0a020);
}

.papers__tabs {
  margin-top: 12px;
}

.papers__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.papers__section-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--lc-border, rgba(0, 0, 0, 0.08));
}

.papers__section-count {
  align-self: center;
  font-size: 12px;
  opacity: 0.75;
  margin-left: auto;
}

.papers__group {
  margin-top: 10px;
}

.papers__group-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  opacity: 0.8;
  margin-top: 8px;
}

.papers__pool {
  margin-top: 8px;
}

.papers__item {
  margin-top: 10px;
  padding: 8px 10px;
  border: 1px solid var(--lc-border, rgba(0, 0, 0, 0.08));
  border-radius: var(--lc-radius-md);
}

.papers__item-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.papers__item-head .n-button {
  margin-left: auto;
}

.papers__options {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 6px;
}

.papers__option {
  display: flex;
  align-items: center;
  gap: 6px;
}

.papers__option-key {
  width: 56px;
  flex: none;
}

.papers__answer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}

.papers__explain {
  flex: 1;
  min-width: 160px;
}

.papers__alt {
  display: flex;
  align-items: center;
  margin-top: 6px;
}

.papers__alt .n-input {
  margin-left: 8px;
  max-width: 320px;
}

.papers__group-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.papers__add {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 14px;
}

.papers__preview {
  flex: 1;
  min-width: 0;
}

.papers__code {
  font-size: 11px;
  line-height: 1.6;
  max-height: 260px;
  overflow: auto;
}

.papers__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
