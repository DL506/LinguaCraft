<!-- 平板抽屉容器(任务书 14 组件清单):装载 SideNav,开合状态存 app store -->
<template>
  <n-drawer v-model:show="appStore.drawerOpen" placement="left" :width="240" display-directive="show">
    <SideNav :collapsed="false" />
  </n-drawer>
</template>

<script setup lang="ts">
import { NDrawer } from 'naive-ui'
import SideNav from '@/components/nav/SideNav.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>