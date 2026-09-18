<!-- 完形单题(任务书 10.9):空位序号 + 四选项;批改态对错着色与解析 -->
<template>
  <div :id="`cloze-q-${id}`" class="cloze-question" :class="{ 'is-active': active }">
    <p class="cloze-question__stem">空位 {{ id }}</p>
    <n-radio-group :value="modelValue" :disabled="graded" @update:value="onPick">
      <div
        v-for="opt in options"
        :key="opt.key"
        class="cloze-question__option"
        :class="optionClass(opt.key)"
      >
        <n-radio :value="opt.key">
          <span class="cloze-question__key">{{ opt.key }}.</span>
          <span>{{ opt.text }}</span>
        </n-radio>
      </div>
    </n-radio-group>

    <div v-if="graded && detail" class="cloze-question__detail">
      <p>
        <template v-if="detail.isCorrect">
          <n-icon class="cloze-question__mark is-correct" :component="CheckmarkCircle" />
          回答正确
        </template>
        <template v-else>
          <n-icon class="cloze-question__mark is-wrong" :component="CloseCircle" />
          你的答案:{{ detail.userAnswer || '未作答' }} · 正确答案:{{ detail.correctAnswer }}
        </template>
      </p>
      <p class="cloze-question__explanation">解析:{{ detail.explanation }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NIcon, NRadio, NRadioGroup } from 'naive-ui'
import { CheckmarkCircle, CloseCircle } from '@vicons/ionicons5'
import type { ClozeQuestionForAnswer, SubmitDetail } from '@/types/practice'

const props = defineProps<{
  /** 空位编号(与占位符 n 一致) */
  id: number
  /** 该空位的四个选项 */
  options: ClozeQuestionForAnswer['blanks'][number]['options']
  /** 当前选中选项 key */
  modelValue: string
  /** 是否批改态 */
  graded: boolean
  /** 批改明细 */
  detail?: SubmitDetail
  /** 文章侧点击空位时高亮本题目 */
  active: boolean
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
.cloze-question {
  padding: 12px;
  border: 1px solid transparent;
  border-radius: var(--lc-radius);
}

.cloze-question.is-active {
  border-color: var(--lc-primary);
  background-color: var(--lc-primary-soft);
}

.cloze-question__stem {
  font-size: 14px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 8px;
}

.cloze-question__option {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid transparent;
  margin-bottom: 4px;
}

.cloze-question__option:hover {
  background-color: var(--lc-bg-hover);
}

.cloze-question__option.is-correct {
  border-color: var(--lc-success);
}

.cloze-question__option.is-wrong {
  border-color: var(--lc-error);
}

.cloze-question__key {
  font-weight: 600;
  margin-right: 4px;
}

/* 批改对错图标(取状态变量,亮暗双主题一致) */
.cloze-question__mark {
  font-size: 14px;
  vertical-align: -2px;
  margin-right: 4px;
}

.cloze-question__mark.is-correct {
  color: var(--lc-success);
}

.cloze-question__mark.is-wrong {
  color: var(--lc-error);
}

.cloze-question__detail {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--lc-text-2);
  background-color: var(--lc-bg-hover);
}

.cloze-question__explanation {
  margin-top: 4px;
  color: var(--lc-text-3);
}
</style>