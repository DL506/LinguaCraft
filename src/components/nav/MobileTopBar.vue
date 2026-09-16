<!-- 手机顶部栏(任务书 6.2):Logo + 头像下拉(含切换主题,见 5.6 表) -->
<template>
  <div class="mobile-top-bar">
    <router-link to="/dashboard" class="mobile-top-bar__brand">
      <AppLogo />
    </router-link>
    <n-dropdown :options="menuOptions" trigger="click" @select="onMenuSelect">
      <n-avatar round size="small" class="mobile-top-bar__avatar">明</n-avatar>
    </n-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { NAvatar, NDropdown, type MenuOption } from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import { useAppStore } from '@/stores/app'
import { clearToken } from '@/utils/storage'

const router = useRouter()
const appStore = useAppStore()

/** 手机顶部下拉:切换主题 + 常用入口(任务书 5.6) */
const menuOptions = computed<MenuOption[]>(() => [
  { key: 'theme', label: '切换主题' },
  { key: 'stats', label: '学习统计' },
  { key: 'settings', label: '设置' },
  { key: 'divider', type: 'divider' },
  { key: 'logout', label: '退出登录' },
])

function onMenuSelect(key: string): void {
  if (key === 'theme') appStore.toggleTheme()
  else if (key === 'stats') router.push('/user/stats')
  else if (key === 'settings') router.push('/user/settings')
  else if (key === 'logout') {
    clearToken()
    router.push('/login')
  }
}
</script>

<style scoped>
.mobile-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 16px;
}

.mobile-top-bar__avatar {
  background-color: var(--lc-primary);
}
</style>