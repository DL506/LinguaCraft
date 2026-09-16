// highlight.js 双主题切换(任务书 5.4):两套本地 CSS 同时引入,运行时用 disabled 属性切换
export function applyHighlightTheme(isDark: boolean): void {
  const light = document.getElementById('hljs-light') as HTMLLinkElement | null
  const dark = document.getElementById('hljs-dark') as HTMLLinkElement | null
  if (light) light.disabled = isDark
  if (dark) dark.disabled = !isDark
}