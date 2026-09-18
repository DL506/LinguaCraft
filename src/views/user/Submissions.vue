<!-- 我的提交(任务书 10.15):状态筛选(全部/已批改/部分批改/待批改)+ 桌面表格 / 手机卡片 + 详情抽屉
     详情经 GET /user/submissions/:id;graded/partially 展示批改结果,pending 展示等待信息 -->
<template>
  <div class="submissions-page">
    <!-- 状态筛选(支持 ?status= 初始,首页「查看」跳转) -->
    <div class="submissions-page__filters">
      <n-button
        v-for="filter in filters"
        :key="filter.value"
        size="small"
        :type="status === filter.value ? 'primary' : 'default'"
        ghost
        @click="onFilterChange(filter.value)"
      >
        {{ filter.label }}
      </n-button>
    </div>

    <!-- 桌面:表格 -->
    <n-data-table
      v-if="!isMobile"
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="false"
      :row-key="(row: SubmissionListItem) => row.id"
    />

    <!-- 手机:卡片流 -->
    <div v-else class="submissions-page__cards">
      <div v-for="item in list" :key="item.id" class="submission-card">
        <p class="submission-card__title">{{ item.title }}</p>
        <p class="submission-card__meta">{{ typeLabel(item.type) }} · {{ formatDateTime(item.submittedAt) }}</p>
        <div class="submission-card__row">
          <n-tag size="small" :type="statusType(item.status)" :bordered="false">{{ statusLabel(item.status) }}</n-tag>
          <span v-if="item.score !== undefined" class="submission-card__score">得分 {{ item.score }}/{{ item.totalScore }}</span>
          <n-button size="small" type="primary" secondary @click="openDetail(item.id)">查看详情</n-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="submissions-page__pager">
      <n-pagination :page="page" :page-size="PAGE_SIZE" :item-count="total" @update:page="onPageChange" />
    </div>

    <!-- 详情抽屉 -->
    <n-drawer v-model:show="detailShow" :width="isMobile ? '100%' : 480" placement="right">
      <div v-if="detail" class="detail">
        <div class="detail__head">
          <n-tag :type="statusType(detail.status)" :bordered="false">{{ statusLabel(detail.status) }}</n-tag>
          <span v-if="detail.score !== undefined" class="detail__score">
            {{ detail.score }}/{{ detail.totalScore }} 分
          </span>
          <n-tag v-if="detail.gradingMethod" size="small" :bordered="false" type="info">
            {{ gradingLabel(detail.gradingMethod) }}
          </n-tag>
        </div>

        <p v-if="detail.status === 'pending'" class="detail__pending">
          {{ detail.estimatedTime ?? '预计 24 小时内出结果' }},请稍后再来查看。
        </p>

        <!-- 逐题明细(批改后下发) -->
        <table v-if="detail.details?.length" class="detail__table">
          <thead>
            <tr><th>题号</th><th>你的答案</th><th>正确答案</th><th>结果</th></tr>
          </thead>
          <tbody>
            <tr v-for="row in detail.details" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.userAnswer || '未作答' }}</td>
              <td>{{ row.correctAnswer }}</td>
              <td>{{ row.isCorrect ? '✅' : '❌' }}</td>
            </tr>
          </tbody>
        </table>

        <!-- 写作评分维度 / 范文 / 总评 -->
        <div v-if="detail.dimensions?.length" class="detail__block">
          <p class="detail__block-title">评分维度</p>
          <p v-for="dim in detail.dimensions" :key="dim.name" class="detail__dimension">
            {{ dim.name }}:{{ dim.score }}({{ dim.comment }})
          </p>
        </div>
        <div v-if="detail.sampleAnswer" class="detail__block">
          <p class="detail__block-title">范文</p>
          <p class="detail__pre">{{ detail.sampleAnswer }}</p>
        </div>
        <div v-if="detail.overallComment" class="detail__block">
          <p class="detail__block-title">总评</p>
          <p>{{ detail.overallComment }}</p>
        </div>

        <!-- 错题解析(批改后) -->
        <div v-if="detail.details?.some((d) => !d.isCorrect)" class="detail__block">
          <p class="detail__block-title">错题解析</p>
          <p v-for="row in detail.details?.filter((d) => !d.isCorrect)" :key="row.id" class="detail__explanation">
            {{ row.id }}.{{ row.explanation }}
          </p>
        </div>
      </div>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /user/submissions(状态筛选+分页)与 GET /user/submissions/:id(详情)
import { computed, h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NDataTable, NDrawer, NPagination, NTag, type DataTableColumns } from 'naive-ui'
import { getSubmissionDetail, getSubmissions } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { formatDateTime } from '@/utils/format'
import type { GradingMethod, GradingStatus, PracticeType, WritingSubmitResult } from '@/types/practice'
import type { SubmissionListItem } from '@/types/user'

const route = useRoute()
const router = useRouter()
const { isMobile } = useDevice()

/** 每页条数 */
const PAGE_SIZE = 10

