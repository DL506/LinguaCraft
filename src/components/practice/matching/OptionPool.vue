<!-- 五选五选项池(任务书 10.8):点击选择,已用的置灰;手机端横向可滚动 -->
<template>
  <div class="option-pool">
    <OptionCard
      v-for="opt in options"
      :key="opt.key"
      :option="opt"
      :used="usedKeys.includes(opt.key)"
      :active="activeKey === opt.key"
      @pick="$emit('pick', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import OptionCard from '@/components/practice/matching/OptionCard.vue'
import type { MatchingQuestionForAnswer } from '@/types/practice'

defineProps<{
  options: MatchingQuestionForAnswer['options']
  /** 已被使用的选项 key 列表(置灰) */
  usedKeys: string[]
  /** 当前选中待匹配的选项 key */
  activeKey: string | null
}>()

defineEmits<{ pick: [key: string] }>()
</script>

<style scoped>
.option-pool {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  /* 手机:横向可滚动(10.8);桌面也允许滚动兜底 */
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.option-pool :deep(.option-card) {
  flex-shrink: 0;
  max-width: 220px;
}
</style>