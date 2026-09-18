<!-- 五选五选项卡(任务书 10.8):三端点选,非拖拽;已用置灰不可选,选中态主色边框 -->
<template>
  <div
    class="option-card"
    :class="{ 'is-used': used, 'is-active': active }"
    role="button"
    :aria-disabled="used"
    @click="onClick"
  >
    <span class="option-card__key">{{ option.key }}</span>
    <span class="option-card__text">{{ option.text }}</span>
  </div>
</template>

<script setup lang="ts">
import type { MatchingQuestionForAnswer } from '@/types/practice'

const props = defineProps<{
  option: MatchingQuestionForAnswer['options'][number]
  /** 已被填入某空(置灰,不可再选) */
  used: boolean
  /** 选中待匹配态 */
  active: boolean
}>()

const emit = defineEmits<{ pick: [key: string] }>()

function onClick(): void {
  if (props.used) return
  emit('pick', props.option.key)
}
</script>

<style scoped>
.option-card {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  min-width: 120px;
  padding: 10px 12px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background-color: var(--lc-bg-card);
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.option-card:hover {
  border-color: var(--lc-primary);
}

.option-card.is-active {
  border-color: var(--lc-primary);
  background-color: var(--lc-primary-soft);
}

.option-card.is-used {
  opacity: 0.45;
  cursor: not-allowed;
}

.option-card__key {
  font-weight: 700;
  color: var(--lc-primary);
  flex-shrink: 0;
}

.option-card__text {
  color: var(--lc-text-1);
  font-size: 13px;
  /* 长文本断词兜底,防止横向溢出 */
  overflow-wrap: break-word;
}
</style>