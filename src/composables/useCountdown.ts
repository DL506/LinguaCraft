// 倒计时 composable(任务书 10.6):答题页计时展示,remaining 归零触发超时自动提交(页面层监听)
import { computed, onUnmounted, ref } from 'vue'

export function useCountdown(initialSeconds = 0) {
  /** 计时总长(秒) */
  const total = ref(initialSeconds)
  /** 已过秒数 */
  const elapsed = ref(0)
  /** 是否计时中 */
  const isRunning = ref(false)
  /** 剩余秒数 */
  const remaining = computed(() => Math.max(0, total.value - elapsed.value))

  let timer: number | undefined

  function start(): void {
    if (timer !== undefined) return
    isRunning.value = true
    timer = window.setInterval(() => {
      elapsed.value++
      if (remaining.value <= 0) stop()
    }, 1000)
  }

  function pause(): void {
    if (timer !== undefined) {
      window.clearInterval(timer)
      timer = undefined
    }
    isRunning.value = false
  }

  /** 停止计时(pause 语义一致,保留独立命名便于页面调用) */
  function stop(): void {
    pause()
    elapsed.value = total.value
  }

  /** 重置并重新开始(如刷新一批新题) */
  function reset(seconds = initialSeconds): void {
    pause()
    elapsed.value = 0
    total.value = seconds
  }

  onUnmounted(() => pause())

  return { total, elapsed, isRunning, remaining, start, pause, stop, reset }
}