// 沉浸模式(按用户调整:答题页进入后不再默认沉浸,由用户在答题头部手动开启/退出)
// 状态为模块级单例:布局层只读消费(isImmersive),头部按钮调用 toggle 切换;
// body 类名同步由模块级 effect 统一完成;组件卸载自动复位,防止离开页面后导航仍被隐藏
// meta.immersive 语义调整:仅用于标记"支持沉浸"的答题页(决定头部是否显示切换按钮),不再自动生效
import { computed, onUnmounted, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

/** 沉浸是否开启(全局单例状态,默认关闭) */
const immersiveActive = ref(false)

/** body 类名同步(模块级单次注册,供全局样式钩子使用) */
watchEffect(() => {
  document.body.classList.toggle('immersive', immersiveActive.value)
})

export function useImmersive() {
  const route = useRoute()

  /** 当前路由是否支持沉浸的答题页(meta.immersive → 显示头部切换按钮) */
  const isImmersivePage = computed(() => !!route.meta.immersive)

  // 组件卸载时自动退出沉浸(离开答题页即恢复导航)
  onUnmounted(() => {
    immersiveActive.value = false
  })

  /** 开启沉浸:隐藏导航,只留练习头部;手机答题场景尝试锁竖屏(任务书第 16 章,失败静默) */
  function enableImmersive(): void {
    immersiveActive.value = true
    const orientation = window.screen?.orientation
    if (orientation && typeof orientation.lock === 'function') {
      orientation.lock('portrait').catch(() => {})
    }
  }

  /** 退出沉浸:恢复导航并解除竖屏锁定 */
  function disableImmersive(): void {
    immersiveActive.value = false
    window.screen?.orientation?.unlock?.()
  }

  /** 切换沉浸(答题头部按钮) */
  function toggleImmersive(): void {
    immersiveActive.value = !immersiveActive.value
  }

  return {
    /** 当前是否处于沉浸(默认 false,需手动开启) */
    isImmersive: computed(() => immersiveActive.value),
    /** 当前页是否支持沉浸(按钮显隐依据) */
    isImmersivePage,
    enableImmersive,
    disableImmersive,
    toggleImmersive,
  }
}