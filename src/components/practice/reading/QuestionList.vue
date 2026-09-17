<!-- 题目列表(任务书 14 组件清单):渲染全部阅读题,答题/批改两态;暴露 scrollTo 供答题卡定位 -->
<template>
  <div ref="listEl" class="question-list">
    <QuestionItem
      v-for="q in questions"
      :key="q.id"
      :question="q"
      :model-value="answers[q.id] ?? ''"
      :graded="graded"
      :detail="detailOf(q.id)"
      @update:model-value="onPick(q.id, $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import QuestionItem from '@/components/practice/reading/QuestionItem.vue'
import type { ReadingQuestionForAnswer, SubmitDetail } from '@/types/practice'

const props = defineProps<{
  questions: ReadingQuestionForAnswer['questions']
  /** 已作答映射:题号 → 选项 key */
  answers: Record<number, string>
  /** 批改态标记 */
  graded: boolean
  /** 批改明细(按题号) */
  details: SubmitDetail[]
}>()

const emit = defineEmits<{ 'update:answer': [id: number, key: string] }>()

const listEl = ref<HTMLElement | null>(null)

function onPick(id: number, key: string): void {
  emit('update:answer', id, key)
}

/** 取某题的批改明细 */
function detailOf(id: number): SubmitDetail | undefined {
  if (!props.graded) return undefined
  return props.details.find((d) => d.id === id)
}

/** 滚动定位到指定题(答题卡点击跳转) */
function scrollTo(id: number): void {
  listEl.value?.querySelector(`#question-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

defineExpose({ scrollTo })
</script>

<style scoped>
.question-list {
  display: flex;
  flex-direction: column;
}
</style>