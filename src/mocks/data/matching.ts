// 五选五(短文造句匹配)mock 演示数据(Full 版含答案)
// 精简约定:真题 1 篇 + 老师卷 1 篇;空位以 [[n]] 占位(任务书第 8 章)
import type { StoredPracticeItem } from '@/types/practice'

export const matchingStore: StoredPracticeItem<'matching'>[] = [
  {
    id: 'matching-real-2023-1',
    title: '2023年真题·五选五 Passage 1',
    difficulty: 3,
    tags: ['句间衔接', '逻辑顺序'],
    content: {
      id: 'matching-real-2023-1',
      type: 'matching',
      source: { type: 'real', meta: { year: 2023, region: '广东' } },
      passage:
        'Reading is one of the most valuable habits a student can build. [[1]] First, set a fixed time every day. [[2]] Second, choose materials at the right level. [[3]] Third, keep a small notebook for new words. [[4]] Finally, share what you read with friends. [[5]]',
      blanks: [
        {
          id: 1,
          answer: 'C',
          explanation: '本空引出后文的 First/Second 建议,需选择承上启下的总起句。',
        },
        {
          id: 2,
          answer: 'A',
          explanation: 'A 解释固定时间的好处,承接 First, set a fixed time。',
        },
        {
          id: 3,
          answer: 'E',
          explanation: 'E 说明材料太难的后果,呼应 choose materials at the right level。',
        },
        {
          id: 4,
          answer: 'B',
          explanation: 'B 讲述记录生词如何帮助记忆,承接 keep a notebook。',
        },
        {
          id: 5,
          answer: 'D',
          explanation: 'D 收束全段,鼓励坚持并点明坚持带来的变化,与分享话题衔接。',
        },
      ],
      options: [
        { key: 'A', text: 'A regular time helps your brain get ready to focus.' },
        { key: 'B', text: 'Reviewing these words once a week keeps them fresh.' },
        { key: 'C', text: 'How can you build this habit? Here are four tips.' },
        { key: 'D', text: 'Talking about a book makes its ideas truly yours.' },
        { key: 'E', text: 'If a text is too hard, you may give up too soon.' },
      ],
    },
  },
  {
    id: 'matching-teacher-1',
    title: '王老师·模拟五选五 Passage 1',
    difficulty: 4,
    tags: ['句间衔接', '因果逻辑'],
    content: {
      id: 'matching-teacher-1',
      type: 'matching',
      source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
      passage:
        'Morning exercise can change your whole day. [[1]] First, ten minutes of stretching wakes up your muscles. [[2]] Second, exercise releases chemicals that lift your mood. [[3]] Third, a short run improves blood flow to the brain. [[4]] You do not need a gym membership to enjoy these benefits. [[5]]',
      blanks: [
        {
          id: 1,
          answer: 'B',
          explanation: '空后展开三点好处,需总起句引出。',
        },
        {
          id: 2,
          answer: 'D',
          explanation: 'D 具体说明拉伸后身体变灵活,承接 wakes up your muscles。',
        },
        {
          id: 3,
          answer: 'E',
          explanation: 'E 指出好心情对学习状态的影响,承接 lift your mood。',
        },
        {
          id: 4,
          answer: 'A',
          explanation: 'A 承接 blood flow,解释大脑清醒的表现。',
        },
        {
          id: 5,
          answer: 'C',
          explanation: 'C 提出在家即可运动,呼应 no gym membership。',
        },
      ],
      options: [
        { key: 'A', text: 'As a result, you think more clearly in morning classes.' },
        { key: 'B', text: 'Here are three reasons to give it a try.' },
        { key: 'C', text: 'A jump rope or a simple mat is enough to start.' },
        { key: 'D', text: 'Your body feels lighter and more flexible after that.' },
        { key: 'E', text: 'With a better mood, you learn faster.' },
      ],
    },
  },
]