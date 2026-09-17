<!-- 顶部一级导航(任务书 6.2):桌面/平板;Logo 点击回首页 -->
<template>
  <div class="top-nav">
    <router-link to="/dashboard" class="top-nav__brand">
      <AppLogo />
    </router-link>

    <n-menu
      class="top-nav__menu"
      mode="horizontal"
      :value="activeSection"
      :options="menuOptions"
      @update:value="onMenuSelect"
    />

    <div class="top-nav__actions">
      <n-button v-if="isTablet" quaternary circle @click="appStore.drawerOpen = true">
        <template #icon><n-icon :component="MenuOutline" /></template>
      </n-button>
      <ThemeToggle />
      <n-dropdown trigger="hover" :options="userOptions" @select="onUserSelect">
        <n-avatar round size="small" class="top-nav__avatar">{{ avatarText }}</n-avatar>
      </n-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
// 一级菜单高亮:route.meta.section(任务书 6.3 高亮规则)
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MenuOutline } from '@vicons/ionicons5'
import { NAvatar, NButton, NDropdown, NIcon, NMenu, type MenuOption } from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { navConfig, navIconMap } from '@/router/nav'
import { clearToken } from '@/utils/storage'

defineProps<{ isTablet?: boolean }>()

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

/** 头像文字:昵称首字,未登录取演示昵称首字 */
const avatarText = computed(() => (userStore.nickname ? userStore.nickname[0] : '明'))

/** 当前高亮的一级菜单 key */
const activeSection = computed(() => (route.meta.section as string | undefined) ?? null)

/** 一级菜单选项(点击进入该一级下的首个页面) */
const menuOptions = computed<MenuOption[]>(() =>
  navConfig.map((item) => ({
    key: item.key,
    label: item.label,
    icon: () => h(NIcon, null, { default: () => h(navIconMap[item.icon]) }),
  }))
)

/** 一级菜单点击:进入该一级下的首个页面 */
function onMenuSelect(key: string): void {
  const parent = navConfig.find((item) => item.key === key)
  if (parent?.children?.[0]) router.push(parent.children[0].key)
}

/** 头像下拉菜单 */
const userOptions = computed<MenuOption[]>(() => [
  { key: 'stats', label: '学习统计' },
  { key: 'settings', label: '设置' },
  { key: 'divider', type: 'divider' },
  { key: 'logout', label: '退出登录' },
])

function onUserSelect(key: string): void {
  if (key === 'stats') router.push('/user/stats')
  else if (key === 'settings') router.push('/user/settings')
  else if (key === 'logout') {
    clearToken()
    router.push('/login')
  }
}
</script>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
  padding: 0 24px;
}

.top-nav__brand {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.top-nav__menu {
  flex: 1;
  min-width: 0;
}

.top-nav__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.top-nav__avatar {
  cursor: pointer;
  background-color: var(--lc-primary);
}
</style>