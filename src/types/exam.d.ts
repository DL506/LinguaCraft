// 真题模考域类型:整卷结构、提交与成绩报告(任务书 11.7、12.4;阅读部分契约调整见下)
import type {
  ClozeQuestionForAnswer,
  ClozeQuestionFull,
  GrammarFillQuestionForAnswer,
  GrammarFillQuestionFull,
  MatchingQuestionForAnswer,
  MatchingQuestionFull,
  PracticeType,
  ReadingQuestionForAnswer,
  ReadingQuestionFull,
  WritingQuestionForAnswer,
  WritingQuestionFull,
} from './practice'

/** 阅读部分内容(契约调整):整卷阅读为 3 篇,每篇 5 题,题号全局连续 1-15 */
export type ReadingPaperContent = ReadingQuestionForAnswer[]

/** 阅读部分完整内容(仅 mock 层存储) */
export type ReadingPaperContentFull = ReadingQuestionFull[]

/** 整卷结构(下发版:content 为不含答案的下发类型) */
export interface ExamPaper {
  id: string
  /** 年份 */
  year: number
  title: string
  /** 卷面总分(100) */
  totalScore: number
  /** 考试时长(分钟,120) */
  duration: number
  /** 五个作答部分,part 从 1 开始 */
  sections: ExamSection[]
}

/** 整卷中的一个作答部分 */
export interface ExamSection {
  /** 部分序号:1-阅读,2-五选五,3-完形,4-语法填空,5-作文 */
  part: number
  type: PracticeType
  /** 该部分卷面分值 */
  score: number
  content:
    | ReadingPaperContent
    | MatchingQuestionForAnswer
    | ClozeQuestionForAnswer
    | GrammarFillQuestionForAnswer
    | WritingQuestionForAnswer
}

/** 完整类型联合(仅 mock 层存储,含答案) */
export type ExamSectionContentFull =
  | ReadingPaperContentFull
  | MatchingQuestionFull
  | ClozeQuestionFull
  | GrammarFillQuestionFull
  | WritingQuestionFull

/** 整卷完整结构(mock 存储版) */
export interface ExamPaperFull {
  id: string
  year: number
  title: string
  totalScore: number
  duration: number
  sections: { part: number; type: PracticeType; score: number; content: ExamSectionContentFull }[]
}

/** 模考列表项(12.4) */
export interface ExamListItem {
  id: string
  year: number
  title: string
  totalScore: number
  duration: number
  /** 最近一次得分(无则缺省) */
  lastScore?: number
  lastSubmittedAt?: string
}

/** 整卷交卷请求(12.4) */
export interface ExamSubmitRequest {
  /**
   * 客观题作答:key 为答题卡全局编号(1~45,见任务书 11.7 编号映射规则),
   * value 为选项 key(五选五)或填空文本(语法填空)
   */
  objective: Record<number, string>
  /** 作文作答 */
  writing?: { content: string }
  /** 答题耗时(秒) */
  duration: number
  /** 本次提交指定批改方式;缺省时服务端读用户设置生效 */
  gradingMethod?: 'ai' | 'manual'
}

/** 模考成绩报告(12.4) */
export interface ExamReport {
  score: number
  totalScore: number
  /** 各部分得分 */
  sections: { part: number; type: PracticeType; score: number; total: number }[]
  /** 薄弱项描述 */
  weakPoints: string[]
  /** 历年成绩对比 */
  compareWithHistory: { year: number; score: number }[]
  /** 客观题是否已批改 */
  objectiveGraded: boolean
  /** 写作是否已批改 */
  writingGraded: boolean
  /** 写作得分(写作已批改时返回) */
  writingScore?: number
}