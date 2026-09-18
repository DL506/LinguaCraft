// 滑动手势 composable(任务书 10.13):背单词手机端左滑跳过、右滑收藏、上滑显示答案
import { onUnmounted, type Ref } from 'vue'

export type SwipeDirection = 'left' | 'right' | 'up' | 'down'

/** 手势判定阈值(px):滑动距离超过该值才视为有效手势 */
const SWIPE_THRESHOLD = 50

export function useSwipe(
  target: Ref<HTMLElement | undefined>,
  onSwipe: (direction: SwipeDirection) => void
): { bind: () => void; unbind: () => void } {
  let startX = 0
  let startY = 0

  function onTouchStart(event: TouchEvent): void {
    const touch = event.touches[0]
    startX = touch.clientX
    startY = touch.clientY
  }

  function onTouchEnd(event: TouchEvent): void {
    const touch = event.changedTouches[0]
    const deltaX = touch.clientX - startX
    const deltaY = touch.clientY - startY
    if (Math.abs(deltaX) < SWIPE_THRESHOLD && Math.abs(deltaY) < SWIPE_THRESHOLD) return
    if (Math.abs(deltaX) > Math.abs(deltaY)) {
      onSwipe(deltaX > 0 ? 'right' : 'left')
    } else {
      onSwipe(deltaY > 0 ? 'down' : 'up')
    }
  }

  function bind(): void {
    const el = target.value
    if (!el) return
    el.addEventListener('touchstart', onTouchStart, { passive: true })
    el.addEventListener('touchend', onTouchEnd, { passive: true })
  }

  function unbind(): void {
    const el = target.value
    if (!el) return
    el.removeEventListener('touchstart', onTouchStart)
    el.removeEventListener('touchend', onTouchEnd)
  }

  onUnmounted(unbind)

  return { bind, unbind }
}