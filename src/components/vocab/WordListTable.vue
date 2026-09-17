<!-- 词表浏览(任务书 14 组件清单):桌面 n-data-table / 手机卡片流;按词书分页加载 -->
<template>
  <div class="word-list-table">
    <!-- 桌面:表格 -->
    <n-data-table
      v-if="!isMobile"
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="false"
      :row-key="(row: Word) => row.id"
    />

    <!-- 手机:卡片流 -->
    <div v-else class="word-list-table__cards">
      <div v-for="word in list" :key="word.id" class="word-list-table__card">
        <p class="word-list-table__spelling">
          {{ word.spelling }}
          <span class="word-list-table__phonetic">{{ word.phonetic }}</span>
        </p>
        <p class="word-list-table__meaning">{{ word.partOfSpeech }} {{ word.meanings.join(';') }}</p>
      </div>
    </div>

    <div class="word-list-table__pager">
      <n-pagination
        :page="page"
        :page-size="PAGE_SIZE"
        :item-count="total"
        :page-slot="5"
        @update:page="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { NDataTable, NPagination, NTag, type DataTableColumns } from 'naive-ui'
import { getBookWords } from '@/api/vocab'
import { useDevice } from '@/composables/useDevice'
import type { Word } from '@/types/vocab'

const props = defineProps<{
  /** 当前词书 id */
  bookId: string
}>()

const { isMobile } = useDevice()

/** 每页词量 */
const PAGE_SIZE = 20

const list = ref<Word[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

/** 分层文案 */
const levelLabel: Record<Word['level'], string> = {
  high: '高频',
  core: '核心',
  cognitive: '认知',
}

/** 桌面表格列 */
const columns = computed<DataTableColumns<Word>>(() => [
  { title: '拼写', key: 'spelling', width: 160 },
  { title: '音标', key: 'phonetic', width: 160 },
  { title: '词性', key: 'partOfSpeech', width: 80 },
  { title: '释义', key: 'meanings', render: (row) => row.meanings.join(';') },
  {
    title: '分层',
    key: 'level',
    width: 90,
    render: (row) => h(NTag, { size: 'small', bordered: false }, { default: () => levelLabel[row.level] }),
  },
])

/** 拉取当前页 */
async function fetchPage(): Promise<void> {
  loading.value = true
  try {
    const res = await getBookWords(props.bookId, page.value, PAGE_SIZE)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPageChange(next: number): void {
  page.value = next
  fetchPage()
}

/** 切换词书时重置到第一页 */
watch(
  () => props.bookId,
  () => {
    page.value = 1
    fetchPage()
  }
)

onMounted(fetchPage)
</script>

<style scoped>
.word-list-table {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.word-list-table__cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.word-list-table__card {
  padding: 12px;
  border-radius: var(--lc-radius);
  background-color: var(--lc-bg-card);
}

.word-list-table__spelling {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 4px;
}

.word-list-table__phonetic {
  font-size: 12px;
  font-weight: 400;
  color: var(--lc-text-3);
  margin-left: 6px;
}

.word-list-table__meaning {
  font-size: 13px;
  color: var(--lc-text-2);
  overflow-wrap: break-word;
}

.word-list-table__pager {
  display: flex;
  justify-content: center;
}
</style>