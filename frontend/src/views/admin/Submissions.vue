<!-- 后台批改工作台(阶段 A4):待批改队列 + 判分抽屉
     断链背景:写作题与 AI 批改失败的提交会停在 pending,定时任务只处理 auto 那批,此前无人可判 -->
<template>
  <div class="grading">
    <div class="grading__head">
      <h2 class="grading__title">批改工作台</h2>
      <n-space size="small">
        <n-button size="small" :type="quick === 'manual' ? 'primary' : 'default'" ghost @click="applyQuick('manual')">
          只看必须人工
        </n-button>
        <n-button size="small" :type="quick === 'all' ? 'primary' : 'default'" ghost @click="applyQuick('all')">
          全部提交
        </n-button>
      </n-space>
    </div>

    <n-tabs v-model:value="tab" type="segment" class="grading__tabs" @update:value="onTabChange">
      <n-tab-pane name="practice" tab="练习提交">
        <!-- 筛选:状态 / 题型 / 判分方式 / 关键词 -->
        <div class="grading__filters">
          <n-select v-model:value="filterStatus" size="small" :options="statusOptions" style="width: 128px" @update:value="reload" />
          <n-select v-model:value="filterType" size="small" :options="typeOptions" style="width: 132px" @update:value="reload" />
          <n-select v-model:value="filterMethod" size="small" :options="methodOptions" style="width: 148px" @update:value="reload" />
          <n-input
            v-model:value="keyword"
            size="small"
            placeholder="标题或题目 ID"
            clearable
            style="width: 220px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <n-button size="small" @click="reload">查询</n-button>
        </div>

        <n-alert type="info" :show-icon="false" class="grading__notice">
          判分方式 <b>auto</b> 的提交会在阈值后由定时任务自动比对出分,无需人工插手;<b>manual</b>(写作题、AI 批改失败件)
          必须在这里判完,否则学生会一直看到「待批改」。
        </n-alert>

        <n-data-table
          :columns="columns"
          :data="list"
          :loading="loading"
          :bordered="false"
          :scroll-x="1120"
          :row-key="(row: AdminSubmissionListItem) => row.id"
        />

        <div class="grading__pager">
          <n-pagination :page="page" :page-size="PAGE_SIZE" :item-count="total" @update:page="onPageChange" />
          <span class="grading__count">共 {{ total }} 条</span>
        </div>
      </n-tab-pane>

      <!-- 模考提交:客观分交卷即算,这里只补写作部分 -->
      <n-tab-pane name="exam" tab="模考提交">
        <div class="grading__filters">
          <n-select
            v-model:value="examFilterStatus"
            size="small"
            :options="statusOptions"
            style="width: 128px"
            @update:value="reloadExam"
          />
          <n-input
            v-model:value="examKeyword"
            size="small"
            placeholder="试卷 ID,如 exam-2026"
            clearable
            style="width: 220px"
            @keyup.enter="reloadExam"
            @clear="reloadExam"
          />
          <n-button size="small" @click="reloadExam">查询</n-button>
        </div>

        <n-alert type="info" :show-icon="false" class="grading__notice">
          模考的客观题得分在交卷时就算好了;这一栏只处理<b>写作部分</b>。AI 作文评分失败时提交会停在
          partially_graded 且写作永远不出分,在这里判完即流转为已批改。
        </n-alert>

        <n-data-table
          :columns="examColumns"
          :data="examList"
          :loading="examLoading"
          :bordered="false"
          :scroll-x="1100"
          :row-key="(row: AdminExamSubmissionListItem) => row.id"
        />

        <div class="grading__pager">
          <n-pagination :page="examPage" :page-size="PAGE_SIZE" :item-count="examTotal" @update:page="onExamPageChange" />
          <span class="grading__count">共 {{ examTotal }} 条</span>
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 判分抽屉 -->
    <n-drawer v-model:show="drawerShow" :width="isMobile ? '100%' : 640" placement="right">
      <n-drawer-content :title="detail ? `判分 · #${detail.id} ${detail.title}` : '判分'" closable :native-scrollbar="false">
        <n-spin :show="detailLoading">
          <div v-if="detail" class="grade">
            <div class="grade__meta">
              <n-tag size="small" :bordered="false" type="info">{{ typeLabel(detail.type) }}</n-tag>
              <span>{{ detail.nickname || '未知用户' }}</span>
              <span>等待 {{ formatWaiting(detail.waitingMinutes) }}</span>
              <span>卷面总分 {{ detail.totalScore }}</span>
            </div>

            <!-- 题目材料(只读) -->
            <n-card size="small" title="题目" class="grade__card">
              <p v-if="isWriting" class="grade__prompt">{{ promptText }}</p>
              <p v-else class="grade__passage">{{ passageText }}</p>
              <p v-if="detail.questionHasAnswer === false" class="grade__hint">
                本题库无参考答案,判定完全依赖你的批阅。
              </p>
            </n-card>

            <!-- 写作:四维给分 -->
            <n-card v-if="isWriting" size="small" title="评分" class="grade__card">
              <label class="grade__label">学生作文</label>
              <div class="grade__answer">{{ writingText || '(未作答)' }}</div>
              <div v-for="(row, index) in dimensionRows" :key="row.name" class="grade__dim">
                <span class="grade__dim-name">{{ row.name }}</span>
                <n-input-number v-model:value="row.score" size="small" :min="0" :max="dimensionCap" style="width: 108px" />
                <n-input v-model:value="row.comment" size="small" placeholder="该维度评语(可空)" />
                <n-button size="tiny" quaternary type="error" @click="dimensionRows.splice(index, 1)">删</n-button>
              </div>
              <div class="grade__sum">
                维度合计 <b>{{ dimensionTotal }}</b> / 满分 {{ detail.totalScore }}
                <span v-if="dimensionTotal > Number(detail.totalScore)" class="grade__over">超出卷面总分,不可提交</span>
              </div>
              <label class="grade__label">参考范文(可空)</label>
              <n-input v-model:value="sampleAnswer" type="textarea" :autosize="{ minRows: 2 }" />
              <label class="grade__label">总评</label>
              <n-input v-model:value="overallComment" type="textarea" :autosize="{ minRows: 2 }" />
              <label class="grade__label">得分覆盖(可空,留空按维度合计)</label>
              <n-input-number v-model:value="scoreOverride" size="small" :min="0" :max="Number(detail.totalScore)" style="width: 148px" />
            </n-card>

            <!-- 客观题:逐题判定 -->
            <n-card v-else size="small" title="逐题判定" class="grade__card">
              <p class="grade__hint">题号必须与学生作答一一对应(服务端会拒绝漏判或多判)。</p>
              <div v-for="row in itemRows" :key="row.id" class="grade__item">
                <div class="grade__item-head">
                  <n-tag size="small" :bordered="false">{{ row.id }}</n-tag>
                  <span class="grade__item-answer">学生作答:{{ row.userAnswer || '(未作答)' }}</span>
                  <n-checkbox v-model:checked="row.isCorrect">答对</n-checkbox>
                </div>
                <div class="grade__item-fields">
                  <n-input v-model:value="row.correctAnswer" size="small" placeholder="正确答案" style="width: 148px" />
                  <n-input v-model:value="row.explanation" size="small" placeholder="评语/解析(学生端逐题表会显示)" />
                </div>
              </div>
              <label class="grade__label">得分覆盖(可空,留空按单题分值 × 正确数)</label>
              <n-input-number v-model:value="scoreOverride" size="small" :min="0" :max="Number(detail.totalScore)" style="width: 148px" />
            </n-card>

            <div class="grade__actions">
              <n-button size="small" @click="drawerShow = false">取消</n-button>
              <n-button size="small" type="primary" :loading="submitting" @click="onSubmit">提交判分</n-button>
            </div>
          </div>
        </n-spin>
      </n-drawer-content>
    </n-drawer>

    <!-- 模考作文判分抽屉 -->
    <n-drawer v-model:show="examDrawerShow" :width="isMobile ? '100%' : 640" placement="right">
      <n-drawer-content
        :title="examDetail ? `模考作文判分 · #${examDetail.id}` : '模考作文判分'"
        closable
        :native-scrollbar="false"
      >
        <n-spin :show="examDetailLoading">
          <div v-if="examDetail" class="grade">
            <div class="grade__meta">
              <n-tag size="small" :bordered="false" type="info">{{ examDetail.paperTitle || examDetail.examId }}</n-tag>
              <span>{{ examDetail.nickname || '未知用户' }}</span>
              <span>客观 {{ examDetail.objectiveScore }} 分</span>
              <span>写作满分 {{ examDetail.writingFullScore }}</span>
              <span>等待 {{ formatWaiting(examDetail.waitingMinutes) }}</span>
            </div>

            <n-card size="small" title="写作题目" class="grade__card">
              <p class="grade__prompt">{{ writingPromptText }}</p>
              <p v-if="writingRequirements.length" class="grade__hint">要点:{{ writingRequirements.join(' / ') }}</p>
              <p class="grade__hint">字数要求:{{ writingWordLimit }} 词</p>
            </n-card>

            <n-card size="small" title="学生作文与评分" class="grade__card">
              <div class="grade__answer">{{ examDetail.writingContent || '(未作答)' }}</div>
              <div v-for="(row, index) in examDimensionRows" :key="row.name" class="grade__dim">
                <span class="grade__dim-name">{{ row.name }}</span>
                <n-input-number v-model:value="row.score" size="small" :min="0" :max="100" style="width: 108px" />
                <n-input v-model:value="row.comment" size="small" placeholder="该维度评语(可空)" />
                <n-button size="tiny" quaternary type="error" @click="examDimensionRows.splice(index, 1)">删</n-button>
              </div>
              <n-button size="tiny" dashed @click="addExamDimension">+ 加一个维度</n-button>
              <div class="grade__sum">
                维度合计 <b>{{ examDimensionTotal }}</b> / 写作满分 {{ examDetail.writingFullScore }}
                <span v-if="examDimensionTotal > Number(examDetail.writingFullScore)" class="grade__over">
                  超出写作满分,不可提交
                </span>
              </div>
              <label class="grade__label">参考范文(可空)</label>
              <n-input v-model:value="examSampleAnswer" type="textarea" :autosize="{ minRows: 2 }" />
              <label class="grade__label">总评</label>
              <n-input v-model:value="examOverallComment" type="textarea" :autosize="{ minRows: 2 }" />
              <label class="grade__label">写作得分覆盖(可空,留空按维度合计)</label>
              <n-input-number
                v-model:value="examScoreOverride"
                size="small"
                :min="0"
                :max="Number(examDetail.writingFullScore)"
                style="width: 148px"
              />
            </n-card>

            <div class="grade__actions">
              <n-button size="small" @click="examDrawerShow = false">取消</n-button>
              <n-button size="small" type="primary" :loading="examSubmitting" @click="onExamSubmit">提交判分</n-button>
            </div>
          </div>
        </n-spin>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/submissions(队列);GET /api/admin/submissions/{id}(材料);POST .../{id}/grade(落库)
