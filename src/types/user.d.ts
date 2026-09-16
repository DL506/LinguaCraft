// 个人中心域类型:资料、统计、设置、提交记录、错题、收藏(任务书 11.8-11.10、12.7)
import type { GradingStatus, PracticeType, QuestionSource } from './practice'

/** 用户资料(12.7) */
export interface UserProfile {
  id: string
  username: string
  nickname: string
  avatar?: string
  /** 等级数值 */
  level: number
  /** 等级名称 */
  levelName: string
  /** 连续学习天数 */
  continuousDays: number
  /** 累计学习天数 */
  totalStudyDays: number
}

/** 学习统计(12.7) */
export interface UserStats {
  /** 累计学习时长(分钟) */
  totalStudyTime: number
  /** 累计背单词数 */
  totalWords: number
  /** 累计阅读篇数 */
  totalReading: number
  /** 总正确率(0-100) */
  accuracy: number
  /** 待批改数量(首页提示使用) */
  pendingGradingCount: number
  /** 各题型得分率(0-100) */
  typeAccuracy: {
    reading: number
    matching: number
    cloze: number
    grammar: number
    writing: number
  }
  /** 得分趋势(按天) */
  scoreTrend: {
    date: string
    reading: number
    matching: number
    cloze: number
    grammar: number
    writing: number
  }[]
  /** 各来源正确率(0-100,ai 来源使用过前为 null 语义,用 -1 表示无数据) */
  sourceAccuracy: { real: number; teacher: number; ai: number }
}

/** 用户设置(12.7;themeMode 不在本接口,主题偏好只存 localStorage,见任务书 5.3) */
export interface UserSettings {
  /** 每日背单词目标 */
  dailyWordGoal: number
  /** 每日阅读目标(篇) */
  dailyReadingGoal: number
  /** 发音口音 */
  accent: 'us' | 'uk'
  /** 语法填空判分是否区分大小写(服务端生效的判分偏好) */
  grammarFillCaseSensitive: boolean
  /** 批改方式偏好(服务端生效:submit 请求不重复携带) */
  gradingMethod: 'ai' | 'manual'
  /** 学习提醒时间 HH:mm */
  reminderTime?: string
}

/** 提交记录列表项(任务书 11.9) */
export interface SubmissionListItem {
  id: string
  type: PracticeType
  title: string
  source: QuestionSource
  status: GradingStatus
  score?: number
  totalScore?: number
  submittedAt: string
  estimatedTime?: string
}

/** 错题(任务书 11.8) */
export interface MistakeItem {
  id: string
  /** 来源题目 id */
  questionId: string
  type: PracticeType
  source: QuestionSource
  /** 题干摘要(如 "What is the main idea?") */
  stem: string
  /** 用户作答 */
  userAnswer: string
  /** 正确答案 */
  correctAnswer: string
  /** 解析 */
  explanation: string
  createdAt: string
}

/** 收藏(任务书 11.10):支持单词与句子两种类型 */
export interface FavoriteItem {
  id: string
  type: 'word' | 'sentence'
  content: string
  /** 来源引用 id(题目/单词) */
  refId?: string
  createdAt: string
}