// 三端断点(任务书 6.1):桌面 ≥1024 / 平板 768~1023 / 手机 <768,三端共用一套代码
import { useMediaQuery } from '@vueuse/core'

export function useDevice() {
  const isMobile = useMediaQuery('(max-width: 767px)')
  const isTablet = useMediaQuery('(min-width: 768px) and (max-width: 1023px)')
  const isDesktop = useMediaQuery('(min-width: 1024px)')
  return { isMobile, isTablet, isDesktop }
}