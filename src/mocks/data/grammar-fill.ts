// 语法填空 mock 演示数据(Full 版含答案)
// 精简约定:真题 1 篇 + 老师卷 1 篇;每篇 10 空,空位以 [[n]] 占位
// 提示词 hint 缺省表示无提示词填空;大小写敏感判分偏好由服务端读用户设置(任务书 12.3 注)
import type { StoredPracticeItem } from '@/types/practice'

export const grammarFillStore: StoredPracticeItem<'grammar-fill'>[] = [
  {
    id: 'grammar-real-2023-1',
    title: '2023年真题·语法填空 Passage 1',
    difficulty: 3,
    tags: ['时态语态', '非谓语'],
    content: {
      id: 'grammar-real-2023-1',
      type: 'grammar-fill',
      source: { type: 'real', meta: { year: 2023, region: '广东' } },
      passage:
        'Language learning apps have become increasingly [[1]] (popular) among college students in recent years. One reason [[2]] (be) that such apps turn boring memorization into games. A survey [[3]] (conduct) last spring showed that students who used these apps for twenty minutes a day scored higher in dictation tests. “The key is consistency,” said Professor Liu, [[4]] has studied digital learning for a decade. He advises beginners [[5]] (set) a small daily goal rather than a large one. Meanwhile, teachers remind students that an app is only a tool; real progress still depends on how much effort learners [[6]] (put) in. The most successful users are those who combine apps [[7]] traditional reading. [[8]] (fortunate), some students still stop after two or three days, giving up just before real improvement. Experts suggest [[9]] (review) words in the morning when the mind is fresh. With persistence, even ten minutes a day can make a [[10]] (true) big difference.',
      blanks: [
        {
          id: 1,
          hint: 'popular',
          answer: 'popular',
          explanation: 'increasingly + 原级形容词,无比较含义。',
        },
        {
          id: 2,
          hint: 'be',
          answer: 'is',
          explanation: '主语 one reason 为单数,陈述客观原因用一般现在时。',
        },
        {
          id: 3,
          hint: 'conduct',
          answer: 'conducted',
          explanation: 'survey 与 conduct 为被动关系,last spring 提示过去时,作后置定语。',
        },
        {
          id: 4,
          answer: 'who',
          explanation: '非限制性定语从句,先行词 Professor Liu 指人,用 who。',
        },
        {
          id: 5,
          hint: 'set',
          answer: 'to set',
          explanation: 'advise sb. to do sth. 建议某人做某事。',
        },
        {
          id: 6,
          hint: 'put',
          answer: 'put',
          explanation: 'how much effort learners put in:从句为一般现在时,主语复数,put 原形(过去分词同形)。',
        },
        {
          id: 7,
          answer: 'with',
          explanation: 'combine A with B 把 A 与 B 结合,固定搭配。',
        },
        {
          id: 8,
          hint: 'fortunate',
          answer: 'Unfortunately',
          explanation: '句意转折“遗憾的是”,用副词置于句首,首字母大写。',
        },
        {
          id: 9,
          hint: 'review',
          answer: 'reviewing',
          explanation: 'suggest doing sth. 建议做某事,动名词作宾语。',
        },
        {
          id: 10,
          hint: 'true',
          answer: 'truly',
          explanation: '修饰形容词 big 需用副词 truly。',
        },
      ],
    },
  },
  {
    id: 'grammar-teacher-1',
    title: '王老师·模拟语法填空 Passage 1',
    difficulty: 3,
    tags: ['时态语态', '比较结构'],
    content: {
      id: 'grammar-teacher-1',
      type: 'grammar-fill',
      source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
      passage:
        'More and more college students are choosing part-time jobs, not only for money but also for experience. Working in a coffee shop, for example, helps students become more [[1]] (confidence) when talking to strangers. A study found that students with working experience tend to be better at [[2]] (manage) their time. However, experts warn that study should always come [[3]] (one). They suggest students limit work to ten hours a week. Otherwise, grades may drop [[4]] (quick), and tiredness will follow. “A part-time job is like seasoning,” one career adviser said, “too much of it [[5]] (ruin) the dish.” She advises students [[6]] (choose) jobs related to their majors, so that the work also builds future careers. Many campuses now hold job fairs for students [[7]] want to gain real-world skills. With [[8]] (they) help, hundreds of students have found suitable positions. Those who balance work and study well often say the experience makes them [[9]] (responsible) than before. In the end, the value of a part-time job depends on how wisely it [[10]] (use).',
      blanks: [
        {
          id: 1,
          hint: 'confidence',
          answer: 'confident',
          explanation: 'become + 形容词,自信的 confident。',
        },
        {
          id: 2,
          hint: 'manage',
          answer: 'managing',
          explanation: 'be good at doing sth. 擅长做某事,at 后接动名词。',
        },
        {
          id: 3,
          hint: 'one',
          answer: 'first',
          explanation: 'come first 位居第一,固定表达。',
        },
        {
          id: 4,
          hint: 'quick',
          answer: 'quickly',
          explanation: '修饰动词 drop 需用副词 quickly。',
        },
        {
          id: 5,
          hint: 'ruin',
          answer: 'ruins',
          explanation: '引用格言用一般现在时,主语 too much of it 视为单数。',
        },
        {
          id: 6,
          hint: 'choose',
          answer: 'to choose',
          explanation: 'advise sb. to do sth. 建议某人做某事。',
        },
        {
          id: 7,
          answer: 'who',
          explanation: '定语从句先行词 students 指人,从句缺主语,用 who。',
        },
        {
          id: 8,
          hint: 'they',
          answer: 'their',
          explanation: 'with + 形代 + 名词,their help 他们的帮助。',
        },
        {
          id: 9,
          hint: 'responsible',
          answer: 'more responsible',
          explanation: 'than before 表比较,多音节词前加 more。',
        },
        {
          id: 10,
          hint: 'use',
          answer: 'is used',
          explanation: 'it(job)与 use 为被动关系,陈述客观事实用一般现在时被动。',
        },
      ],
    },
  },
]