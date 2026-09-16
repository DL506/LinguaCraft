// 背单词域类型:单词与词书(任务书 11.11、12.5)

/** 单词分层:高频 / 核心 / 认知(任务书 10.13) */
export type WordLevel = 'high' | 'core' | 'cognitive'

/** 单词结构 */
export interface Word {
  id: string
  /** 拼写 */
  spelling: string
  /** 音标 */
  phonetic: string
  /** 词性 */
  partOfSpeech: string
  /** 释义列表 */
  meanings: string[]
  /** 例句 */
  exampleSentence?: string
  /** 例句翻译 */
  exampleTranslation?: string
  /** 真题原句(任务书 10.13:背单词时展示历年真题原句) */
  realExamSentence?: string
  /** 真题出处(年份+题型) */
  realExamSource?: string
  /** 所属分层 */
  level: WordLevel
  /** 发音音频地址 */
  audioUrl?: string
}

/** 词书(12.5) */
export interface VocabBook {
  id: string
  name: string
  /** 词书总词数 */
  total: number
  /** 已学词数 */
  learned: number
}

/** 背单词复习请求(12.5):rating 为记忆自评 */
export interface VocabReviewRequest {
  wordId: string
  rating: 'unknown' | 'fuzzy' | 'known'
}

/** 今日背单词任务(12.5) */
export interface VocabTodayTask {
  /** 新学词数 */
  newCount: number
  /** 待复习词数 */
  reviewCount: number
  /** 今日已完成数 */
  doneCount: number
  /** 今日目标 */
  target: number
}