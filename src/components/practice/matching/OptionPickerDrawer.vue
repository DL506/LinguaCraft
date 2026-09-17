<!-- 手机选项选择面板(任务书 10.8):点击空格弹出底部 n-drawer,从面板中选择选项 -->
<template>
  <n-drawer :show="show" placement="bottom" display-directive="show" @update:show="$emit('update:show', $event)">
    <div class="picker">
      <p class="picker__title">选择选项</p>
      <div class="picker__list">
        <div
          v-for="opt in options"
          :key="opt.key"
          class="picker__item"
          :class="{ 'is-used': usedKeys.includes(opt.key) }"
          @click="onPick(opt.key)"
        >
          <span class="picker__key">{{ opt.key }}</span>
          <span class="picker__text">{{ opt.text }}</span>
        </div>
      </div>
    </div>
  </n-drawer>
</template>

<script setup lang="ts">
import { NDrawer } from 'naive-ui'
import type { MatchingQuestionForAnswer } from '@/types/practice'

const props = defineProps<{
  show: boolean
  options: MatchingQuestionForAnswer['options']
  /** 已被使用的选项 key(置灰) */
  usedKeys: string[]
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
  pick: [key: string]
}>()

function onPick(key: string): void {
  if (props.usedKeys.includes(key)) return
  emit('pick', key)
  emit('update:show', false)
}
</script>

<style scoped>
.picker {
  padding: 16px;
}

.picker__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 12px;
}

.picker__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 列表项 ≥44px 触达高度(任务书第 16 章) */
.picker__item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-height: 44px;
  padding: 10px 12px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  cursor: pointer;
}

.picker__item.is-used {
  opacity: 0.45;
  cursor: not-allowed;
}

.picker__key {
  font-weight: 700;
  color: var(--lc-primary);
  flex-shrink: 0;
}

.picker__text {
  color: var(--lc-text-1);
  font-size: 13px;
  overflow-wrap: break-word;
}
</style>