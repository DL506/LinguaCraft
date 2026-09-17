<!-- 背单词进度栏(任务书 10.13):今日进度条 + 新学/复习/待复习/已掌握 + [进入复习] + 快捷键说明
     桌面右侧栏;手机折叠为顶部条(由父级布局控制) -->
<template>
  <div class="word-progress" :class="{ 'is-mobile': isMobile }">
    <div class="word-progress__head">
      <span class="word-progress__title">今日进度</span>
      <span class="word-progress__count">{{ task.doneCount }}/{{ task.target }}</span>
    </div>
    <n-progress
      type="line"
      :percentage="task.target > 0 ? Math.min(100, Math.round((task.doneCount / task.target) * 100)) : 0"
      :show-indicator="false"
      :height="8"
    />

    <ul class="word-progress__stats">
      <li><span>新学</span><b>{{ task.newCount }}</b></li>
      <li><span>待复习</span><b>{{ task.reviewCount }}</b></li>
      <li><span>已掌握</span><b>{{ learned }}</b></li>
    </ul>

    <n-button type="primary" secondary block @click="$emit('enter-review')">进入复习</n-button>

    <div v-if="!isMobile" class="word-progress__shortcuts">
      <p class="word-progress__shortcuts-title">快捷键</p>
      <p>1 不认识 · 2 模糊 · 3 认识 · 空格 显示释义</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NButton, NProgress } from 'naive-ui'
import { useDevice } from '@/composables/useDevice'
import type { VocabTodayTask } from '@/types/vocab'

defineProps<{
  /** 今日任务进度(12.5) */
  task: VocabTodayTask
  /** 当前词书已掌握词数 */
  learned: number
}>()

defineEmits<{ 'enter-review': [] }>()

const { isMobile } = useDevice()
</script>

<style scoped>
.word-progress {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.word-progress__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.word-progress__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--lc-text-1);
}

.word-progress__count {
  font-size: 13px;
  color: var(--lc-text-2);
  font-variant-numeric: tabular-nums;
}

.word-progress__stats {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.word-progress__stats li {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: var(--lc-text-3);
}

.word-progress__stats b {
  font-size: 16px;
  color: var(--lc-text-1);
}

.word-progress__shortcuts {
  border-top: 1px dashed var(--lc-border);
  padding-top: 10px;
}

.word-progress__shortcuts-title {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-bottom: 4px;
}

.word-progress__shortcuts p:last-child {
  font-size: 12px;
  color: var(--lc-text-2);
}

/* 手机折叠到顶部(10.13) */
.word-progress.is-mobile {
  padding: 12px;
}
</style>