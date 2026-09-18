<!-- 题型列表(任务书 14 组件清单):真题/模拟题共用;列表项带提交记录时显示「查看结果」(10.6) -->
<template>
  <div class="practice-list">
    <n-card
      v-for="item in list"
      :key="item.id"
      class="practice-list__card"
      :bordered="false"
    >
      <div class="item">
        <div class="item__main">
          <p class="item__title">{{ item.title }}</p>
          <div class="item__meta">
            <n-tag v-for="tag in item.tags" :key="tag" size="small" :bordered="false">{{ tag }}</n-tag>
            <n-tag size="small" :bordered="false" type="info">难度 {{ item.difficulty }}/5</n-tag>
          </div>
          <p v-if="item.submitted && item.score !== undefined" class="item__score">
            上次得分 {{ item.score }}/{{ item.totalScore }}
            ({{ item.submissionStatus === 'pending' ? '待批改' : '已批改' }})
          </p>
        </div>
        <div class="item__actions">
          <n-button v-if="item.submitted" type="primary" secondary @click="$emit('review', item)">查看结果</n-button>
          <n-button v-else type="primary" @click="$emit('start', item)">开始作答</n-button>
          <n-button v-if="item.submitted" @click="$emit('start', item)">重新作答</n-button>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NButton, NCard, NTag } from 'naive-ui'
import type { PracticeListItem } from '@/types/practice'

defineProps<{ list: PracticeListItem[] }>()

defineEmits<{
  /** 开始/重新作答某题 */
  start: [item: PracticeListItem]
  /** 查看某题提交结果(按最新状态分支:pending 视图 / 批改态) */
  review: [item: PracticeListItem]
}>()
</script>

<style scoped>
.practice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.practice-list__card {
  background-color: var(--lc-bg-card);
}

.item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item__main {
  flex: 1;
  min-width: 0;
}

.item__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 8px;
}

.item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
}

.item__score {
  font-size: 13px;
  color: var(--lc-text-3);
}

.item__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
</style>