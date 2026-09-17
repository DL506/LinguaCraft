<!-- 来源三段切换器(任务书 10.6):真题 / 老师出题 / AI出题;三端一致,不是导航层级 -->
<template>
  <n-button-group class="source-switcher">
    <n-button
      v-for="option in options"
      :key="option.value"
      :type="modelValue === option.value ? 'primary' : 'default'"
      @click="$emit('update:modelValue', option.value as SourceType)"
    >
      {{ option.label }}
    </n-button>
  </n-button-group>
</template>

<script setup lang="ts">
import { NButton, NButtonGroup } from 'naive-ui'
import type { SourceType } from '@/types/practice'

defineProps<{ modelValue: SourceType }>()
defineEmits<{ 'update:modelValue': [value: SourceType] }>()

/** 三态选项(真题/老师出题/AI出题;AI 由后端直接生成,无参数面板) */
const options: { label: string; value: SourceType }[] = [
  { label: '真题', value: 'real' },
  { label: '老师出题', value: 'teacher' },
  { label: 'AI出题', value: 'ai' },
]
</script>