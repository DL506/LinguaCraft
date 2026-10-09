<!-- 后台管理端布局(阶段 A1):独立于学生端 AppLayout,避免把管理菜单混进任务书定义的三端导航 -->
<template>
  <n-config-provider
    :theme="appStore.isDark ? darkTheme : null"
    :theme-overrides="appStore.isDark ? darkOverrides : lightOverrides"
  >
    <n-message-provider>
      <n-layout position="absolute">
        <n-layout-header bordered class="admin-header">
          <div class="admin-header__brand">
            <AppLogo />
            <n-tag size="small" type="info" :bordered="false">管理后台</n-tag>
          </div>

          <div class="admin-header__actions">
            <n-button quaternary size="small" @click="router.push('/dashboard')">
              <template #icon>
                <n-icon :component="ArrowBackOutline" />
              </template>
              返回学生端
            </n-button>
            <ThemeToggle />
            <n-dropdown trigger="hover" :options="userOptions" @select="onUserSelect">
              <n-avatar round size="small" class="admin-header__avatar">{{ avatarText }}</n-avatar>
            </n-dropdown>
          </div>
        </n-layout-header>

        <n-layout has-sider style="height: calc(100% - 56px)">
          <!-- 左侧管理菜单:条目来自 router/nav.ts 的 adminNav(随阶段 A2~A5 逐次补齐) -->
          <n-layout-sider
            bordered
            collapse-mode="width"
            :width="200"
            :collapsed-width="64"
            :collapsed="appStore.siderCollapsed"
            show-trigger
            @collapse="appStore.siderCollapsed = true"
            @expand="appStore.siderCollapsed = false"
          >
            <n-menu
              :collapsed="appStore.siderCollapsed"
              :collapsed-width="64"
              :collapsed-icon-size="20"
              :value="activeKey"
              :options="menuOptions"
              @update:value="onMenuSelect"
            />
          </n-layout-sider>

          <n-layout-content :native-scrollbar="false">
            <div class="admin-content">
              <router-view />
            </div>
          </n-layout-content>
        </n-layout>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
// 后台布局要点:
// 1) 主题与学生端共用 app store(localStorage 权威源),后台同样支持亮暗双主题;
// 2) 导航只从 adminNav 取,学生端导航项不出现在后台,反之亦然;
// 3) 入口可见性与路由守卫在前端,真正的权限边界是后端 /api/admin/** 的角色校验。
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import {
  darkTheme,
  NAvatar,
  NButton,
  NConfigProvider,
  NDropdown,
  NIcon,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  NMessageProvider,
  NTag,
  type MenuOption,
} from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { darkOverrides, lightOverrides, useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { adminNav, childrenOfSection, navIconMap } from '@/router/nav'
import { clearToken } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

/** 头像文字:管理员昵称首字 */
const avatarText = computed(() => (userStore.nickname ? userStore.nickname[0] : '管'))

/** 管理端二级菜单(按阶段增长) */
const menuOptions = computed<MenuOption[]>(() =>
  childrenOfSection(adminNav.key).map((item) => ({
    key: item.key,
    label: item.label,
    icon: item.icon
      ? () => h(NIcon, null, { default: () => h(navIconMap[item.icon as string]) })
      : undefined,
  }))
)

/** 高亮:当前路径前缀匹配菜单 key */
const activeKey = computed(
  () => menuOptions.value.find((opt) => route.path.startsWith(String(opt.key)))?.key ?? null
)

function onMenuSelect(key: string): void {
  router.push(key)
}

const userOptions: MenuOption[] = [{ key: 'logout', label: '退出登录' }]

function onUserSelect(key: string): void {
  if (key === 'logout') {
    clearToken()
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 100%;
  padding: 0 24px;
}

.admin-header__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.admin-header__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.admin-header__avatar {
  cursor: pointer;
  background-color: var(--lc-primary);
}

/* 后台内容区:比学生端更宽一些,便于放表格 */
.admin-content {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
}
</style>
