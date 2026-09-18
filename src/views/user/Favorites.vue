<!-- 收藏(任务书 10.15):分组列表;顶部分类 Tab(全部/单词/句子);支持删除(增在题页/背单词页完成) -->
<template>
  <div class="favorites-page">
    <!-- 分类 Tab -->
    <div class="favorites-page__tabs">
      <n-button
        v-for="chip in categoryChips"
        :key="chip.value"
        size="small"
        :type="category === chip.value ? 'primary' : 'default'"
        ghost
        @click="category = chip.value"
      >
        {{ chip.label }}
      </n-button>
    </div>

    <!-- 分组列表 -->
    <div class="favorites-page__list">
      <div v-for="item in displayList" :key="item.id" class="favorite-item">
        <div class="favorite-item__main">
          <n-tag size="small" :bordered="false" :type="item.type === 'word' ? 'info' : 'success'">
            {{ item.type === 'word' ? '单词' : '句子' }}
          </n-tag>
          <p class="favorite-item__content">{{ item.content }}</p>
          <p class="favorite-item__time">{{ formatDateTime(item.createdAt) }}</p>
        </div>
        <n-button size="small" type="error" quaternary @click="remove(item)">删除</n-button>
      </div>
    </div>

    <n-empty v-if="displayList.length === 0 && !loading" description="暂无收藏内容" />

    <!-- 分页 -->
    <div class="favorites-page__pager">
      <n-pagination :page="page" :page-size="PAGE_SIZE" :item-count="total" @update:page="onPageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /user/favorites(分页)、DELETE /user/favorites/:id(12.7);分类为本地过滤
import { computed, onMounted, ref } from 'vue'
import { NButton, NEmpty, NPagination, useDialog } from 'naive-ui'
import { deleteFavorite, getFavorites } from '@/api/user'
import { formatDateTime } from '@/utils/format'
import type { FavoriteItem } from '@/types/user'

const dialog = useDialog()

/** 每页条数 */
const PAGE_SIZE = 10

const categoryChips = [
  { label: '全部', value: 'all' },
  { label: '单词', value: 'word' },
  { label: '句子', value: 'sentence' },
] as const

const category = ref<'all' | 'word' | 'sentence'>('all')
const list = ref<FavoriteItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

/** 当前分类下的列表(本地过滤) */
const displayList = computed(() =>
  category.value === 'all' ? list.value : list.value.filter((i) => i.type === category.value)
)

/** 拉取列表 */
async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getFavorites({ page: page.value, size: PAGE_SIZE })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPageChange(next: number): void {
  page.value = next
  fetchList()
}

/** 删除(二次确认) */
function remove(item: FavoriteItem): void {
  dialog.warning({
    title: '删除收藏',
    content: '确认删除该条收藏吗?',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteFavorite(item.id)
      await fetchList()
    },
  })
}

onMounted(fetchList)
</script>

<style scoped>
.favorites-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.favorites-page__tabs {
  display: flex;
  gap: 8px;
}

.favorites-page__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.favorite-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.favorite-item__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}

.favorite-item__content {
  font-size: 14px;
  color: var(--lc-text-1);
  overflow-wrap: break-word;
}

.favorite-item__time {
  font-size: 12px;
  color: var(--lc-text-3);
}

.favorites-page__pager {
  display: flex;
  justify-content: center;
}
</style>