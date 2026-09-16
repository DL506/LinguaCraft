<!-- 手机底部一级 Tab(任务书 6.2):首页/刷题/学习/我的,含安全区适配(第 16 章) -->
<template>
  <div class="mobile-tab-bar">
    <router-link
      v-for="tab in tabs"
      :key="tab.key"
      :to="tab.path"
      class="mobile-tab-bar__item"
      :class="{ 'is-active': activeKey === tab.key }"
    >
      <n-icon :component="navIconMap[tab.icon]" size="20" />
      <span class="mobile-tab-bar__label">{{ tab.label }}</span>
    </router-link>
  </div>
</template>

<script setup lang="ts">
// 底部 Tab 高亮:刷题/学习/我的按 route.meta.section,首页用 home 特判(任务书 6.3)
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import { mobileTabs, navIconMap } from '@/router/nav'

const route = useRoute()

const tabs = mobileTabs

const activeKey = computed(() => {
  const section = route.meta.section as string | undefined
  if (route.path.startsWith('/dashboard')) return 'home'
  return section ?? null
})
</script>

<style scoped>
.mobile-tab-bar {
  display: flex;
  height: 56px;
  /* 底部 Tab 加安全区(任务书第 16 章) */
  padding-bottom: env(safe-area-inset-bottom);
  background-color: var(--lc-bg-card);
  border-top: 1px solid var(--lc-border);
}

.mobile-tab-bar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--lc-text-3);
}

.mobile-tab-bar__item.is-active {
  color: var(--lc-primary);
}

.mobile-tab-bar__label {
  font-size: 11px;
}
</style>