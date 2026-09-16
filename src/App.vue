<!-- 应用根组件:仅渲染路由出口;布局按路由结构分发(公开页走 BlankLayout,业务页走 AppLayout) -->
<template>
  <router-view />
</template>