<!-- 主布局(任务书 6.4 骨架):三端导航形态切换 + 沉浸模式 + Naive UI 主题注入 -->
<template>
  <n-config-provider
    :theme="appStore.isDark ? darkTheme : null"
    :theme-overrides="appStore.isDark ? darkOverrides : lightOverrides"
  >
    <n-message-provider>
      <n-dialog-provider>
        <n-layout position="absolute">
          <!-- 桌面/平板:顶部一级导航(沉浸模式隐藏) -->
          <n-layout-header v-if="!isMobile && !isImmersive" bordered style="height: 56px">
            <TopNav :is-tablet="isTablet" />
          </n-layout-header>

          <!-- 桌面:左侧二级导航常驻 200px,可折叠 -->
          <n-layout-sider
            v-if="isDesktop && !isImmersive"
            bordered
            collapse-mode="width"
            :width="200"
            :collapsed-width="64"
            :collapsed="appStore.siderCollapsed"
            show-trigger
            @collapse="appStore.siderCollapsed = true"
            @expand="appStore.siderCollapsed = false"
          >
            <SideNav :collapsed="appStore.siderCollapsed" />
          </n-layout-sider>

          <!-- 平板:左侧抽屉二级导航 -->
          <NavDrawer v-if="isTablet" />

          <!-- 手机:顶部栏 -->
          <n-layout-header v-if="isMobile && !isImmersive" bordered style="height: 48px">
            <MobileTopBar />
          </n-layout-header>

          <!-- 手机:二级横向 Tab -->
          <MobileSubTabs v-if="isMobile && !isImmersive" />

          <!-- 主内容区 -->
          <n-layout-content :content-style="contentStyle" :native-scrollbar="false">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </n-layout-content>

          <!-- 手机:底部一级 Tab(沉浸模式隐藏,任务书 6.2) -->
          <n-layout-footer v-if="isMobile && !isImmersive" class="mobile-tabbar">
            <MobileTabBar />
          </n-layout-footer>
        </n-layout>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
// 布局分支:桌面侧栏常驻 / 平板抽屉 / 手机顶栏+底面 Tab(任务书 6.2/6.4)
import { computed } from 'vue'
import { darkTheme, NConfigProvider, NDialogProvider, NLayout, NLayoutContent, NLayoutFooter, NLayoutHeader, NLayoutSider, NMessageProvider } from 'naive-ui'
import TopNav from '@/components/nav/TopNav.vue'
import SideNav from '@/components/nav/SideNav.vue'
import NavDrawer from '@/components/nav/NavDrawer.vue'
import MobileTopBar from '@/components/nav/MobileTopBar.vue'
import MobileSubTabs from '@/components/nav/MobileSubTabs.vue'
import MobileTabBar from '@/components/nav/MobileTabBar.vue'
import { darkOverrides, lightOverrides, useAppStore } from '@/stores/app'
import { useDevice } from '@/composables/useDevice'
import { useImmersive } from '@/composables/useImmersive'

const appStore = useAppStore()
const { isMobile, isTablet, isDesktop } = useDevice()
const { isImmersive } = useImmersive()

/** 主内容区样式(任务书 6.4):手机 16px / 其他 24px,桌面 1200px 居中
    手机沉浸态无底部 Tab,不再预留 56px 底部空间 */
const contentStyle = computed(() => {
  const mobileBottom = isImmersive.value
    ? '16px'
    : 'calc(56px + env(safe-area-inset-bottom) + 16px)'
  return {
    padding: isMobile.value ? '16px' : '24px',
    paddingBottom: isMobile.value ? mobileBottom : '24px',
    maxWidth: isDesktop.value ? '1200px' : '100%',
    margin: '0 auto',
  }
})
</script>

<style scoped>
/* 布局容器:绝对定位铺满视口,滚动交给 n-layout-content */
.n-layout {
  height: 100vh;
}

.mobile-tabbar {
  height: 56px;
}

/* 路由淡入淡出过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>