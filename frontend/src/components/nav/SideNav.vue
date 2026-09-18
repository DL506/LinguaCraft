<!-- 左侧二级导航(任务书 6.2):桌面常驻 200px / 平板抽屉复用;高亮 route.path 前缀匹配 -->
<template>
  <n-menu
    :collapsed="collapsed"
    :collapsed-width="64"
    :collapsed-icon-size="20"
    :value="activeKey"
    :options="menuOptions"
    @update:value="onMenuSelect"
  />
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon, NMenu, type MenuOption } from 'naive-ui'
import { childrenOfSection, navIconMap } from '@/router/nav'

defineProps<{ collapsed?: boolean }>()

const route = useRoute()
const router = useRouter()

/** 当前一级菜单的二级项 */
const menuOptions = computed<MenuOption[]>(() =>
  childrenOfSection(route.meta.section as string | undefined).map((item) => ({
    key: item.key,
    label: item.label,
    icon: item.icon
      ? () => h(NIcon, null, { default: () => h(navIconMap[item.icon as string]) })
      : undefined,
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