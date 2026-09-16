// Full → ForAnswer 答案过滤工具(任务书 13.4)
// 设计原则 7:所有 GET 题目接口不得下发答案与解析,handler 返回前统一经本文件过滤
import type {
  ClozeQuestionForAnswer,
  ClozeQuestionFull,
  GrammarFillQuestionForAnswer,
  GrammarFillQuestionFull,
  MatchingQuestionForAnswer,
  MatchingQuestionFull,
  PracticeContentByType,
  PracticeFullByType,
  PracticeItem,
  PracticeType,
  ReadingQuestionForAnswer,
  ReadingQuestionFull,
  StoredPracticeItem,
  WritingQuestionForAnswer,
  WritingQuestionFull,
} from '@/types/practice'
import type {
  ExamPaper,
  ExamPaperFull,
  ExamSectionContentFull,
} from '@/types/exam'

/** 阅读理解:剥离 questions 中的 answer/explanation */
export function stripReadingAnswer(q: ReadingQuestionFull): ReadingQuestionForAnswer {
  return {
    ...q,
    questions: q.questions.map(({ answer, explanation, ...rest }) => rest),
  }
}

/** 五选五:剥离 blanks 中的 answer/explanation */
export function stripMatchingAnswer(q: MatchingQuestionFull): MatchingQuestionForAnswer {
  return {
    ...q,
    blanks: q.blanks.map(({ answer, explanation, ...rest }) => rest),
  }
}

/** 完形填空:剥离 blanks 中的 answer/explanation */
export function stripClozeAnswer(q: ClozeQuestionFull): ClozeQuestionForAnswer {
  return {
    ...q,
    blanks: q.blanks.map(({ answer, explanation, ...rest }) => rest),
  }
}

/** 语法填空:剥离 blanks 中的 answer/acceptAlternatives/explanation */
export function stripGrammarFillAnswer(q: GrammarFillQuestionFull): GrammarFillQuestionForAnswer {
  return {
    ...q,
    blanks: q.blanks.map(({ answer, acceptAlternatives, explanation, ...rest }) => rest),
  }
}

/** 作文:剥离范文 sampleAnswer */
export function stripWritingAnswer(q: WritingQuestionFull): WritingQuestionForAnswer {
  const { sampleAnswer, ...rest } = q
  return rest
}

/** 各题型过滤函数映射(内部按 type 分派) */
const stripMapper: {
  [K in PracticeType]: (q: PracticeFullByType[K]) => PracticeContentByType[K]
} = {
  reading: stripReadingAnswer,
  matching: stripMatchingAnswer,
  cloze: stripClozeAnswer,
  'grammar-fill': stripGrammarFillAnswer,
  writing: stripWritingAnswer,
}

/** 通用:Full 存储项 → 下发 PracticeItem(答案、解析均已剥离) */
export function stripPracticeItem(item: StoredPracticeItem<PracticeType>): PracticeItem {
  const content = stripMapper[item.content.type](item.content as never)
  return {
    id: item.id,
    type: item.content.type,
    source: item.content.source,
    difficulty: item.difficulty,
    tags: item.tags,
    content,
  } as PracticeItem
}

/** 整卷:Full → 下发 ExamPaper */
export function stripExamPaper(paper: ExamPaperFull): ExamPaper {
  return {
    ...paper,
    sections: paper.sections.map((s) => ({ ...s, content: stripSectionContent(s.type, s.content) })),
  }
}

/** 整卷单 section 内容过滤(内部工具) */
function stripSectionContent(type: PracticeType, content: ExamSectionContentFull) {
  const mapper = {
    reading: stripReadingAnswer,
    matching: stripMatchingAnswer,
    cloze: stripClozeAnswer,
    'grammar-fill': stripGrammarFillAnswer,
    writing: stripWritingAnswer,
  } as const
  return mapper[type](content as never)
}