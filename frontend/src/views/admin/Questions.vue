<!-- 后台题库管理(阶段 A2):筛选 + 分页表格 + 上下架/删除;编辑跳转独立页 -->
<template>
  <div class="admin-questions">
    <div class="admin-questions__head">
      <h2 class="admin-questions__title">题库管理</h2>
      <n-button type="primary" size="small" @click="router.push('/admin/questions/new')">新建题目</n-button>
    </div>

    <!-- 筛选条:题型 / 来源 / 状态 / 关键词 -->
    <div class="admin-questions__filters">
      <n-select
        v-model:value="filterType"
        size="small"
        :options="typeOptions"
        style="width: 132px"
        @update:value="reload"
      />
      <n-select
        v-model:value="filterSource"
        size="small"
        :options="sourceOptions"
        style="width: 132px"
        @update:value="reload"
      />
      <n-select
        v-model:value="filterStatus"
        size="small"
        :options="statusOptions"
        style="width: 116px"
        @update:value="reload"
      />
      <n-input
        v-model:value="keyword"
        size="small"
        placeholder="标题或 ID 关键词"
        clearable
        style="width: 220px"
        @keyup.enter="reload"
        @clear="reload"
      />
      <n-button size="small" @click="reload">查询</n-button>
    </div>

    <!-- 表格(后台桌面优先;窄屏下表格自身横向滚动) -->
    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="false"
      :scroll-x="1180"
      :row-key="(row: AdminQuestionListItem) => row.id"
    />

    <div class="admin-questions__pager">
      <n-pagination
        :page="page"
        :page-size="PAGE_SIZE"
        :item-count="total"
        @update:page="onPageChange"
      />
      <span class="admin-questions__count">共 {{ total }} 题</span>
    </div>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/questions(列表);PATCH .../status(上下架);DELETE /{id}(引用保护删除)
import { h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NDataTable,
  NInput,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { deleteAdminQuestion, getAdminQuestions, setAdminQuestionStatus } from '@/api/admin'
import type { AdminQuestionListItem } from '@/types/admin'
import type { PracticeType } from '@/types/practice'

const router = useRouter()
const message = useMessage()

/** 每页条数(与后端 size 上限 100 内的合理值) */
const PAGE_SIZE = 20

const list = ref<AdminQuestionListItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

const filterType = ref<PracticeType | null>(null)
const filterSource = ref<string | null>(null)
const filterStatus = ref<number | null>(null)
const keyword = ref('')

/** 题型选项(与后端 PracticeRules 的五种一致) */
const typeOptions = [
  { label: '全部题型', value: '' },
  { label: '阅读理解', value: 'reading' },
  { label: '五选五', value: 'matching' },
  { label: '完形填空', value: 'cloze' },
  { label: '语法填空', value: 'grammar-fill' },
  { label: '作文', value: 'writing' },
]

const sourceOptions = [
  { label: '全部来源', value: '' },
  { label: '真题 real', value: 'real' },
  { label: '模拟题 teacher', value: 'teacher' },
  { label: 'AI 生成 ai', value: 'ai' },
]

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '已上架', value: 1 },
  { label: '已下架', value: 0 },
]

/** 题型中文名(表格展示) */
const TYPE_LABELS: Record<string, string> = {
  reading: '阅读理解',
  matching: '五选五',
  cloze: '完形填空',
  'grammar-fill': '语法填空',
  writing: '作文',
}

/** 表格列定义 */
const columns: DataTableColumns<AdminQuestionListItem> = [
  { title: '题目 ID', key: 'id', width: 200, ellipsis: { tooltip: true } },
  {
    title: '题型',
    key: 'type',
    width: 96,
    render: (row) => h(NTag, { size: 'small', bordered: false }, { default: () => TYPE_LABELS[row.type] ?? row.type }),
  },
  { title: '标题', key: 'title', minWidth: 220, ellipsis: { tooltip: true } },
  { title: '来源', key: 'sourceType', width: 88 },
  { title: '难度', key: 'difficulty', width: 64 },
  {
    title: '分值',
    key: 'totalScore',
    width: 116,
    render: (row) => `${row.totalScore} / 每题 ${row.perQuestionScore}`,
  },
  {
    title: '答案',
    key: 'hasAnswer',
    width: 92,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.hasAnswer ? 'success' : 'warning', bordered: false },
        { default: () => (row.hasAnswer ? '可比对' : 'AI/人工') }
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 84,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === 1 ? 'info' : 'default', bordered: false },
        { default: () => (row.status === 1 ? '上架' : '下架') }
      ),
  },
  { title: '引用', key: 'refSubmissionCount', width: 72 },
  {
    title: '更新时间',
    key: 'updatedAt',
    width: 160,
    render: (row) => (row.updatedAt ? row.updatedAt.replace('T', ' ') : '-'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 210,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 6 }, {
        default: () => [
          h(
            NButton,
            { size: 'tiny', tertiary: true, type: 'primary', onClick: () => router.push(`/admin/questions/edit/${row.id}`) },
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
                  {
                    size: 'tiny',
                    tertiary: true,
                    type: 'error',
                    // 有引用时直接禁用删除按钮,避免误点后才报错
                    disabled: row.refSubmissionCount > 0,
                  },
                  { default: () => '删除' }
                ),
              default: () =>
                row.refSubmissionCount > 0
                  ? `该题已被 ${row.refSubmissionCount} 次提交引用,请改用下架`
                  : '确认物理删除该题?',
            }
          ),
        ],
      }),
  },
]

/** 拉取列表 */
async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getAdminQuestions({
      type: filterType.value || undefined,
      sourceType: filterSource.value || undefined,
      status: filterStatus.value === null ? undefined : filterStatus.value,
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      size: PAGE_SIZE,
    })
    list.value = res.list
    total.value = res.total
  } catch {
    // 错误提示由 request 层统一弹出
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

/** 上下架切换 */
async function onToggleStatus(row: AdminQuestionListItem): Promise<void> {
  const next = row.status === 1 ? 0 : 1
  try {
    await setAdminQuestionStatus(row.id, next as 0 | 1)
    message.success(next === 1 ? '已上架' : '已下架(学生端列表与智能练习不再出现)')
    void fetchList()
  } catch {
    /* 已提示 */
  }
}

/** 删除 */
async function onDelete(row: AdminQuestionListItem): Promise<void> {
  try {
    await deleteAdminQuestion(row.id)
    message.success('已删除')
    void fetchList()
  } catch {
    /* 已提示 */
  }
}

onMounted(fetchList)
</script>

<style scoped>
.admin-questions__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.admin-questions__title {
  margin: 0;
  font-size: 20px;
}

.admin-questions__filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.admin-questions__pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.admin-questions__count {
  font-size: 12px;
  opacity: 0.7;
}
</style>
