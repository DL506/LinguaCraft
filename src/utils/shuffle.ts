// 洗牌工具:Fisher-Yates 算法,用于打乱单词卡 / 选项顺序(不修改原数组)

/** 返回打乱顺序后的新数组 */
export function shuffle<T>(list: readonly T[]): T[] {
  const arr = [...list]
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}