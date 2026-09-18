<!-- 三按钮操作条(任务书 10.13):[不认识][模糊][认识];桌面内嵌 + 快捷键 1/2/3;手机底部固定 -->
<template>
  <div class="word-action-bar" :class="{ 'is-mobile': isMobile }">
    <n-button class="word-action-bar__btn" type="error" secondary @click="$emit('rate', 'unknown')">
      不认识
      <span v-if="!isMobile" class="word-action-bar__key">1</span>
    </n-button>
    <n-button class="word-action-bar__btn" type="warning" secondary @click="$emit('rate', 'fuzzy')">
      模糊
      <span v-if="!isMobile" class="word-action-bar__key">2</span>
    </n-button>
    <n-button class="word-action-bar__btn" type="success" secondary @click="$emit('rate', 'known')">
      认识
      <span v-if="!isMobile" class="word-action-bar__key">3</span>
    </n-button>
  </div>
</template>

<script setup lang="ts">
// 键盘快捷键:1=不认识 2=模糊 3=认识(10.13 桌面图"快捷键"说明)
import { onMounted, onUnmounted } from 'vue'
import { NButton } from 'naive-ui'
import { useDevice } from '@/composables/useDevice'

const emit = defineEmits<{
  rate: [rating: 'unknown' | 'fuzzy' | 'known']
  /** 空格:切换释义展开(手机外也支持) */
  reveal: []
}>()

const { isMobile } = useDevice()

/** 键盘事件:数字键评分,空格切换释义 */
function onKeydown(event: KeyboardEvent): void {
  // 输入框聚焦时不拦截
  const target = event.target as HTMLElement | null
  if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) return
  if (event.key === '1') emit('rate', 'unknown')
  else if (event.key === '2') emit('rate', 'fuzzy')
  else if (event.key === '3') emit('rate', 'known')
  else if (event.key === ' ') {
    event.preventDefault()
    emit('reveal')
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
.word-action-bar {
  display: flex;
  gap: 12px;
}

.word-action-bar__btn {
  flex: 1;
  min-height: 44px;
}

/* 手机:底部固定(10.13) */
.word-action-bar.is-mobile {
  position: sticky;
  bottom: 8px;
  z-index: 10;
  padding: 8px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  box-shadow: var(--lc-shadow-sm);
}

.word-action-bar__key {
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.7;
}
</style>