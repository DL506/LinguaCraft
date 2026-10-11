<!-- 后台真题卷管理(阶段 A6-③):列表 + 整卷 JSON 编辑 + 上下架 + 引用保护删除
     编辑面用 JSON 面板(与题目录入 A2 一致):整卷是嵌套结构,做成表单会退化成"表单拼不回原样"的老问题 -->
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
      模考客观题判分<b>按答题卡全局编号配对</b>:卷面题号与答案题号一旦错位,交卷时不报错、只是分数错。
      保存前服务端会硬校验(部分号唯一、各部分分值之和等于卷面总分、每部分答案编号与卷面一致、写作部分恰好一个且 15 分)。
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

    <n-drawer v-model:show="editorShow" :width="860" placement="right">
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
          <div class="papers__row2">
            <div>
              <label class="papers__label">卷面总分</label>
              <n-input-number v-model:value="form.totalScore" size="small" :min="1" style="width: 100%" />
            </div>
            <div>
              <label class="papers__label">时长(分钟)</label>
              <n-input-number v-model:value="form.duration" size="small" :min="1" style="width: 100%" />
            </div>
          </div>
          <label class="papers__label">上下架</label>
          <n-select v-model:value="form.status" size="small" :options="editorStatusOptions" style="width: 220px" />

          <label class="papers__label">content(卷面 JSON:{ "sections":[{part,type,score,content}] })</label>
          <n-input v-model:value="contentText" type="textarea" :autosize="{ minRows: 10 }" class="papers__json" />
          <label class="papers__label">answer_key(答案 JSON:{ "sections":[{part,type,score,items}] })</label>
          <n-input v-model:value="answerKeyText" type="textarea" :autosize="{ minRows: 8 }" class="papers__json" />

          <p class="papers__hint">
            阅读部分的 content 是<b>数组</b>(多篇文章,题号跨篇连续编号);其余题型是对象,带 blanks。
            写作部分只放 prompt/scoringDimensions,答案侧给 sampleAnswer。
          </p>

          <div class="papers__actions">
            <n-button size="small" @click="editorShow = false">取消</n-button>
            <n-button size="small" type="primary" :loading="saving" @click="onSave">校验并保存</n-button>
          </div>
        </n-spin>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/exam-papers;POST/PUT 整卷;PATCH status;DELETE(引用保护)
import { h, onMounted, reactive, ref } from 'vue'
import {
  NAlert,
  NButton,
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
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import {
  createAdminExamPaper,
  deleteAdminExamPaper,
  getAdminExamPaper,
  getAdminExamPapers,
  setAdminExamPaperStatus,
  updateAdminExamPaper,
} from '@/api/admin'
import type { AdminExamPaper } from '@/types/admin'

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
const form = reactive<{ id: string | null; title: string; year: number; totalScore: number; duration: number; status: number }>({
  id: null,
  title: '',
  year: new Date().getFullYear(),
  totalScore: 100,
  duration: 120,
  status: 1,
})
const contentText = ref('{}')
const answerKeyText = ref('{}')

const statusOptions = [
  { label: '已上架', value: 1 },
  { label: '已下架', value: 0 },
]

const editorStatusOptions = [
  { label: '上架(学生可考)', value: 1 },
  { label: '下架(仅后台可见)', value: 0 },
]

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
      contentText.value = JSON.stringify(detail.content ?? {}, null, 2)
      answerKeyText.value = JSON.stringify(detail.answerKey ?? {}, null, 2)
    } else {
      form.id = null
      form.title = ''
      form.year = new Date().getFullYear()
      form.totalScore = 100
      form.duration = 120
      form.status = 1
      contentText.value = '{\n  "sections": []\n}'
      answerKeyText.value = '{\n  "sections": []\n}'
    }
  } finally {
    editorLoading.value = false
  }
}

async function onSave(): Promise<void> {
  let content: unknown
  let answerKey: unknown
  try {
    content = JSON.parse(contentText.value)
    answerKey = JSON.parse(answerKeyText.value)
  } catch {
    message.error('两份 JSON 存在语法错误,请先修正')
    return
  }
  if (!form.title.trim()) {
    message.warning('标题不能为空')
    return
  }
  const payload = {
    year: form.year,
    title: form.title.trim(),
    totalScore: form.totalScore,
    duration: form.duration,
    status: form.status,
    content,
    answerKey,
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

.papers__row2 {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

.papers__json {
  font-family: ui-monospace, Consolas, monospace;
}

.papers__json :deep(textarea) {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
}

.papers__hint {
  margin: 10px 0 0;
  font-size: 12px;
  opacity: 0.75;
  line-height: 1.7;
}

.papers__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
