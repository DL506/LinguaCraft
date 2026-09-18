<!-- 作文编辑区(任务书 10.11):写作输入 + 字数(词汇数)实时统计
     手机端统计条固定底部(sticky);草稿自动保存由页面统一处理(localStorage) -->
<template>
  <div class="writing-editor">
    <n-input
      class="writing-editor__input"
      type="textarea"
      :value="modelValue"
      :disabled="readonly"
      :autosize="{ minRows: 14, maxRows: 26 }"
      placeholder="Write your composition here..."
      @update:value="$emit('update:modelValue', $event)"
    />
    <div class="writing-editor__footer" :class="{ 'is-mobile': isMobile }">
      <span class="writing-editor__count" :class="countClass">
        {{ wordCount }} / {{ wordLimit }} 词
      </span>
      <span class="writing-editor__tip">按词汇数统计(以空格分词)</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NInput } from 'naive-ui'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  /** 作文正文 */
  modelValue: string
  /** 词数要求(词汇数上限提示) */
  wordLimit: number
  /** 只读展示(批改回看时可复用) */
  readonly?: boolean
}>()

defineEmits<{ 'update:modelValue': [value: string] }>()

const { isMobile } = useDevice()

/** 词汇数:trim 后按连续空白分词(与"词数 100-120"要求口径一致) */
const wordCount = computed(() => {
  const text = props.modelValue.trim()
  return text ? text.split(/\s+/).length : 0
})

/** 数量状态:达到要求转为成功色 */
const countClass = computed(() => (wordCount.value >= props.wordLimit ? 'is-ok' : ''))
</script>

<style scoped>
.writing-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.writing-editor__input {
  min-width: 0;
}

.writing-editor__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--lc-radius);
  background-color: var(--lc-bg-card);
  font-size: 13px;
}

/* 手机:字数统计固定底部(任务书 10.11) */
.writing-editor__footer.is-mobile {
  position: sticky;
  bottom: 8px;
  z-index: 10;
  box-shadow: var(--lc-shadow-sm);
}

.writing-editor__count {
  color: var(--lc-text-2);
  font-variant-numeric: tabular-nums;
}

.writing-editor__count.is-ok {
  color: var(--lc-success);
}

.writing-editor__tip {
  color: var(--lc-text-3);
  font-size: 12px;
}
</style>