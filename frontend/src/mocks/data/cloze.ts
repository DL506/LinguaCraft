// 完形填空 mock 演示数据(Full 版含答案)
// 精简约定:真题 1 篇 + 老师卷 1 篇;每篇 10 空,空位以 [[n]] 占位
import type { StoredPracticeItem } from '@/types/practice'

export const clozeStore: StoredPracticeItem<'cloze'>[] = [
  {
    id: 'cloze-real-2023-1',
    title: '2023年真题·完形填空 Passage 1',
    difficulty: 3,
    tags: ['上下文逻辑', '词义辨析'],
    content: {
      id: 'cloze-real-2023-1',
      type: 'cloze',
      source: { type: 'real', meta: { year: 2023, region: '广东' } },
      passage:
        'When Li Hua entered college, she was too shy to speak in class. She decided to [[1]] this problem step by step. First, she practiced answering questions in [[2]] of a mirror at home. Then she [[3]] a small study group with three classmates. At first, her voice [[4]], but her friends encouraged her to continue. Week by week, her confidence [[5]]. By the end of the term, she could give a ten-minute speech without [[6]]. Looking back, she believes the key was not [[7]] but practice. “Everyone can improve [[8]] they are willing to try,” she says. Now she [[9]] newcomers in her department, sharing the same advice: start small, and never [[10]] up.',
      blanks: [
        {
          id: 1,
          options: [
            { key: 'A', text: 'face' },
            { key: 'B', text: 'avoid' },
            { key: 'C', text: 'hide' },
            { key: 'D', text: 'enjoy' },
          ],
          answer: 'A',
          explanation: 'face this problem 意为“正视这个问题”,与后文逐步克服呼应。',
        },
        {
          id: 2,
          options: [
            { key: 'A', text: 'need' },
            { key: 'B', text: 'front' },
            { key: 'C', text: 'middle' },
            { key: 'D', text: 'side' },
          ],
          answer: 'B',
          explanation: 'in front of a mirror 对着镜子练习,是常见表达。',
        },
        {
          id: 3,
          options: [
            { key: 'A', text: 'joined' },
            { key: 'B', text: 'refused' },
            { key: 'C', text: 'broke' },
            { key: 'D', text: 'sold' },
          ],
          answer: 'A',
          explanation: 'join a group 加入学习小组,符合组建小组的语境。',
        },
        {
          id: 4,
          options: [
            { key: 'A', text: 'rose' },
            { key: 'B', text: 'shook' },
            { key: 'C', text: 'carried' },
            { key: 'D', text: 'sang' },
          ],
          answer: 'B',
          explanation: 'voice shook 声音发抖,描写初次发言的紧张。',
        },
        {
          id: 5,
          options: [
            { key: 'A', text: 'fell' },
            { key: 'B', text: 'disappeared' },
            { key: 'C', text: 'grew' },
            { key: 'D', text: 'stopped' },
          ],
          answer: 'C',
          explanation: 'confidence grew 信心增强,与 Week by week 渐进语义一致。',
        },
        {
          id: 6,
          options: [
            { key: 'A', text: 'fear' },
            { key: 'B', text: 'notes' },
            { key: 'C', text: 'applause' },
            { key: 'D', text: 'interest' },
          ],
          answer: 'A',
          explanation: 'without fear 毫不害怕,表明克服了紧张心理。',
        },
        {
          id: 7,
          options: [
            { key: 'A', text: 'talent' },
            { key: 'B', text: 'money' },
            { key: 'C', text: 'luck' },
            { key: 'D', text: 'time' },
          ],
          answer: 'A',
          explanation: 'the key was not talent but practice:关键是练习而非天赋,与后文并列。',
        },
        {
          id: 8,
          options: [
            { key: 'A', text: 'as long as' },
            { key: 'B', text: 'as if' },
            { key: 'C', text: 'even though' },
            { key: 'D', text: 'in case' },
          ],
          answer: 'A',
          explanation: 'as long as 只要,引导条件状语,符合“只要愿意尝试”语义。',
        },
        {
          id: 9,
          options: [
            { key: 'A', text: 'charges' },
            { key: 'B', text: 'teaches' },
            { key: 'C', text: 'helps' },
            { key: 'D', text: 'watches' },
          ],
          answer: 'C',
          explanation: 'helps newcomers 帮助新生,呼应分享经验的语境。',
        },
        {
          id: 10,
          options: [
            { key: 'A', text: 'put' },
            { key: 'B', text: 'stand' },
            { key: 'C', text: 'wake' },
            { key: 'D', text: 'give' },
          ],
          answer: 'D',
          explanation: 'never give up 永不放弃,固定搭配,收束全文主题。',
        },
      ],
    },
  },
  {
    id: 'cloze-teacher-1',
    title: '王老师·模拟完形填空 Passage 1',
    difficulty: 3,
    tags: ['上下文逻辑', '名词辨析'],
    content: {
      id: 'cloze-teacher-1',
      type: 'cloze',
      source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
      passage:
        'Wang Lin never liked running. In high school, she was always the last one in the 800-meter test. Everything changed when she entered college and found a running [[1]] on campus. Her coach told her not to care about [[2]] but about finishing. She started with slow jogs at dawn. At first, even one kilometer made her out of [[3]]. But she kept a training diary and recorded every small [[4]]. After three months, she finished a five-kilometer race. The moment she crossed the line, she could not [[5]] her feelings. Running taught her that progress is made with [[6]] steps. Now she runs to clear her mind before exams. “My legs are stronger, and so is my [[7]],” she says. Last month, she [[8]] a campus fun run for beginners. More than fifty students joined her [[9]]. She hopes to run a half marathon next year, step by [[10]] of course.',
      blanks: [
        {
          id: 1,
          options: [
            { key: 'A', text: 'club' },
            { key: 'B', text: 'shop' },
            { key: 'C', text: 'class' },
            { key: 'D', text: 'library' },
          ],
          answer: 'A',
          explanation: 'a running club 跑步社团,与教练出现的下文一致。',
        },
        {
          id: 2,
          options: [
            { key: 'A', text: 'speed' },
            { key: 'B', text: 'weather' },
            { key: 'C', text: 'clothes' },
            { key: 'D', text: 'diet' },
          ],
          answer: 'A',
          explanation: 'not care about speed but finishing 不在意速度而在意完成。',
        },
        {
          id: 3,
          options: [
            { key: 'A', text: 'breath' },
            { key: 'B', text: 'order' },
            { key: 'C', text: 'date' },
            { key: 'D', text: 'sight' },
          ],
          answer: 'A',
          explanation: 'out of breath 上气不接下气,固定搭配。',
        },
        {
          id: 4,
          options: [
            { key: 'A', text: 'mistake' },
            { key: 'B', text: 'victory' },
            { key: 'C', text: 'promise' },
            { key: 'D', text: 'secret' },
          ],
          answer: 'B',
          explanation: 'recorded every small victory 记录每次小胜利,与训练日记呼应。',
        },
        {
          id: 5,
          options: [
            { key: 'A', text: 'control' },
            { key: 'B', text: 'doubt' },
            { key: 'C', text: 'trust' },
            { key: 'D', text: 'spare' },
          ],
          answer: 'A',
          explanation: 'could not control her feelings 无法控制情绪,激动之情溢于言表。',
        },
        {
          id: 6,
          options: [
            { key: 'A', text: 'giant' },
            { key: 'B', text: 'careless' },
            { key: 'C', text: 'steady' },
            { key: 'D', text: 'sudden' },
          ],
          answer: 'C',
          explanation: 'steady steps 稳健的步伐,象征循序渐进的进步。',
        },
        {
          id: 7,
          options: [
            { key: 'A', text: 'will' },
            { key: 'B', text: 'shadow' },
            { key: 'C', text: 'noise' },
            { key: 'D', text: 'sleeve' },
          ],
          answer: 'A',
          explanation: 'so is my will 意志力也更强了,与双腿更健壮并列。',
        },
        {
          id: 8,
          options: [
            { key: 'A', text: 'refused' },
            { key: 'B', text: 'organized' },
            { key: 'C', text: 'canceled' },
            { key: 'D', text: 'bought' },
          ],
          answer: 'B',
          explanation: 'organized a campus fun run 组织趣味跑,与新手参与呼应。',
        },
        {
          id: 9,
          options: [
            { key: 'A', text: 'dishes' },
            { key: 'B', text: 'bikes' },
            { key: 'C', text: 'run' },
            { key: 'D', text: 'meeting' },
          ],
          answer: 'C',
          explanation: 'joined her run 参加了她的跑步活动。',
        },
        {
          id: 10,
          options: [
            { key: 'A', text: 'end' },
            { key: 'B', text: 'step' },
            { key: 'C', text: 'day' },
            { key: 'D', text: 'lane' },
          ],
          answer: 'B',
          explanation: 'step by step 一步一步,呼应全文主题并巧妙双关。',
        },
      ],
    },
  },
]