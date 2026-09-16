<!-- 主题切换按钮(任务书 5.6):桌面/平板顶栏太阳/月亮 -->
<template>
  <n-button quaternary circle :aria-label="isDark ? '切换到亮色主题' : '切换到暗色主题'" @click="appStore.toggleTheme()">
    <template #icon>
      <n-icon :component="isDark ? SunnyOutline : MoonOutline" />
    </template>
  </n-button>
</template>

<script setup lang="ts">
// 图标:月亮=当前亮色(点击进入暗色),太阳=当前暗色(点击进入亮色)
import { MoonOutline, SunnyOutline } from '@vicons/ionicons5'
import { NButton, NIcon } from 'naive-ui'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const { isDark } = appStore
</script>