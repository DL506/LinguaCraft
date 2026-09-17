<!-- 来源筛选栏(任务书 14 组件清单):列表页顶部——来源三段切换 + 年份/老师筛选
     真题与老师出题列表复用本组件 -->
<template>
  <div class="practice-source-bar">
    <SourceSwitcher :model-value="source" @update:model-value="$emit('update:source', $event)" />

    <div v-if="source !== 'ai'" class="practice-source-bar__filters">
      <n-button
        v-for="chip in filterChips"
        :key="chip"
        size="small"
        :type="selected === chip ? 'primary' : 'default'"
        ghost
        @click="$emit('update:selected', chip)"
      >
        {{ chip }}
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui'
import SourceSwitcher from '@/components/common/SourceSwitcher.vue'
import type { SourceType } from '@/types/practice'

defineProps<{
  source: SourceType
  /** 筛选选项(真题为年份集合,老师出题为老师名集合) */
  filterChips: string[]
  /** 当前选中的筛选值('全部' 表示不过滤) */
  selected: string
}>()

defineEmits<{
  'update:source': [value: SourceType]
  'update:selected': [value: string]
}>()
</script>

<style scoped>
.practice-source-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.practice-source-bar__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>