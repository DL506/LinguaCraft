<!-- 个人中心共享布局(任务书 10.15):左资料卡 + 右 Tab 包裹子页
     P2 阶段仅提供路由出口,资料卡与 Tab 在 P12 实现 -->
<template>
  <div class="user-center">
    <router-view />
  </div>
</template>

<style scoped>
.user-center {
  min-height: 100%;
}
</style>