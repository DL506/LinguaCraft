<!-- 五选五空格槽位(任务书 10.8):默认虚线;选中主色;已填实线+可移除;批改态绿勾 / 红叉 -->
<template>
  <span class="blank-slot" :class="slotClass" role="button" @click="onClick">
    <template v-if="graded && detail">
      <n-icon
        class="blank-slot__mark"
        :class="detail.isCorrect ? 'is-correct' : 'is-wrong'"
        :component="detail.isCorrect ? CheckmarkCircle : CloseCircle"
      />
      <span class="blank-slot__text">[[{{ id }}]] {{ detail.userAnswer || '未作答' }}</span>
      <span v-if="!detail.isCorrect" class="blank-slot__correct">正确:{{ detail.correctAnswer }}</span>
    </template>
    <template v-else-if="answerKey">
      <span class="blank-slot__text">[[{{ id }}]] 已选 {{ answerKey }}</span>
      <span class="blank-slot__remove" title="移除" @click.stop="$emit('remove')"></span>
    </template>
    <template v-else>
      <span class="blank-slot__text">[[{{ id }}]] 点击选择选项</span>
    </template>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NIcon } from 'naive-ui'
import { CheckmarkCircle, CloseCircle } from '@vicons/ionicons5'
import type { SubmitDetail } from '@/types/practice'

const props = defineProps<{
  /** 空位编号(与占位符 n 一致) */
  id: number
  /** 当前填入的选项 key(未填为空) */
  answerKey?: string
  /** 选中待匹配态(主色边框) */
  active: boolean
  /** 批改态 */
  graded: boolean
  /** 批改明细 */
  detail?: SubmitDetail
}>()

const emit = defineEmits<{ click: []; remove: [] }>()

/** 槽位状态类:批改对错优先,其次已填/选中 */
const slotClass = computed(() => {
  if (props.graded && props.detail) return props.detail.isCorrect ? 'is-correct' : 'is-wrong'
  if (props.answerKey) return 'is-filled'
  if (props.active) return 'is-active'
  return ''
})

function onClick(): void {
  // 批改态固定展示,不再交互
  if (props.graded) return
  emit('click')
}
</script>

<style scoped>
.blank-slot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 2px 10px;
  margin: 0 4px;
  border: 1px dashed var(--lc-border);
  border-radius: 8px;
  background-color: var(--lc-bg-card);
  cursor: pointer;
  font-size: 13px;
  vertical-align: middle;
  /* 长内容断词,防溢出 */
  overflow-wrap: break-word;
}

.blank-slot:hover {
  border-color: var(--lc-primary);
}

/* 选中(待匹配):主色边框 + 浅色背景(任务书 10.8 状态表) */
.blank-slot.is-active {
  border-style: solid;
  border-color: var(--lc-primary);
  background-color: var(--lc-primary-soft);
}

/* 已填:实线边框 */
.blank-slot.is-filled {
  border-style: solid;
}

/* 批改正确:绿框 */
.blank-slot.is-correct {
  border-style: solid;
  border-color: var(--lc-success);
}

/* 批改错误:红框 */
.blank-slot.is-wrong {
  border-style: solid;
  border-color: var(--lc-error);
}

.blank-slot__text {
  color: var(--lc-text-2);
}

.blank-slot__remove {
  color: var(--lc-text-3);
}

.blank-slot__remove:hover {
  color: var(--lc-error);
}

.blank-slot__mark {
  font-size: 14px;
}

.blank-slot__mark.is-correct {
  color: var(--lc-success);
}

.blank-slot__mark.is-wrong {
  color: var(--lc-error);
}

.blank-slot__correct {
  color: var(--lc-success);
  font-size: 12px;
}
</style>