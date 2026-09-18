<!-- 通用答题头部(任务书 10.6):桌面/平板 单行 = ← 退出 | 题型名 | 来源三段切换 | 计时 | 提交
     手机:分两行适配窄屏——行1 = 退出|标题|沉浸|计时|提交;行2 = 来源切换(仅非答题态,
     答题中切换来源会放弃当前作答,故隐藏);五种题型共用,答题态显示计时与提交 -->
<template>
  <div
    class="practice-header"
    :class="{ 'is-mobile': isMobile, 'is-immersive-top': isMobile && isImmersive }"
  >
    <!-- 主行:退出 / 标题 / 来源(桌面) / 右侧操作(沉浸·计时·提交) -->
    <div class="practice-header__row">
      <n-button quaternary circle class="practice-header__back" aria-label="退出" @click="$emit('back')">
        <template #icon><n-icon :component="ArrowBackOutline" /></template>
      </n-button>

      <span class="practice-header__title">{{ title }}</span>

      <SourceSwitcher
        v-if="!isMobile"
        :model-value="source"
        @update:model-value="$emit('update:source', $event)"
      />

      <div class="practice-header__right">
        <!-- 沉浸切换(手动开启,默认不沉浸):隐藏/恢复导航 -->
        <n-button
          v-if="isImmersivePage"
          quaternary
          circle
          :title="isImmersive ? '退出沉浸' : '进入沉浸'"
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

    <!-- 手机备用行:来源切换(仅非答题态显示) -->
    <div v-if="isMobile && !answering" class="practice-header__row practice-header__row--source">
      <SourceSwitcher :model-value="source" @update:model-value="$emit('update:source', $event)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowBackOutline, ContractOutline, ExpandOutline } from '@vicons/ionicons5'
import { NButton, NIcon } from 'naive-ui'
import SourceSwitcher from '@/components/common/SourceSwitcher.vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'
import { useDevice } from '@/composables/useDevice'
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

const { isMobile } = useDevice()

// 沉浸切换:头部按钮手动开启/退出(默认不进入沉浸,实现见 useImmersive)
const { isImmersive, isImmersivePage, toggleImmersive } = useImmersive()
</script>

<style scoped>
.practice-header {
  display: flex;
  flex-direction: column;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--lc-border);
  background-color: var(--lc-bg-card);
}

/* 行容器:桌面单行时填满 56px;手机两行时各行自定高 */
.practice-header__row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  height: 100%;
  min-width: 0;
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

/* ===== 手机:两行紧凑布局(窄屏适配) ===== */
.practice-header.is-mobile {
  height: auto;
  padding: 0 12px;
}

.practice-header.is-mobile .practice-header__row {
  height: 48px;
  gap: 8px;
}

/* 来源行(仅非答题态):略矮于主行 */
.practice-header.is-mobile .practice-header__row--source {
  height: 44px;
  padding-bottom: 4px;
}

/* 手机:标题占据中段并可省略,防长标题挤压右侧 */
.practice-header.is-mobile .practice-header__title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 沉浸态(手机顶部栏被隐藏):补顶部安全区,防刘海遮挡 */
.practice-header.is-mobile.is-immersive-top {
  padding-top: env(safe-area-inset-top);
}
</style>