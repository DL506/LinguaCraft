<!-- 答题卡(任务书 14 组件清单):题号圆点,点击定位;批改态按对错着色 -->
<template>
  <div class="answer-sheet">
    <button
      v-for="id in ids"
      :key="id"
      type="button"
      class="answer-sheet__dot"
      :class="dotClass(id)"
      @click="$emit('select', id)"
    >
      {{ id }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SubmitDetail } from '@/types/practice'

const props = defineProps<{
  /** 题号序列(1..n) */
  count: number
  /** 已作答映射 */
  answers: Record<number, string>
  /** 批改明细(批改态着色) */
  details: SubmitDetail[]
}>()

defineEmits<{ select: [id: number] }>()

const ids = computed(() => Array.from({ length: props.count }, (_, i) => i + 1))

/** 圆点样式:批改态按对/错/未答;答题态按已答/未答 */
function dotClass(id: number): string {
  const detail = props.details.find((d) => d.id === id)
  if (detail) return detail.isCorrect ? 'is-correct' : 'is-wrong'
  return props.answers[id] ? 'is-answered' : ''
}
</script>

<style scoped>
.answer-sheet {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.answer-sheet__dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid var(--lc-border);
  background-color: var(--lc-bg-card);
  color: var(--lc-text-2);
  font-size: 12px;
  cursor: pointer;
  /* 触达目标:移动端可用(任务书 16 章 ≥44px 建议,化整卡整体可点) */
  min-width: 28px;
}

.answer-sheet__dot:hover {
  border-color: var(--lc-primary);
}

.answer-sheet__dot.is-answered {
  background-color: var(--lc-primary);
  border-color: var(--lc-primary);
  color: #fff;
}

.answer-sheet__dot.is-correct {
  background-color: var(--lc-success);
  border-color: var(--lc-success);
  color: #fff;
}

.answer-sheet__dot.is-wrong {
  background-color: var(--lc-error);
  border-color: var(--lc-error);
  color: #fff;
}
</style>