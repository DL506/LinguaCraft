<!-- 单选题(任务书 14 组件清单):答题态可选,批改态展示对错着色与解析 -->
<template>
  <div :id="`question-${question.id}`" class="question-item">
    <p class="question-item__stem">{{ question.id }}. {{ question.stem }}</p>
    <n-radio-group :value="modelValue" :disabled="graded" @update:value="onPick">
      <div
        v-for="opt in question.options"
        :key="opt.key"
        class="question-item__option"
        :class="optionClass(opt.key)"
      >
        <n-radio :value="opt.key">
          <span class="question-item__key">{{ opt.key }}.</span>
          <span>{{ opt.text }}</span>
        </n-radio>
      </div>
    </n-radio-group>

    <!-- 批改态详情:对错标记 + 用户答案 + 正确答案 + 解析(答案仅批改后下发) -->
    <div v-if="graded && detail" class="question-item__detail">
      <p>
        <template v-if="detail.isCorrect">✅ 回答正确</template>
        <template v-else>❌ 你的答案:{{ detail.userAnswer || '未作答' }} · 正确答案:{{ detail.correctAnswer }}</template>
      </p>
      <p class="question-item__explanation">解析:{{ detail.explanation }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NRadio, NRadioGroup } from 'naive-ui'
import type { ReadingQuestionForAnswer } from '@/types/practice'
import type { SubmitDetail } from '@/types/practice'

const props = defineProps<{
  /** 题目(阅读题) */
  question: ReadingQuestionForAnswer['questions'][number]
  /** 当前选中选项 key */
  modelValue: string
  /** 是否批改态 */
  graded: boolean
  /** 批改明细(按题号下发) */
  detail?: SubmitDetail
}>()

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

function onPick(value: string | number | boolean): void {
  emit('update:modelValue', String(value))
}

/** 批改态选项着色:正确绿 / 错选红 */
function optionClass(key: string): string {
  if (!props.graded || !props.detail) return ''
  if (key === props.detail.correctAnswer) return 'is-correct'
  if (key === props.detail.userAnswer && !props.detail.isCorrect) return 'is-wrong'
  return ''
}
</script>

<style scoped>
.question-item {
  padding: 14px 0;
  border-bottom: 1px dashed var(--lc-border);
}

.question-item__stem {
  font-size: 15px;
  color: var(--lc-text-1);
  margin-bottom: 10px;
}

.question-item__option {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid transparent;
  margin-bottom: 6px;
}

.question-item__option:hover {
  background-color: var(--lc-bg-hover);
}

.question-item__option.is-correct {
  border-color: var(--lc-success);
  background-color: var(--lc-primary-soft);
}

.question-item__option.is-wrong {
  border-color: var(--lc-error);
  background-color: rgba(208, 48, 80, 0.06);
}

.question-item__key {
  font-weight: 600;
  margin-right: 4px;
}

.question-item__detail {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--lc-text-2);
  background-color: var(--lc-bg-hover);
}

.question-item__explanation {
  margin-top: 4px;
  color: var(--lc-text-3);
}
</style>