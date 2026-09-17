<!-- 待批改占位页(任务书 10.6):已提交,等待批改中;[返回列表] [刷新状态] -->
<template>
  <div class="pending-view">
    <div class="pending-view__icon">📤</div>
    <h2 class="pending-view__title">已提交,等待批改中</h2>
    <p class="pending-view__sub">{{ estimatedTime || '预计 24 小时内出结果' }}</p>
    <div class="pending-view__actions">
      <n-button @click="$emit('back')">返回列表</n-button>
      <n-button type="primary" :loading="refreshing" @click="$emit('refresh')">刷新状态</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui'

defineProps<{ estimatedTime?: string; refreshing?: boolean }>()
defineEmits<{ back: []; refresh: [] }>()
</script>

<style scoped>
.pending-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 60vh;
}

.pending-view__icon {
  font-size: 48px;
}

.pending-view__title {
  font-size: 18px;
  color: var(--lc-text-1);
}

.pending-view__sub {
  font-size: 13px;
  color: var(--lc-text-3);
}

.pending-view__actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
</style>