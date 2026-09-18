<!-- 我的词书(任务书 10.15):卡片网格,桌面 3 列 / 手机单列;展示词数、已学与进度 -->
<template>
  <div class="books-page">
    <div class="books-page__grid">
      <div v-for="book in books" :key="book.id" class="book-card">
        <p class="book-card__name">{{ book.name }}</p>
        <p class="book-card__meta">{{ book.total }} 词 · 已学 {{ book.learned }}</p>
        <n-progress
          type="line"
          :percentage="book.total > 0 ? Math.round((book.learned / book.total) * 100) : 0"
          :show-indicator="false"
          :height="8"
        />
        <n-button type="primary" secondary block class="book-card__action" @click="startLearn">
          开始学习
        </n-button>
      </div>
    </div>

    <n-empty v-if="books.length === 0 && !loading" description="暂无词书" />
  </div>
</template>

<script setup lang="ts">
// 数据:GET /vocab/books(12.5);「开始学习」进入背单词页
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NEmpty, NProgress } from 'naive-ui'
import { getVocabBooks } from '@/api/vocab'
import type { VocabBook } from '@/types/vocab'

const router = useRouter()
const books = ref<VocabBook[]>([])
const loading = ref(false)

function startLearn(): void {
  router.push('/learn/vocab')
}

onMounted(async () => {
  loading.value = true
  try {
    books.value = await getVocabBooks()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.books-page {
  min-width: 0;
}

/* 桌面 3 列;手机单列 */
.books-page__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 767px) {
  .books-page__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

.book-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  overflow-wrap: break-word;
}

.book-card__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
}

.book-card__meta {
  font-size: 12px;
  color: var(--lc-text-3);
}

.book-card__action {
  margin-top: 4px;
}
</style>