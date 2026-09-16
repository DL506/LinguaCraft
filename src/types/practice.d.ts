// 刷题域类型:题目来源、双类型(下发/完整)、通用结构、提交结果(任务书 11.1-11.6)
// 约定:所有 GET 题目接口不得下发 answer/explanation/acceptAlternatives 字段,
// 答案与解析只在 POST .../submit 返回的 SubmitDetail 中下发(设计原则 7)

/** 来源类型:real=真题,teacher=老师出的模拟卷,ai=AI 出题(任务书 11.1) */
export type SourceType = 'real' | 'teacher' | 'ai'

/** 题目来源附带信息:按来源类型取不同字段 */
export interface QuestionSource {
  type: SourceType
  meta: {
    /** 年份(真题) */
    year?: number
    /** 地区(真题) */
    region?: string
    /** 老师姓名(老师出题) */
    teacherName?: string
    /** 学校(老师出题) */
    school?: string
    /** 生成模型(AI 出题) */
    model?: string
    /** 生成时间(AI 出题) */
    generatedAt?: string
  }
}

/** 题型枚举:五种考试题型(任务书 11.3) */
export type PracticeType = 'reading' | 'matching' | 'cloze' | 'grammar-fill' | 'writing'

// ============ 下发用类型(ForAnswer,不含答案)============

/** 阅读理解下发结构 */
export interface ReadingQuestionForAnswer {
  id: string
  type: 'reading'
  source: QuestionSource
  /** 文章正文 */
  passage: string
  questions: {
    /** 题号(1 起,与题序一致) */
    id: number
    /** 题干 */
    stem: string
    /** 选项 */
    options: { key: string; text: string }[]
  }[]
}

/** 五选五下发结构 */
export interface MatchingQuestionForAnswer {
  id: string
  type: 'matching'
  source: QuestionSource
  /** 挖空后的文章,空位用 [[n]] 占位(任务书第 8 章) */
  passage: string
  blanks: { id: number }[]
  options: { key: string; text: string }[]
}

/** 完形填空下发结构 */
export interface ClozeQuestionForAnswer {
  id: string
  type: 'cloze'
  source: QuestionSource
  /** 挖空后的文章,空位用 [[n]] 占位 */
  passage: string
  blanks: {
    /** 空位编号(与占位符 n 一致) */
    id: number
    /** 该空的四个选项 */
    options: { key: string; text: string }[]
  }[]
}

/** 语法填空下发结构 */
export interface GrammarFillQuestionForAnswer {
  id: string
  type: 'grammar-fill'
  source: QuestionSource
  /** 挖空后的文章,空位用 [[n]] 占位 */
  passage: string
  blanks: {
    /** 空位编号(与占位符 n 一致) */
    id: number
    /** 提示词(无提示词填空则缺省) */
    hint?: string
  }[]
}

/** 作文下发结构 */
export interface WritingQuestionForAnswer {
  id: string
  type: 'writing'
  source: QuestionSource
  /** 写作题目与要求 */
  prompt: string
  /** 要点要求列表 */
  requirements: string[]
  /** 字数限制 */
  wordLimit: number
  /** 评分维度列表 */
  scoringDimensions: string[]
}

// ============ 完整类型(Full:后端内部 / mock 存储用,含答案)============

/** 阅读理解完整结构:题目追加 answer 与 explanation */
export interface ReadingQuestionFull extends ReadingQuestionForAnswer {
  questions: (ReadingQuestionForAnswer['questions'][number] & {
    /** 正确答案选项 key */
    answer: string
    /** 解析 */
    explanation: string
  })[]
}

/** 五选五完整结构:空位追加 answer 与 explanation */
export interface MatchingQuestionFull extends MatchingQuestionForAnswer {
  blanks: { id: number; answer: string; explanation: string }[]
}

/** 完形填空完整结构:空位追加 answer 与 explanation */
export interface ClozeQuestionFull extends ClozeQuestionForAnswer {
  blanks: (ClozeQuestionForAnswer['blanks'][number] & {
    /** 正确答案选项 key */
    answer: string
    /** 解析 */
    explanation: string
  })[]
}

/** 语法填空完整结构:空位追加 answer/acceptAlternatives/explanation */
export interface GrammarFillQuestionFull extends GrammarFillQuestionForAnswer {
  blanks: (GrammarFillQuestionForAnswer['blanks'][number] & {
    /** 标准答案 */
    answer: string
    /** 可接受的替代答案(大小写等变体) */
    acceptAlternatives?: string[]
    /** 解析 */
    explanation: string
  })[]
}

