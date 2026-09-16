// 格式化工具:时间、日期、文案(全站共用)

/** 秒数 → mm:ss(计时器展示用) */
export function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/** 分钟数 → 中文时长文案(学习时长展示用) */
export function formatMinutes(minutes: number): string {
  if (minutes < 60) return `${minutes} 分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h} 小时 ${m} 分钟` : `${h} 小时`
}

/** 输入 Date 转为 yyyy-MM-dd HH:mm */
export function formatDateTime(input: string | Date): string {
  const d = typeof input === 'string' ? new Date(input) : input
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

/** 输入 Date 转为 yyyy-MM-dd */
export function formatDate(input: string | Date): string {
  const d = typeof input === 'string' ? new Date(input) : input
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}