<!-- 计时显示(任务书 14 组件清单):mm:ss,用于答题头部 -->
<template>
  <span class="countdown-timer">{{ formatDuration(seconds) }}</span>
</template>

<script setup lang="ts">
import { formatDuration } from '@/utils/format'

defineProps<{ seconds: number }>()
</script>

<style scoped>
.countdown-timer {
  font-variant-numeric: tabular-nums;
  color: var(--lc-text-2);
}
</style>