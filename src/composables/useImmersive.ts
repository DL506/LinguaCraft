// 沉浸模式(任务书 6.5):所有答题页进入沉浸,隐藏导航,只留练习头部
import { computed, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

export function useImmersive() {
  const route = useRoute()
  const isImmersive = computed(() => !!route.meta.immersive)
  watchEffect(() => {
    document.body.classList.toggle('immersive', isImmersive.value)
  })
  return { isImmersive }
}