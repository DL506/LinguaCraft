<!-- AI 出题生成视图(任务书 10.6):点即生成,loading 后直接答题,无参数面板;失败给重试 -->
<template>
  <div class="ai-generating">
    <n-icon class="ai-generating__icon" :component="Bot24Regular" :size="48" />
    <h2 class="ai-generating__title">{{ failed ? '生成失败' : 'AI 正在为你出题...' }}</h2>
    <p v-if="!failed" class="ai-generating__sub">大约需要 10~20 秒</p>
    <div class="ai-generating__actions">
      <n-button v-if="failed" type="primary" @click="$emit('retry')">重试</n-button>
      <n-button v-else @click="$emit('cancel')">取消</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NButton, NIcon } from 'naive-ui'
// ionicons5 无机器人图标,机器人语义场景选用 Fluent 图标库
import { Bot24Regular } from '@vicons/fluent'

defineProps<{ failed?: boolean }>()
defineEmits<{ retry: []; cancel: [] }>()
</script>

<style scoped>
.ai-generating {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 60vh;
}

.ai-generating__icon {
  /* AI 出题机器人标识(主题主色,亮暗自适应) */
  color: var(--lc-primary);
}

.ai-generating__title {
  font-size: 18px;
  color: var(--lc-text-1);
}

.ai-generating__sub {
  font-size: 13px;
  color: var(--lc-text-3);
}

.ai-generating__actions {
  margin-top: 8px;
}
</style>