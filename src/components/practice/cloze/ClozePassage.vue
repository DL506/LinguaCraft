<!-- 完形文章(任务书 10.9):全文 + 空位槽;空位显示编号/已选选项;批改态绿 ✅ / 红 ❌ -->
<template>
  <div class="cloze-passage">
    <template v-for="(seg, index) in segments" :key="index">
      <template v-if="seg.type === 'text'">{{ seg.content }}</template>
      <span
        v-else
        class="cloze-blank"
        :class="blankClass(seg.id ?? 0)"
        role="button"
        @click="onBlankClick(seg.id ?? 0)"
      >
        <template v-if="graded && detailOf(seg.id ?? 0)">
          <span class="cloze-blank__mark">{{ detailOf(seg.id ?? 0)?.isCorrect ? '✅' : '' }}</span>
          <span>[[{{ seg.id }}]] {{ detailOf(seg.id ?? 0)?.userAnswer || '未作答' }}</span>
          <span v-if="!detailOf(seg.id ?? 0)?.isCorrect" class="cloze-blank__correct">
            正确:{{ detailOf(seg.id ?? 0)?.correctAnswer }}
          </span>
        </template>
        <template v-else-if="answers[seg.id ?? 0]">[[{{ seg.id }}]] {{ answers[seg.id ?? 0] }}</template>
        <template v-else>[[{{ seg.id }}]]</template>
      </span>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePlaceholder } from '@/composables/usePlaceholder'
import type { SubmitDetail } from '@/types/practice'

const props = defineProps<{
  /** 挖空文章(空位以 [[n]] 占位) */
  passage: string
  /** 作答:空位 id → 选项 key */
  answers: Record<number, string>
  /** 当前选中的空位(高亮) */
  activeId: number | null
  /** 批改态 */
  graded: boolean
  /** 批改明细 */
  details: SubmitDetail[]
}>()

const emit = defineEmits<{ select: [id: number] }>()

const { parsePassage } = usePlaceholder()

/** 占位符解析:文章 → 文本/空位片段 */
const segments = computed(() => parsePassage(props.passage))

function detailOf(id: number): SubmitDetail | undefined {
  if (!props.graded) return undefined
  return props.details.find((d) => d.id === id)
}

/** 空位状态类:批改对错优先,其次已填/选中 */
function blankClass(id: number): string {
  const detail = detailOf(id)
  if (detail) return detail.isCorrect ? 'is-correct' : 'is-wrong'
  if (props.answers[id]) return 'is-filled'
  if (props.activeId === id) return 'is-active'
  return ''
}

function onBlankClick(id: number): void {
  if (props.graded) return
  emit('select', id)
}
</script>

<style scoped>
.cloze-passage {
  line-height: 2.2;
  color: var(--lc-text-1);
  /* 断词兜底,防横向溢出 */
  overflow-wrap: break-word;
}

.cloze-blank {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 30px;
  padding: 1px 10px;
  margin: 0 4px;
  border: 1px dashed var(--lc-border);
  border-radius: 8px;
  background-color: var(--lc-bg-card);
  cursor: pointer;
  font-size: 13px;
  color: var(--lc-text-2);
  vertical-align: middle;
}

.cloze-blank:hover {
  border-color: var(--lc-primary);
}

.cloze-blank.is-active {
  border-style: solid;
  border-color: var(--lc-primary);
  background-color: var(--lc-primary-soft);
}

.cloze-blank.is-filled {
  border-style: solid;
  color: var(--lc-text-1);
}

.cloze-blank.is-correct {
  border-style: solid;
  border-color: var(--lc-success);
}

.cloze-blank.is-wrong {
  border-style: solid;
  border-color: var(--lc-error);
}

.cloze-blank__mark {
  font-size: 12px;
}

.cloze-blank__correct {
  color: var(--lc-success);
  font-size: 12px;
}
</style>