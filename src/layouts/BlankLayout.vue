<!-- 空白布局:登录/注册/忘记密码/重置密码等公开页(任务书第 4 章 meta.layout=blank)
     同样注入 Naive UI 主题与消息提示,保证登录页双主题可用 -->
<template>
  <n-config-provider
    :theme="appStore.isDark ? darkTheme : null"
    :theme-overrides="appStore.isDark ? darkOverrides : lightOverrides"
  >
    <n-message-provider>
      <div class="blank-layout">
        <router-view />
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { darkTheme, NConfigProvider, NMessageProvider } from 'naive-ui'
import { darkOverrides, lightOverrides, useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style scoped>
.blank-layout {
  min-height: 100vh;
  background-color: var(--lc-bg);
}
</style>