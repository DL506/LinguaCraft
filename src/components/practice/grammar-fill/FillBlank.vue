<!-- 语法填空单空(任务书 10.10):提示词(或"无提示词")+ n-input 输入;
     提交后逐空显示正确/错误,解析可展开 -->
<template>
  <div :id="`fill-q-${id}`" class="fill-question" :class="{ 'is-active': active }">
    <p class="fill-question__head">
      <span class="fill-question__no">空 {{ id }}</span>
      <span v-if="hint" class="fill-question__hint">({{ hint }})</span>
      <span v-else class="fill-question__hint fill-question__hint--none">无提示词</span>
    </p>
    <n-input
      :value="modelValue"
      size="large"
      :disabled="graded"
      :status="inputStatus"
      placeholder="输入答案"
      @update:value="onInput"
      @keydown.enter="$emit('enter')"
    />

    <!-- 批改态:对错 + 正确答案 + 可展开解析(10.10) -->
    <div v-if="graded && detail" class="fill-question__detail">
      <p>
        <template v-if="detail.isCorrect">✅ 回答正确</template>
        <template v-else>❌ 你的答案:{{ detail.userAnswer || '未作答' }} · 正确答案:{{ detail.correctAnswer }}</template>
      </p>
      <n-collapse>
        <n-collapse-item title="查看解析" name="explanation">
          {{ detail.explanation }}
        </n-collapse-item>
      </n-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NCollapse, NCollapseItem, NInput } from 'naive-ui'
import type { SubmitDetail } from '@/types/practice'

const props = defineProps<{
  /** 空位编号(与占位符 n 一致) */
  id: number
  /** 提示词(无则显示"无提示词") */
  hint?: string
  /** 当前填空文本 */
  modelValue: string
  /** 是否批改态 */
  graded: boolean
  /** 批改明细 */
  detail?: SubmitDetail
  /** 文章侧点击空位时高亮本填空 */
  active: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  /** 回车提交(由页面决定行为:移动端收起键盘/确认) */
  enter: []
}>()

function onInput(value: string): void {
  emit('update:modelValue', value)
}

/** 批改态输入框状态:绿/红(naive 输入框 status) */
const inputStatus = computed(() => {
  if (!props.graded || !props.detail) return undefined
  return props.detail.isCorrect ? ('success' as const) : ('error' as const)
})
</script>

<style scoped>
.fill-question {
  padding: 12px;
  border: 1px solid transparent;
  border-radius: var(--lc-radius);
}

.fill-question.is-active {
  border-color: var(--lc-primary);
  background-color: var(--lc-primary-soft);
}

.fill-question__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.fill-question__no {
  font-size: 14px;
  font-weight: 600;
  color: var(--lc-text-1);
}

.fill-question__hint {
  font-size: 13px;
  color: var(--lc-text-2);
}

.fill-question__hint--none {
  color: var(--lc-text-3);
}

.fill-question__detail {
  margin-top: 8px;
  font-size: 13px;
  color: var(--lc-text-2);
}
</style>