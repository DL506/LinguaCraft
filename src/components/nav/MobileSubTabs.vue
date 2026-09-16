<!-- 手机二级横向 Tab(任务书 6.2):顶部横向 Tab,按当前 section 展示子项 -->
<template>
  <div class="mobile-sub-tabs">
    <n-menu mode="horizontal" :value="activeKey" :options="menuOptions" @update:value="onMenuSelect" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NMenu, type MenuOption } from 'naive-ui'
import { childrenOfSection } from '@/router/nav'

const route = useRoute()
const router = useRouter()

/** 按当前 section 生成二级项 */
const menuOptions = computed<MenuOption[]>(() =>
  childrenOfSection(route.meta.section as string | undefined).map((item) => ({
    key: item.key,
    label: item.label,
  }))
)

/** 高亮:route.path 前缀匹配(任务书 6.3) */
const activeKey = computed(() => {
  const matched = menuOptions.value.find((opt) => route.path.startsWith(String(opt.key)))
  return matched ? (matched.key as string) : null
})

function onMenuSelect(key: string): void {
  router.push(key)
}
</script>

<style scoped>
.mobile-sub-tabs {
  border-bottom: 1px solid var(--lc-border);
  background-color: var(--lc-bg-card);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.mobile-sub-tabs :deep(.n-menu) {
  min-width: max-content;
}
</style>