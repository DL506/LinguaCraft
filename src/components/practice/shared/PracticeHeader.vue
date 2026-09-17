<!-- 通用答题头部(任务书 10.6):← 退出 | 题型名 | 来源三段切换 | 计时 | 提交
     五种题型共用;答题态显示计时与提交,列表/生成态隐藏 -->
<template>
  <div class="practice-header">
    <n-button quaternary circle class="practice-header__back" aria-label="退出" @click="$emit('back')">
      <template #icon><n-icon :component="ArrowBackOutline" /></template>
    </n-button>

    <span class="practice-header__title">{{ title }}</span>

    <SourceSwitcher :model-value="source" @update:model-value="$emit('update:source', $event)" />

    <div class="practice-header__right">
      <!-- 沉浸切换(会话级延续):显示/隐藏导航,默认沉浸(验收 5 不变) -->
      <n-button
        v-if="isImmersivePage"
        quaternary
        circle
        :title="isImmersive ? '显示导航' : '隐藏导航(沉浸)'"
        @click="toggleImmersive"
      >
        <template #icon>
          <n-icon :component="isImmersive ? ExpandOutline : ContractOutline" />
        </template>
      </n-button>
      <CountdownTimer v-if="answering" :seconds="elapsed" />
      <n-button v-if="answering" type="primary" size="small" :loading="submitting" @click="$emit('submit')">
        提交
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowBackOutline, ContractOutline, ExpandOutline } from '@vicons/ionicons5'
import { NButton, NIcon } from 'naive-ui'
import SourceSwitcher from '@/components/common/SourceSwitcher.vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'
import { useImmersive } from '@/composables/useImmersive'
import type { SourceType } from '@/types/practice'

defineProps<{
  /** 题型名 */
  title: string
  /** 当前来源(切换器选中态) */
  source: SourceType
  /** 是否答题态(显示计时与提交) */
  answering: boolean
  /** 已作答耗时(秒) */
  elapsed: number
  /** 提交中 */
  submitting: boolean
}>()

defineEmits<{
  back: []
  'update:source': [value: SourceType]
  submit: []
}>()

// 沉浸切换:头部按钮控制导航显隐(会话级延续,实现见 useImmersive)
const { isImmersive, isImmersivePage, toggleImmersive } = useImmersive()
</script>

<style scoped>
.practice-header {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--lc-border);
  background-color: var(--lc-bg-card);
}

.practice-header__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  min-width: 72px;
}

.practice-header__right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>