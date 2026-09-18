<!-- 题型列表筛选栏(任务书 14 组件清单):年份/老师筛选 chips
     来源三段切换统一由通用答题头部承担(10.6),此处不再重复渲染 -->
<template>
  <div class="practice-source-bar">
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
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui'

defineProps<{
  /** 筛选选项(真题为年份集合,模拟题为老师名集合) */
  filterChips: string[]
  /** 当前选中的筛选值('全部' 表示不过滤) */
  selected: string
}>()

defineEmits<{
  'update:selected': [value: string]
}>()
</script>

<style scoped>
.practice-source-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
</style>