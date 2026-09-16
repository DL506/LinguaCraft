<!-- Logo:图标 + 应用名(任务书 14 组件清单,三端一致) -->
<template>
  <div class="app-logo">
    <img src="@/assets/logo.svg" alt="LinguaCraft" class="app-logo__img" />
    <span class="app-logo__name">LinguaCraft</span>
  </div>
</template>

<style scoped>
.app-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-logo__img {
  width: 28px;
  height: 28px;
}

.app-logo__name {
  font-size: 16px;
  font-weight: 700;
  color: var(--lc-text-1);
}
</style>