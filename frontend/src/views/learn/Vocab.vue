<!-- 背单词页(任务书 10.13):词书选择 + 模式切换 + 今日进度;学习模式(卡片/手势/三按钮)与词表模式
     桌面:左词卡 + 右进度栏;手机:进度条置顶 + 全屏卡片 + 底部三按钮,左滑跳过/右滑收藏/上滑显示答案 -->
<template>
  <div class="vocab-page">
    <!-- 顶部:词书 + 模式 + 视图切换 + 今日计数(10.13) -->
    <div class="vocab-page__top">
      <n-select
        class="vocab-page__book"
        :value="store.currentBookId"
        :options="bookOptions"
        size="small"
        @update:value="onBookChange"
      />
      <n-radio-group :value="store.mode" size="small" @update:value="onModeChange">
        <n-radio-button value="new">新学</n-radio-button>
        <n-radio-button value="review">复习</n-radio-button>
      </n-radio-group>
      <div class="vocab-page__view-switch">
        <n-button size="small" :type="view === 'learn' ? 'primary' : 'default'" ghost @click="view = 'learn'">
          学习
        </n-button>
        <n-button size="small" :type="view === 'list' ? 'primary' : 'default'" ghost @click="view = 'list'">
          词表
        </n-button>
      </div>
      <span class="vocab-page__today">今日 {{ store.todayTask.doneCount }}/{{ store.todayTask.target }}</span>
    </div>

    <!-- 词表模式 -->
    <WordListTable v-if="view === 'list'" :book-id="store.currentBookId" />

    <!-- 学习模式 -->
    <div v-else class="vocab-page__learn" :class="{ 'is-mobile': isMobile }">
      <!-- 手机:进度条折叠到顶部 -->
      <WordProgressAside
        v-if="isMobile"
        :task="store.todayTask"
        :learned="store.currentBook?.learned ?? 0"
        @enter-review="onEnterReview"
      />

      <!-- 桌面:左卡片区 + 右进度栏 -->
      <div class="vocab-page__main">
        <template v-if="store.currentWord">
          <div ref="cardAreaRef" class="vocab-page__card-area">
            <WordCard :word="store.currentWord" :revealed="revealed" @reveal="revealed = true" />
          </div>
          <WordActionBar @rate="onRate" @reveal="revealed = !revealed" />
          <p v-if="isMobile" class="vocab-page__gesture-tip">左滑跳过 · 右滑收藏 · 上滑显示答案</p>
        </template>

        <!-- 队列耗尽:完成态 -->
        <div v-else class="vocab-page__done">
          <n-empty description="这一组词汇已学完" />
          <div class="vocab-page__done-actions">
            <n-button v-if="store.mode === 'new'" type="primary" @click="onEnterReview">进入复习</n-button>
            <n-button @click="onRestart">继续学习</n-button>
          </div>
        </div>
      </div>

      <!-- 桌面:右侧进度栏 -->
      <aside v-if="!isMobile" class="vocab-page__aside">
        <WordProgressAside
          :task="store.todayTask"
          :learned="store.currentBook?.learned ?? 0"
          @enter-review="onEnterReview"
        />
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
// 交互(10.13):三按钮评分提交 /vocab/review;手势:左滑跳过(不提交)、右滑收藏、上滑显示答案
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { NButton, NEmpty, NRadioButton, NRadioGroup, NSelect, useMessage } from 'naive-ui'
import WordCard from '@/components/vocab/WordCard.vue'
import WordActionBar from '@/components/vocab/WordActionBar.vue'
import WordProgressAside from '@/components/vocab/WordProgressAside.vue'
import WordListTable from '@/components/vocab/WordListTable.vue'
import { addFavorite } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useSwipe } from '@/composables/useSwipe'
import { useVocabStore } from '@/stores/vocab'

const message = useMessage()
const { isMobile } = useDevice()
const store = useVocabStore()

/** 视图:学习 / 词表 */
const view = ref<'learn' | 'list'>('learn')
/** 释义展开态(桌面恒 true;手机默认折叠,上滑/点击/空格展开) */
const revealed = ref(!isMobile.value)
/** 卡片区域(手势绑定目标) */
const cardAreaRef = ref<HTMLElement | undefined>(undefined)

/** 词书下拉选项 */
const bookOptions = computed(() => store.books.map((b) => ({ label: `${b.name}(${b.total}词)`, value: b.id })))

/** 移动端手势:左滑跳过 / 右滑收藏 / 上滑显示答案(10.13);bind 在卡片渲染后调用 */
const { bind: bindSwipe } = useSwipe(cardAreaRef, (direction) => {
  if (direction === 'left') {
    store.skipWord()
  } else if (direction === 'right') {
    void onFavorite()
  } else if (direction === 'up') {
    revealed.value = true
  }
})

/** 切换词书 */
async function onBookChange(bookId: string): Promise<void> {
  await store.setBook(bookId)
}

/** 切换模式(新学/复习) */
async function onModeChange(mode: string | number): Promise<void> {
  await store.setMode(mode === 'review' ? 'review' : 'new')
}

/** 三按钮评分 */
async function onRate(rating: 'unknown' | 'fuzzy' | 'known'): Promise<void> {
  await store.rateWord(rating)
}

/** 进入复习模式 */
async function onEnterReview(): Promise<void> {
  await store.setMode('review')
}

/** 重新开始(队列重置,继续学习) */
async function onRestart(): Promise<void> {
  await store.resetQueue()
}

/** 收藏当前单词(手机右滑) */
async function onFavorite(): Promise<void> {
  const word = store.currentWord
  if (!word) return
  try {
    await addFavorite({ type: 'word', content: word.spelling, refId: word.id })
    message.success(`已收藏 ${word.spelling}`)
  } catch {
    // 错误提示由 request 层统一弹出
  }
}

/** 切换单词时重置查看态并绑定手势(手机默认折叠,桌面直接可见) */
watch(
  () => store.currentWord,
  () => {
    revealed.value = !isMobile.value
    // 卡片元素随单词出现后绑定触摸监听(重复绑定同一函数无副作用)
    nextTick(() => bindSwipe())
  },
  { immediate: true }
)

/** 端切换同步查看态 */
watch(isMobile, (mobile) => {
  revealed.value = !mobile
})

onMounted(async () => {
  await store.init()
  await store.resetQueue()
})
</script>

<style scoped>
/* 页面自然流式布局:由布局层内容区统一滚动 */
.vocab-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.vocab-page__top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.vocab-page__book {
  width: 220px;
  max-width: 100%;
}

.vocab-page__view-switch {
  display: flex;
  gap: 8px;
}

.vocab-page__today {
  margin-left: auto;
  font-size: 13px;
  color: var(--lc-text-2);
  font-variant-numeric: tabular-nums;
}

/* 桌面:左卡片 + 右进度栏 */
.vocab-page__learn {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  min-width: 0;
}

.vocab-page__learn.is-mobile {
  grid-template-columns: minmax(0, 1fr);
}

.vocab-page__main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.vocab-page__card-area {
  min-width: 0;
  /* 阻止左右滑触发页面滚动,便于手势判定 */
  touch-action: pan-y;
}

.vocab-page__gesture-tip {
  text-align: center;
  font-size: 12px;
  color: var(--lc-text-3);
}

.vocab-page__done {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 48px 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.vocab-page__done-actions {
  display: flex;
  gap: 12px;
}

.vocab-page__aside {
  min-width: 0;
}
</style>