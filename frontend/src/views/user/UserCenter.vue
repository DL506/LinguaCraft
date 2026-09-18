<!-- 个人中心共享布局(任务书 10.15):桌面 左资料卡 280px + 右 Tab;平板 顶部资料卡 + Tab;手机 顶部资料卡 + 横向可滚动 Tab + 卡片流
     子页经路由渲染在右侧/下方 -->
<template>
  <div class="user-center" :class="{ 'is-wide': isDesktop }">
    <!-- 资料卡 -->
    <aside class="user-center__profile">
      <n-avatar round :size="isMobile ? 48 : 64" class="user-center__avatar">
        {{ avatarText }}
      </n-avatar>
      <div class="user-center__info">
        <p class="user-center__nickname">{{ profile?.nickname ?? '...' }}</p>
        <n-tag size="small" :bordered="false" type="info">
          Lv.{{ profile?.level ?? '-' }} {{ profile?.levelName ?? '' }}
        </n-tag>
        <p class="user-center__days">
          连续学习 {{ profile?.continuousDays ?? 0 }} 天 · 累计 {{ profile?.totalStudyDays ?? 0 }} 天
        </p>
      </div>
    </aside>

    <!-- 右侧/下方:Tab + 子页 -->
    <div class="user-center__main">
      <nav class="user-center__tabs">
        <router-link
          v-for="tab in tabs"
          :key="tab.path"
          :to="tab.path"
          class="user-center__tab"
          :class="{ 'is-active': route.path.startsWith(tab.path) }"
        >
          {{ tab.label }}
        </router-link>
      </nav>
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
// 资料卡数据:GET /user/profile(12.7);Tab 与路由 children 一一对应
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { NAvatar, NTag } from 'naive-ui'
import { useDevice } from '@/composables/useDevice'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const { isMobile, isDesktop } = useDevice()
const userStore = useUserStore()

/** 资料卡显示数据(优先 store 缓存) */
const profile = computed(() => userStore.profile)

/** 头像文字:昵称首字 */
const avatarText = computed(() => (profile.value?.nickname ? profile.value.nickname[0] : '我'))

/** Tab 项(与 10.15 表格一致) */
const tabs = [
  { path: '/user/stats', label: '学习统计' },
  { path: '/user/submissions', label: '我的提交' },
  { path: '/user/books', label: '我的词书' },
  { path: '/user/mistakes', label: '错题本' },
  { path: '/user/favorites', label: '收藏' },
  { path: '/user/settings', label: '设置' },
]

onMounted(() => {
  // 资料卡数据(已缓存则跳过)
  if (!userStore.profile) {
    void userStore.fetchProfile()
  }
})
</script>

<style scoped>
/* 默认单列(平板/手机:资料卡在上);仅桌面双列 280px + 内容 */
.user-center {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
  align-items: start;
  min-width: 0;
}

.user-center.is-wide {
  grid-template-columns: 280px minmax(0, 1fr);
}

.user-center__profile {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.user-center__avatar {
  background-color: var(--lc-primary);
  flex-shrink: 0;
}

.user-center__info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  min-width: 0;
}

.user-center__nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--lc-text-1);
}

.user-center__days {
  font-size: 12px;
  color: var(--lc-text-3);
}

.user-center__main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

/* Tab:横向可滚动(手机) */
.user-center__tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
}

.user-center__tab {
  flex-shrink: 0;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--lc-text-2);
  background-color: var(--lc-bg-card);
  min-height: 36px;
  line-height: 20px;
}

.user-center__tab:hover {
  color: var(--lc-primary);
}

.user-center__tab.is-active {
  color: #fff;
  background-color: var(--lc-primary);
}
</style>