const filters = [
  { label: '全部', value: 'all' },
  { label: '已批改', value: 'graded' },
  { label: '部分批改', value: 'partially_graded' },
  { label: '待批改', value: 'pending' },
] as const

const status = ref<'all' | GradingStatus>('all')
const list = ref<SubmissionListItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

/** 详情(WritingSubmitResult 兼容 SubmitResult,含写作扩展字段) */
const detail = ref<WritingSubmitResult | null>(null)
const detailShow = ref(false)

/** 题型文案 */
function typeLabel(type: PracticeType): string {
  const map: Record<PracticeType, string> = {
    reading: '阅读理解',
    matching: '五选五',
    cloze: '完形填空',
    'grammar-fill': '语法填空',
    writing: '作文',
  }
  return map[type]
}

/** 状态文案 */
function statusLabel(value: GradingStatus): string {
  const map: Record<GradingStatus, string> = {
    graded: '已批改',
    partially_graded: '部分批改',
    pending: '待批改',
  }
  return map[value]
}

/** 状态标签色 */
function statusType(value: GradingStatus): 'success' | 'warning' | 'default' {
  if (value === 'graded') return 'success'
  if (value === 'partially_graded') return 'warning'
  return 'default'
}

/** 批改方式文案 */
function gradingLabel(method: GradingMethod): string {
  const map: Record<GradingMethod, string> = { ai: 'AI 批改', auto: '客观题比对', manual: '人工批改' }
  return map[method]
}

/** 桌面表格列 */
const columns = computed<DataTableColumns<SubmissionListItem>>(() => [
  { title: '题型', key: 'type', width: 100, render: (row) => typeLabel(row.type) },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '来源', key: 'source', width: 110, render: (row) => sourceLabel(row) },
  { title: '状态', key: 'status', width: 100, render: (row) => h(NTag, { size: 'small', bordered: false, type: statusType(row.status) }, { default: () => statusLabel(row.status) }) },
  { title: '得分', key: 'score', width: 90, render: (row) => (row.score !== undefined ? `${row.score}/${row.totalScore}` : '-') },
  { title: '提交时间', key: 'submittedAt', width: 160, render: (row) => formatDateTime(row.submittedAt) },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => h(NButton, { size: 'small', type: 'primary', secondary: true, onClick: () => openDetail(row.id) }, { default: () => '查看详情' }),
  },
])

/** 来源文案 */
function sourceLabel(item: SubmissionListItem): string {
  if (item.source.type === 'teacher') return item.source.meta.teacherName ?? '老师出题'
  if (item.source.type === 'ai') return 'AI出题'
  return item.source.meta.year ? `${item.source.meta.year}年真题` : '真题'
}

/** 拉取列表 */
async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getSubmissions({ status: status.value, page: page.value, size: PAGE_SIZE })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/** 切换筛选(同步 URL query,刷新可保持) */
function onFilterChange(value: 'all' | GradingStatus): void {
  status.value = value
  page.value = 1
  router.replace({ query: value === 'all' ? {} : { status: value } })
  fetchList()
}

function onPageChange(next: number): void {
  page.value = next
  fetchList()
}

/** 查看详情 */
async function openDetail(id: string): Promise<void> {
  detail.value = (await getSubmissionDetail(id)) as WritingSubmitResult
  detailShow.value = true
}

onMounted(() => {
  // 初始状态来自 query(首页「查看」跳转带 status=pending)
  const queryStatus = String(route.query.status ?? 'all')
  status.value = (['all', 'graded', 'partially_graded', 'pending'] as const).includes(
    queryStatus as 'all' | GradingStatus
  )
    ? (queryStatus as 'all' | GradingStatus)
    : 'all'
  fetchList()
})
</script>

<style scoped>
.submissions-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.submissions-page__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.submissions-page__cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submission-card {
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.submission-card__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 4px;
  overflow-wrap: break-word;
}

.submission-card__meta {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-bottom: 8px;
}

.submission-card__row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.submission-card__score {
  font-size: 13px;
  color: var(--lc-text-2);
}

.submissions-page__pager {
  display: flex;
  justify-content: center;
}

/* 详情抽屉 */
.detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  overflow-wrap: break-word;
}

.detail__head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.detail__score {
  font-size: 18px;
  font-weight: 700;
  color: var(--lc-primary);
}

.detail__pending {
  font-size: 13px;
  color: var(--lc-text-2);
}

.detail__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.detail__table th,
.detail__table td {
  padding: 6px 8px;
  border-bottom: 1px solid var(--lc-border);
  text-align: left;
  color: var(--lc-text-2);
}

.detail__table th {
  color: var(--lc-text-3);
  font-weight: 500;
}

.detail__block-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 6px;
}

.detail__dimension,
.detail__explanation {
  font-size: 13px;
  color: var(--lc-text-2);
  margin-bottom: 4px;
}

.detail__pre {
  font-size: 13px;
  color: var(--lc-text-2);
  white-space: pre-wrap;
}
</style>