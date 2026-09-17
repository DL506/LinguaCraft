// 沉浸模式(任务书 6.5):所有答题页进入沉浸,隐藏导航,只留练习头部
// 增强(检查修复):练习页头部提供"显示/隐藏导航"切换;用户切出后为会话级延续(内存态,刷新重置)
import { computed, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

/** 用户是否主动切出了沉浸(模块级共享状态:AppLayout 与练习页头部同步响应) */
const userExitedImmersive = ref(false)

export function useImmersive() {
  const route = useRoute()
  /** 路由是否为沉浸型页面(meta.immersive,如五种题型页与模考) */
  const isImmersivePage = computed(() => !!route.meta.immersive)
  /** 最终沉浸态:沉浸型页面 且 用户未主动切出 */
  const isImmersive = computed(() => isImmersivePage.value && !userExitedImmersive.value)

  // body class 同步:多组件各自建立 watchEffect,写入同一 class,幂等安全
  watchEffect(() => {
    document.body.classList.toggle('immersive', isImmersive.value)
  })

  /** 切换导航显隐:会话级延续,直到用户切回或刷新页面 */
  function toggleImmersive(): void {
    userExitedImmersive.value = !userExitedImmersive.value
  }

  return { isImmersive, isImmersivePage, userExitedImmersive, toggleImmersive }
}