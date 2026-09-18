// 全局应用 store:双主题(权威源 localStorage,任务书 5.3)、侧栏折叠、抽屉开合
// 同时导出 Naive UI 亮/暗主题覆盖(任务书 5.2),AppLayout 与 BlankLayout 共用
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { applyHighlightTheme } from '@/composables/useHighlightTheme'

export type ThemeMode = 'light' | 'dark' | 'system'

/** Naive UI 亮色主题覆盖(任务书 5.2):亮蓝色调 */
export const lightOverrides = {
  common: {
    primaryColor: '#3B82F6',
    primaryColorHover: '#60A5FA',
    primaryColorPressed: '#2563EB',
    primaryColorSuppl: '#60A5FA',
    borderRadius: '10px',
    fontSize: '14px',
    bodyColor: '#F8FAFC',
    cardColor: '#FFFFFF',
    modalColor: '#FFFFFF',
    popoverColor: '#FFFFFF',
    borderColor: '#E2E8F0',
    textColorBase: '#0F172A',
  },
}

/** Naive UI 暗色主题覆盖(任务书 5.2):暗蓝色调 */
export const darkOverrides = {
  common: {
    primaryColor: '#60A5FA',
    primaryColorHover: '#93C5FD',
    primaryColorPressed: '#3B82F6',
    primaryColorSuppl: '#93C5FD',
    borderRadius: '10px',
    fontSize: '14px',
    bodyColor: '#0B1120',
    cardColor: '#111827',
    modalColor: '#1E293B',
    popoverColor: '#1E293B',
    borderColor: '#1E293B',
    textColorBase: '#F1F5F9',
  },
}

export const useAppStore = defineStore('app', () => {
  /** 主题偏好:light / dark / system,三态(任务书 5.3) */
  const themeMode = ref<ThemeMode>('system')
  /** 系统是否偏好暗色(themeMode=system 时生效) */
  const systemPrefersDark = ref(false)
  /** 桌面侧栏是否折叠 */
  const siderCollapsed = ref(false)
  /** 平板抽屉是否打开 */
  const drawerOpen = ref(false)

  /** 当前是否为暗色 */
  const isDark = computed(() => {
    if (themeMode.value === 'system') return systemPrefersDark.value
    return themeMode.value === 'dark'
  })

  /** 初始化主题:读 localStorage(lc-theme),监听系统偏好,应用主题(main.ts 调用一次) */
  function initTheme(): void {
    const saved = localStorage.getItem('lc-theme') as ThemeMode | null
    if (saved) themeMode.value = saved
    const mql = window.matchMedia('(prefers-color-scheme: dark)')
    systemPrefersDark.value = mql.matches
    const onChange = (e: MediaQueryListEvent) => {
      systemPrefersDark.value = e.matches
    }
    if (mql.addEventListener) mql.addEventListener('change', onChange)
    else if (mql.addListener) mql.addListener(onChange)
    applyTheme()
  }

  /** 设置主题模式:写回 localStorage,立即应用 */
  function setThemeMode(mode: ThemeMode): void {
    themeMode.value = mode
    localStorage.setItem('lc-theme', mode)
    applyTheme()
  }

  /** 切换亮/暗(顶栏按钮,任务书 5.6) */
  function toggleTheme(): void {
    setThemeMode(isDark.value ? 'light' : 'dark')
  }

  /** 应用主题:html.dark class + highlight.js 双主题切换(任务书 5.3/5.4) */
  function applyTheme(): void {
    document.documentElement.classList.toggle('dark', isDark.value)
    applyHighlightTheme(isDark.value)
  }

  return {
    themeMode,
    systemPrefersDark,
    siderCollapsed,
    drawerOpen,
    isDark,
    initTheme,
    setThemeMode,
    toggleTheme,
    applyTheme,
  }
})