import { computed, h, onMounted, ref } from 'vue'
import {
  NAlert,
  NButton,
  NCard,
  NCheckbox,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputNumber,
  NPagination,
  NSpace,
  NSpin,
  NSelect,
  NTabPane,
  NTabs,
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import {
  getAdminExamSubmission,
  getAdminExamSubmissions,
  getAdminSubmission,
  getAdminSubmissions,
  gradeAdminExamWriting,
  gradeAdminSubmission,
} from '@/api/admin'
import type {
  AdminExamSubmissionDetail,
  AdminExamSubmissionListItem,
  AdminSubmissionDetail,
  AdminSubmissionListItem,
  ManualGradeDimension,
  ManualGradeItem,
} from '@/types/admin'
import type { PracticeType } from '@/types/practice'

const message = useMessage()

/** 每页条数 */
const PAGE_SIZE = 20
/** 单维度输入上限(避免误填超大值;总分仍由服务端按卷面总分校验) */
const dimensionCap = 100

const list = ref<AdminSubmissionListItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

const filterStatus = ref<string | null>('pending')
const filterType = ref<PracticeType | null>(null)
const filterMethod = ref<string | null>(null)
const keyword = ref('')
const quick = ref<'manual' | 'all'>('manual')

const drawerShow = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const detail = ref<AdminSubmissionDetail | null>(null)

/** 判分行模型 */
interface ItemRow {
  id: number
  userAnswer: string
  correctAnswer: string
  isCorrect: boolean
  explanation: string
}

const itemRows = ref<ItemRow[]>([])
const dimensionRows = ref<{ name: string; score: number | null; comment: string }[]>([])
const sampleAnswer = ref('')
const overallComment = ref('')
const scoreOverride = ref<number | null>(null)

/** 窄屏判定(后台以桌面为主,抽屉在手机上占满全屏) */
const isMobile = computed(() => window.innerWidth < 768)

const statusOptions = [
  { label: '待批改', value: 'pending' },
  { label: '已批改', value: 'graded' },
  { label: '部分批改', value: 'partially_graded' },
  { label: '全部状态', value: '' },
]

const typeOptions = [
  { label: '全部题型', value: '' },
  { label: '阅读理解', value: 'reading' },
  { label: '五选五', value: 'matching' },
  { label: '完形填空', value: 'cloze' },
  { label: '语法填空', value: 'grammar-fill' },
  { label: '作文', value: 'writing' },
]

const methodOptions = [
  { label: '全部判分方式', value: '' },
  { label: 'manual(须人工)', value: 'manual' },
  { label: 'auto(定时自动)', value: 'auto' },
  { label: 'ai(已出分)', value: 'ai' },
]

const TYPE_LABELS: Record<string, string> = {
  reading: '阅读理解',
  matching: '五选五',
  cloze: '完形填空',
  'grammar-fill': '语法填空',
  writing: '作文',
}

const METHOD_LABELS: Record<string, string> = {
  ai: 'AI',
  auto: '自动',
  manual: '人工',
}

function typeLabel(type: string): string {
  return TYPE_LABELS[type] ?? type
}

/** 等待时长人话化 */
function formatWaiting(minutes?: number): string {
  if (minutes === undefined || minutes === null) return '-'
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时 ${minutes % 60} 分`
  return `${Math.floor(hours / 24)} 天 ${hours % 24} 小时`
}

const columns: DataTableColumns<AdminSubmissionListItem> = [
  { title: '提交', key: 'id', width: 76 },
  { title: '学生', key: 'nickname', width: 110, render: (row) => row.nickname || '-' },
  {
    title: '题型',
    key: 'type',
    width: 96,
    render: (row) => h(NTag, { size: 'small', bordered: false }, { default: () => typeLabel(row.type) }),
  },
  { title: '标题', key: 'title', minWidth: 200, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 92,
    render: (row) =>
      h(
        NTag,
        {
          size: 'small',
          bordered: false,
          type: row.status === 'pending' ? 'warning' : row.status === 'graded' ? 'success' : 'info',
        },
        { default: () => (row.status === 'pending' ? '待批改' : row.status === 'graded' ? '已批改' : '部分批改') }
      ),
  },
  {
    title: '判分方式',
    key: 'gradingMethod',
    width: 96,
    render: (row) => METHOD_LABELS[row.gradingMethod] ?? row.gradingMethod,
  },
  { title: '题数', key: 'answeredCount', width: 68 },
  {
    title: '得分',
    key: 'score',
    width: 108,
    render: (row) => (row.score === undefined || row.score === null ? '-' : `${row.score} / ${row.totalScore}`),
  },
  {
    title: '等待',
    key: 'waitingMinutes',
    width: 116,
    render: (row) => (row.status === 'pending' ? formatWaiting(row.waitingMinutes) : '-'),
  },
  {
    title: '提交时间',
    key: 'submittedAt',
    width: 160,
    render: (row) => row.submittedAt.replace('T', ' '),
  },
  {
    title: '操作',
    key: 'actions',
    width: 108,
    fixed: 'right',
    render: (row) =>
      h(
        NButton,
        { size: 'tiny', tertiary: true, type: 'primary', onClick: () => openDetail(row) },
        // 已批改的行也进抽屉,只是只读展示材料(判分按钮由服务端状态校验兜底)
        { default: () => (row.status === 'pending' ? '判分' : '查看') }
      ),
  },
]

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getAdminSubmissions({
      status: filterStatus.value || undefined,
      type: filterType.value || undefined,
      method: filterMethod.value || undefined,
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

/** 快捷筛选:只看必须人工处理的断链件 */
function applyQuick(mode: 'manual' | 'all'): void {
  quick.value = mode
  filterStatus.value = mode === 'manual' ? 'pending' : ''
  filterMethod.value = mode === 'manual' ? 'manual' : ''
  reload()
}

/** 从 answer_key 里取该题号的参考答案(预填判分页,减少手工输入) */
function referenceAnswer(answerKey: Record<string, unknown> | undefined, id: number): string {
  if (!answerKey) return ''
  const items = (answerKey.questions ?? answerKey.blanks) as { id: number; answer?: string }[] | undefined
  const matched = items?.find((entry) => Number(entry.id) === id)
  return matched?.answer ?? ''
}

async function openDetail(row: AdminSubmissionListItem): Promise<void> {
  drawerShow.value = true
  detailLoading.value = true
  detail.value = null
  scoreOverride.value = null
  sampleAnswer.value = ''
  overallComment.value = ''
  itemRows.value = []
  dimensionRows.value = []
  try {
    const data = await getAdminSubmission(row.id)
    detail.value = data
    if (data.type === 'writing') {
      const dimensions = (data.questionContent?.scoringDimensions as string[]) ?? []
      dimensionRows.value = dimensions.map((name) => ({ name, score: null, comment: '' }))
      overallComment.value = data.writingResult
        ? String((data.writingResult as Record<string, unknown>).overallComment ?? '')
        : ''
      sampleAnswer.value = data.writingResult
        ? String((data.writingResult as Record<string, unknown>).sampleAnswer ?? '')
        : ''
    } else {
      const answers = data.answers ?? {}
      itemRows.value = Object.keys(answers)
        .filter((key) => /^\d+$/.test(key))
        .map(Number)
        .sort((a, b) => a - b)
        .map((id) => ({
          id,
          userAnswer: String(answers[String(id)] ?? ''),
          correctAnswer: referenceAnswer(data.answerKey, id),
          isCorrect: false,
          explanation: '',
        }))
    }
  } finally {
    detailLoading.value = false
  }
}

const isWriting = computed(() => detail.value?.type === 'writing')

const promptText = computed(() => String(detail.value?.questionContent?.prompt ?? ''))
const passageText = computed(() => String(detail.value?.questionContent?.passage ?? ''))
const writingText = computed(() => String(detail.value?.answers?.content ?? ''))

const dimensionTotal = computed(() =>
  Number(
    dimensionRows.value
      .reduce((sum, row) => sum + Number(row.score ?? 0), 0)
      .toFixed(1)
  )
)

async function onSubmit(): Promise<void> {
  const current = detail.value
  if (!current) return

  const items: ManualGradeItem[] = itemRows.value.map((row) => ({
    id: row.id,
    correctAnswer: row.correctAnswer.trim(),
    isCorrect: row.isCorrect,
    explanation: row.explanation.trim(),
  }))
  const dimensions: ManualGradeDimension[] = dimensionRows.value.map((row) => ({
    name: row.name,
    score: Number(row.score ?? 0),
    comment: row.comment.trim(),
  }))

  if (isWriting.value) {
    if (dimensions.length === 0) {
      message.warning('缺少评分维度,无法判分')
      return
    }
    if (dimensions.some((row) => !Number.isFinite(row.score))) {
      message.warning('每个维度都要填分数')
      return
    }
  } else if (items.length === 0) {
    message.warning('该提交没有可判分的作答')
    return
  }

  submitting.value = true
  try {
    await gradeAdminSubmission(current.id, {
      items: isWriting.value ? undefined : items,
      dimensions: isWriting.value ? dimensions : undefined,
      sampleAnswer: isWriting.value ? sampleAnswer.value.trim() || undefined : undefined,
      overallComment: isWriting.value ? overallComment.value.trim() || undefined : undefined,
      score: scoreOverride.value ?? undefined,
    })
    message.success('已判分,学生端立刻可见结果')
    drawerShow.value = false
    void fetchList()
  } catch {
    /* 服务端拒绝原因(状态已变、题号不齐、超分)由 request 层弹出 */
  } finally {
    submitting.value = false
  }
}

// ==================== 模考提交页签(阶段 A4-②)====================
const tab = ref<'practice' | 'exam'>('practice')

const examList = ref<AdminExamSubmissionListItem[]>([])
const examTotal = ref(0)
const examPage = ref(1)
const examLoading = ref(false)
/** 默认落在"客观分已出、作文欠着"这批 —— 它们正是 AI 批改失败后的死档 */
const examFilterStatus = ref<string | null>('partially_graded')
const examKeyword = ref('')

const examDrawerShow = ref(false)
const examDetailLoading = ref(false)
const examSubmitting = ref(false)
const examDetail = ref<AdminExamSubmissionDetail | null>(null)
const examDimensionRows = ref<{ name: string; score: number | null; comment: string }[]>([])
const examSampleAnswer = ref('')
const examOverallComment = ref('')
const examScoreOverride = ref<number | null>(null)

const examColumns: DataTableColumns<AdminExamSubmissionListItem> = [
  { title: '提交', key: 'id', width: 76 },
  { title: '学生', key: 'nickname', width: 110, render: (row) => row.nickname || '-' },
  { title: '试卷', key: 'paperTitle', minWidth: 200, ellipsis: { tooltip: true } },
  {
    title: '作文',
    key: 'writingGraded',
    width: 104,
    render: (row) =>
      h(
        NTag,
        {
          size: 'small',
          bordered: false,
          type: row.writingGraded ? 'success' : row.hasWritingContent ? 'warning' : 'default',
        },
        { default: () => (row.writingGraded ? '已出分' : row.hasWritingContent ? '待判' : '未写') }
      ),
  },
  { title: '客观分', key: 'objectiveScore', width: 92, render: (row) => row.objectiveScore ?? '-' },
  { title: '写作分', key: 'writingScore', width: 88, render: (row) => row.writingScore ?? '-' },
  { title: '卷面', key: 'totalScore', width: 80 },
  {
    title: '判分方式',
    key: 'gradingMethod',
    width: 96,
    render: (row) => METHOD_LABELS[row.gradingMethod] ?? row.gradingMethod,
  },
  {
    title: '等待',
    key: 'waitingMinutes',
    width: 116,
    render: (row) => (row.writingGraded ? '-' : formatWaiting(row.waitingMinutes)),
  },
  { title: '交卷时间', key: 'submittedAt', width: 160, render: (row) => row.submittedAt.replace('T', ' ') },
  {
    title: '操作',
    key: 'actions',
    width: 108,
    fixed: 'right',
    render: (row) =>
      h(
        NButton,
        { size: 'tiny', tertiary: true, type: 'primary', onClick: () => openExamDetail(row) },
        { default: () => (row.writingGraded ? '查看' : '判作文') }
      ),
  },
]

async function fetchExamList(): Promise<void> {
  examLoading.value = true
  try {
    const res = await getAdminExamSubmissions({
      status: examFilterStatus.value || undefined,
      keyword: examKeyword.value.trim() || undefined,
      page: examPage.value,
      size: PAGE_SIZE,
    })
    examList.value = res.list
    examTotal.value = res.total
  } finally {
    examLoading.value = false
  }
}

function reloadExam(): void {
  examPage.value = 1
  void fetchExamList()
}

function onExamPageChange(next: number): void {
  examPage.value = next
  void fetchExamList()
}

/** 切页签时懒加载模考队列(避免进页面就打两次请求) */
function onTabChange(value: string | number): void {
  if (value === 'exam' && examList.value.length === 0) void fetchExamList()
}

async function openExamDetail(row: AdminExamSubmissionListItem): Promise<void> {
  examDrawerShow.value = true
  examDetailLoading.value = true
  examDetail.value = null
  examDimensionRows.value = []
  examSampleAnswer.value = ''
  examOverallComment.value = ''
  examScoreOverride.value = null
  try {
    const data = await getAdminExamSubmission(row.id)
    examDetail.value = data
    const names = ((data.writingSection?.scoringDimensions as string[]) ?? [])
    // 已判过的行回显既有结果,便于复核
    const existing = (data.writingResult?.dimensions as { name: string; score: number; comment: string }[]) ?? []
    examDimensionRows.value = (existing.length ? existing.map((d) => d.name) : names).map((name) => {
      const hit = existing.find((d) => d.name === name)
      return { name, score: hit ? Number(hit.score) : null, comment: hit ? hit.comment : '' }
    })
    examSampleAnswer.value = String(data.writingResult?.sampleAnswer ?? '')
    examOverallComment.value = String(data.writingResult?.overallComment ?? '')
  } finally {
    examDetailLoading.value = false
  }
}

function addExamDimension(): void {
  examDimensionRows.value.push({ name: '新维度', score: null, comment: '' })
}

const writingPromptText = computed(() => String(examDetail.value?.writingSection?.prompt ?? ''))
const writingRequirements = computed(() => (examDetail.value?.writingSection?.requirements as string[]) ?? [])
const writingWordLimit = computed(() => examDetail.value?.writingSection?.wordLimit ?? 120)

const examDimensionTotal = computed(() =>
  Number(examDimensionRows.value.reduce((sum, row) => sum + Number(row.score ?? 0), 0).toFixed(1))
)

async function onExamSubmit(): Promise<void> {
  const current = examDetail.value
  if (!current) return
  const dimensions = examDimensionRows.value.map((row) => ({
    name: row.name.trim(),
    score: Number(row.score ?? 0),
    comment: row.comment.trim(),
  }))
  if (dimensions.length === 0) {
    message.warning('至少需要一个评分维度')
    return
  }
  if (dimensions.some((row) => !row.name || !Number.isFinite(row.score))) {
    message.warning('每个维度都要有名称和分数')
    return
  }

  examSubmitting.value = true
  try {
    await gradeAdminExamWriting(current.id, {
      dimensions,
      sampleAnswer: examSampleAnswer.value.trim() || undefined,
      overallComment: examOverallComment.value.trim() || undefined,
      score: examScoreOverride.value ?? undefined,
    })
    message.success('作文已判分,学生成绩报告立刻可见')
    examDrawerShow.value = false
    void fetchExamList()
  } catch {
    /* 服务端拒绝原因(已出分、无正文、超满分、被抢先)由 request 层弹出 */
  } finally {
    examSubmitting.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.grading__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.grading__title {
  margin: 0;
  font-size: 20px;
}

.grading__tabs {
  margin-top: 4px;
}

.grading__filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.grading__notice {
  margin-bottom: 12px;
}

.grading__pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.grading__count {
  font-size: 12px;
  opacity: 0.7;
}

.grade__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  margin-bottom: 12px;
}

.grade__card {
  margin-bottom: 12px;
}

.grade__label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin: 12px 0 4px;
}

.grade__prompt,
.grade__passage,
.grade__answer {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.grade__answer {
  max-height: 220px;
  overflow: auto;
  border: 1px solid var(--lc-border, rgba(128, 128, 128, 0.24));
  border-radius: 6px;
  padding: 8px;
}

.grade__hint {
  margin: 8px 0 0;
  font-size: 12px;
  opacity: 0.75;
}

.grade__item {
  border: 1px solid var(--lc-border, rgba(128, 128, 128, 0.24));
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 8px;
}

.grade__item-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.grade__item-answer {
  flex: 1;
  font-size: 13px;
}

.grade__item-fields {
  display: flex;
  gap: 8px;
}

.grade__dim {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.grade__dim-name {
  width: 88px;
  font-size: 13px;
}

.grade__sum {
  font-size: 13px;
  margin-top: 8px;
}

.grade__over {
  color: var(--lc-error, #d03050);
  margin-left: 8px;
}

.grade__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
