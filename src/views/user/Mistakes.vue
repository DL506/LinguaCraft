<!-- 错题本(任务书 10.15):来源筛选 + 题型筛选;桌面表格 / 手机卡片;查看解析、重做、删除 -->
<template>
  <div class="mistakes-page">
    <!-- 来源 + 题型筛选 -->
    <div class="mistakes-page__filters">
      <n-button
        v-for="chip in sourceChips"
        :key="chip.value"
        size="small"
        :type="source === chip.value ? 'primary' : 'default'"
        ghost
        @click="onSourceChange(chip.value)"
      >
        {{ chip.label }}
      </n-button>
      <span class="mistakes-page__divider" />
      <n-button
        v-for="chip in typeChips"
        :key="chip.value"
        size="small"
        :type="mistakeType === chip.value ? 'primary' : 'default'"
        ghost
        @click="onTypeChange(chip.value)"
      >
        {{ chip.label }}
      </n-button>
    </div>

    <!-- 桌面:表格 -->
    <n-data-table
      v-if="!isMobile"
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="false"
      :row-key="(row: MistakeItem) => row.id"
    />

    <!-- 手机:卡片流(10.15) -->
    <div v-else class="mistakes-page__cards">
      <div v-for="item in list" :key="item.id" class="mistake-card">
        <p class="mistake-card__title">{{ typeLabel(item.type) }} · {{ sourceLabel(item) }}</p>
        <p class="mistake-card__stem">{{ item.stem }}</p>
        <p class="mistake-card__answers">
          你的答案:{{ item.userAnswer || '未作答' }} ❌ 正确答案:{{ item.correctAnswer }} ✅
        </p>
        <div class="mistake-card__actions">
          <n-button size="small" @click="openExplanation(item)">查看解析</n-button>
          <n-button size="small" type="primary" secondary @click="redo(item)">重做</n-button>
          <n-button size="small" type="error" quaternary @click="remove(item)">删除</n-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="mistakes-page__pager">
      <n-pagination :page="page" :page-size="PAGE_SIZE" :item-count="total" @update:page="onPageChange" />
    </div>

    <!-- 解析弹窗 -->
    <n-modal v-model:show="explanationShow" preset="card" title="题目解析" class="mistake-modal">
      <div v-if="current" class="mistake-modal__body">
        <p class="mistake-modal__stem">{{ current.stem }}</p>
        <p class="mistake-modal__line">你的答案:{{ current.userAnswer || '未作答' }}</p>
        <p class="mistake-modal__line">正确答案:{{ current.correctAnswer }}</p>
        <p class="mistake-modal__explanation">{{ current.explanation }}</p>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /user/mistakes(来源/题型筛选+分页)、DELETE /user/mistakes/:id(12.7)
// 重做:跳题型页并带 query(任务书第 4 章重做链)
import { computed, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NDataTable, NModal, NPagination, useDialog } from 'naive-ui'
import { deleteMistake, getMistakes } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import type { PracticeType } from '@/types/practice'
import type { MistakeItem } from '@/types/user'

const router = useRouter()
const dialog = useDialog()
const { isMobile } = useDevice()

/** 每页条数 */
const PAGE_SIZE = 10

const sourceChips = [
  { label: '全部来源', value: '' },
  { label: '真题', value: 'real' },
  { label: '老师出题', value: 'teacher' },
  { label: 'AI出题', value: 'ai' },
] as const

const typeChips = [
  { label: '全部题型', value: '' },
  { label: '阅读理解', value: 'reading' },
  { label: '五选五', value: 'matching' },
  { label: '完形填空', value: 'cloze' },
  { label: '语法填空', value: 'grammar-fill' },
  { label: '作文', value: 'writing' },
] as const

const source = ref('')
const mistakeType = ref<'' | PracticeType>('')
const list = ref<MistakeItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

const current = ref<MistakeItem | null>(null)
const explanationShow = ref(false)

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

/** 来源文案 */
function sourceLabel(item: MistakeItem): string {
  if (item.source.type === 'teacher') return item.source.meta.teacherName ?? '老师出题'
  if (item.source.type === 'ai') return 'AI出题'
  return item.source.meta.year ? `${item.source.meta.year}年真题` : '真题'
}

/** 桌面表格列 */
const columns = computed(() => [
  { title: '题型', key: 'type', width: 100, render: (row: MistakeItem) => typeLabel(row.type) },
  { title: '来源', key: 'source', width: 110, render: (row: MistakeItem) => sourceLabel(row) },
  { title: '题干', key: 'stem', ellipsis: { tooltip: true } },
  { title: '你的答案', key: 'userAnswer', width: 100, render: (row: MistakeItem) => row.userAnswer || '未作答' },
  { title: '正确答案', key: 'correctAnswer', width: 100 },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row: MistakeItem) =>
      h('div', { style: 'display:flex;gap:8px' }, [
        h(NButton, { size: 'small', onClick: () => openExplanation(row) }, { default: () => '查看解析' }),
        h(NButton, { size: 'small', type: 'primary', secondary: true, onClick: () => redo(row) }, { default: () => '重做' }),
        h(NButton, { size: 'small', type: 'error', quaternary: true, onClick: () => remove(row) }, { default: () => '删除' }),
      ]),
  },
])

/** 拉取列表 */
async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getMistakes({
      source: (source.value || undefined) as 'real' | 'teacher' | 'ai' | undefined,
      type: (mistakeType.value || undefined) as PracticeType | undefined,
      page: page.value,
      size: PAGE_SIZE,
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onSourceChange(value: string): void {
  source.value = value
  page.value = 1
  fetchList()
}

function onTypeChange(value: '' | PracticeType): void {
  mistakeType.value = value
  page.value = 1
  fetchList()
}

function onPageChange(next: number): void {
  page.value = next
  fetchList()
}

/** 查看解析 */
function openExplanation(item: MistakeItem): void {
  current.value = item
  explanationShow.value = true
}

/** 重做:进入对应题型页(带 id 与来源标记,任务书第 4 章) */
function redo(item: MistakeItem): void {
  router.push(`/practice/${item.type}?id=${item.questionId}&from=mistakes`)
}

/** 删除(二次确认) */
function remove(item: MistakeItem): void {
  dialog.warning({
    title: '删除错题',
    content: '删除后将不再出现在错题本中,确认删除吗?',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteMistake(item.id)
      await fetchList()
    },
  })
}

onMounted(fetchList)
</script>

<style scoped>
.mistakes-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.mistakes-page__filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.mistakes-page__divider {
  width: 1px;
  height: 18px;
  background-color: var(--lc-border);
}

.mistakes-page__cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mistake-card {
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.mistake-card__title {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-bottom: 6px;
}

.mistake-card__stem {
  font-size: 14px;
  color: var(--lc-text-1);
  margin-bottom: 6px;
  overflow-wrap: break-word;
}

.mistake-card__answers {
  font-size: 13px;
  color: var(--lc-text-2);
  margin-bottom: 10px;
  overflow-wrap: break-word;
}

.mistake-card__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mistakes-page__pager {
  display: flex;
  justify-content: center;
}

.mistake-modal {
  width: min(560px, 92vw);
}

.mistake-modal__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-wrap: break-word;
}

.mistake-modal__stem {
  font-size: 14px;
  color: var(--lc-text-1);
}

.mistake-modal__line {
  font-size: 13px;
  color: var(--lc-text-2);
}

.mistake-modal__explanation {
  padding: 10px 12px;
  border-radius: var(--lc-radius);
  font-size: 13px;
  color: var(--lc-text-2);
  background-color: var(--lc-bg-hover);
}
</style>