/** 作文完整结构:追加范文 */
export interface WritingQuestionFull extends WritingQuestionForAnswer {
  /** 范文 */
  sampleAnswer?: string
}

// ============ 通用题目结构(任务书 11.3)============

/** 下发类型映射:content 与 type 一一对应,实现端无需手动收窄类型 */
export type PracticeContentByType = {
  reading: ReadingQuestionForAnswer
  matching: MatchingQuestionForAnswer
  cloze: ClozeQuestionForAnswer
  'grammar-fill': GrammarFillQuestionForAnswer
  writing: WritingQuestionForAnswer
}

/** 完整类型映射(仅 mock 层 / 后端内部使用) */
export type PracticeFullByType = {
  reading: ReadingQuestionFull
  matching: MatchingQuestionFull
  cloze: ClozeQuestionFull
  'grammar-fill': GrammarFillQuestionFull
  writing: WritingQuestionFull
}

/** 通用题目结构:练习/智能练习/AI 出题返回的答卷单元 */
export interface PracticeItem<T extends PracticeType = PracticeType> {
  id: string
  type: T
  source: QuestionSource
  /** 难度 1-5 */
  difficulty: 1 | 2 | 3 | 4 | 5
  tags: string[]
  content: PracticeContentByType[T]
}

/** 列表项类型(任务书 11.4):来源列表复用,含提交状态 */
export interface PracticeListItem {
  id: string
  type: PracticeType
  source: QuestionSource
  title: string
  difficulty: 1 | 2 | 3 | 4 | 5
  tags: string[]
  /** 是否已有提交记录 */
  submitted: boolean
  submissionId?: string
  submissionStatus?: GradingStatus
  score?: number
  totalScore?: number
  lastSubmittedAt?: string
}

/** mock 数据存储结构:Full 版题目 + 列表元信息(仅 mocks/data 使用) */
export interface StoredPracticeItem<T extends PracticeType = PracticeType> {
  id: string
  title: string
  difficulty: 1 | 2 | 3 | 4 | 5
  tags: string[]
  content: PracticeFullByType[T]
}

// ============ 提交与批改(任务书 11.6)============

/** 批改方式:ai=AI 批改,auto=客观题数据库比对,manual=人工批改 */
export type GradingMethod = 'ai' | 'auto' | 'manual'

/** 批改状态 */
export type GradingStatus = 'graded' | 'partially_graded' | 'pending'

/** 练习提交请求(12.3) */
export interface PracticeSubmitRequest {
  /**
   * 各题型作答结构:
   * 阅读/完形:Record<题号, 选项key>;五选五:Record<空格id, 选项key>;语法填空:Record<空位id, 填空文本>
   * 写作:{ content: string }
   */
  answers: Record<number, string> | { content: string }
  /** 作答耗时(秒) */
  duration: number
  /** 本次提交指定批改方式;缺省时服务端读用户设置生效(12.7 说明) */
  gradingMethod?: 'ai' | 'manual'
}

/** 提交结果:所有 submit 接口统一返回该结构(验收 23) */
export interface SubmitResult {
  submissionId: string
  type: PracticeType
  /** 批改状态:graded 已批改 / partially_graded 部分批改 / pending 待批改 */
  status: GradingStatus
  /** 实际采用的批改方式 */
  gradingMethod: GradingMethod
  /** 客观题是否已批改 */
  objectiveGraded?: boolean
  /** 写作是否已批改 */
  writingGraded?: boolean
  score?: number
  totalScore?: number
  correctCount?: number
  totalCount?: number
  /** 逐题明细:仅在批改完成后下发(含答案与解析) */
  details?: SubmitDetail[]
  /** 预计出结果时间说明(待批改时返回) */
  estimatedTime?: string
}

/** 单题批改明细:答案与解析只在此下发 */
export interface SubmitDetail {
  /** 题号/空位编号 */
  id: number
  /** 用户作答 */
  userAnswer: string
  /** 正确答案 */
  correctAnswer: string
  /** 是否答对 */
  isCorrect: boolean
  /** 解析 */
  explanation: string
}

/** 写作题(type=writing)的提交结果:在 SubmitResult 基础上追加评分维度与范文 */
export interface WritingSubmitResult extends SubmitResult {
  /** 各评分维度得分 */
  dimensions?: { name: string; score: number; comment: string }[]
  /** 范文 */
  sampleAnswer?: string
  /** 总评 */
  overallComment?: string
}