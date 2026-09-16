// 阅读理解 mock 演示数据(Full 版含答案,仅用于联调,真实真题由后端契约提供)
// 精简约定:真题 1 篇 + 老师卷 1 篇;AI 出题由 handler 基于本库随机抽取并改写 source
import type { StoredPracticeItem } from '@/types/practice'

export const readingStore: StoredPracticeItem<'reading'>[] = [
  {
    id: 'reading-real-2023-1',
    title: '2023年真题·阅读理解 Passage 1',
    difficulty: 2,
    tags: ['主旨大意', '细节理解'],
    content: {
      id: 'reading-real-2023-1',
      type: 'reading',
      source: { type: 'real', meta: { year: 2023, region: '广东' } },
      passage:
        'Digital reading is changing how college students study. Instead of carrying heavy textbooks, many students now read on tablets and phones. Researchers found that students who read digitally tend to scan quickly but remember less. Meanwhile, those who read paper books take slower notes and score higher in comprehension tests.',
      questions: [
        {
          id: 1,
          stem: 'What is the main idea of the passage?',
          options: [
            { key: 'A', text: 'Digital reading improves memory.' },
            { key: 'B', text: 'Reading habits affect learning results.' },
            { key: 'C', text: 'Paper textbooks are too expensive.' },
            { key: 'D', text: 'College students never read paper books.' },
          ],
          answer: 'B',
          explanation: '全文对比数字阅读与纸质阅读对学习结果的影响,故选 B。',
        },
        {
          id: 2,
          stem: 'According to the passage, digital readers tend to ________.',
          options: [
            { key: 'A', text: 'read slowly and carefully' },
            { key: 'B', text: 'take detailed notes' },
            { key: 'C', text: 'scan quickly and remember less' },
            { key: 'D', text: 'score higher in tests' },
          ],
          answer: 'C',
          explanation: '第二句明确指出数字阅读者 tend to scan quickly but remember less。',
        },
        {
          id: 3,
          stem: 'Who scores higher in comprehension tests?',
          options: [
            { key: 'A', text: 'Digital readers.' },
            { key: 'B', text: 'Paper book readers.' },
            { key: 'C', text: 'Teachers.' },
            { key: 'D', text: 'Tablet sellers.' },
          ],
          answer: 'B',
          explanation: '第三句说明纸质书阅读者 score higher in comprehension tests。',
        },
        {
          id: 4,
          stem: 'The word “scan” in the passage most probably means ________.',
          options: [
            { key: 'A', text: 'to copy a document' },
            { key: 'B', text: 'to read quickly' },
            { key: 'C', text: 'to take photos' },
            { key: 'D', text: 'to write neatly' },
          ],
          answer: 'B',
          explanation: 'scan 与 remember less 并列,结合语境意为“快速浏览”。',
        },
        {
          id: 5,
          stem: 'What can be inferred from the passage?',
          options: [
            { key: 'A', text: 'Students should give up digital devices.' },
            { key: 'B', text: 'Paper reading may help deeper learning.' },
            { key: 'C', text: 'Tablets are useless for study.' },
            { key: 'D', text: 'Notes are unnecessary in tests.' },
          ],
          answer: 'B',
          explanation: '由纸质阅读者记得更牢、得分更高可推出纸质阅读有助于深度学习。',
        },
      ],
    },
  },
  {
    id: 'reading-teacher-1',
    title: '王老师·模拟阅读卷 Passage 1',
    difficulty: 3,
    tags: ['主旨大意', '推理判断'],
    content: {
      id: 'reading-teacher-1',
      type: 'reading',
      source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
      passage:
        'Sleep is often the first thing college students give up before exams. However, research shows that an all-night study session actually harms memory. During deep sleep, the brain organizes new information and connects it with what we already know. Students who sleep seven to eight hours usually perform better.',
      questions: [
        {
          id: 1,
          stem: 'What do many students give up before exams?',
          options: [
            { key: 'A', text: 'Breakfast.' },
            { key: 'B', text: 'Sleep.' },
            { key: 'C', text: 'Sports.' },
            { key: 'D', text: 'Friends.' },
          ],
          answer: 'B',
          explanation: '第一句指出睡眠是学生考前最先放弃的东西。',
        },
        {
          id: 2,
          stem: 'How does an all-night study session affect memory?',
          options: [
            { key: 'A', text: 'It improves memory greatly.' },
            { key: 'B', text: 'It harms memory.' },
            { key: 'C', text: 'It has no effect.' },
            { key: 'D', text: 'It cleans the brain.' },
          ],
          answer: 'B',
          explanation: '第二句 says an all-night study session actually harms memory。',
        },
        {
          id: 3,
          stem: 'What does the brain do during deep sleep?',
          options: [
            { key: 'A', text: 'It stops working completely.' },
            { key: 'B', text: 'It organizes new information.' },
            { key: 'C', text: 'It only dreams.' },
            { key: 'D', text: 'It forgets old knowledge.' },
          ],
          answer: 'B',
          explanation: '第三句说明深睡眠时大脑整理新信息并与旧知识连接。',
        },
        {
          id: 4,
          stem: 'Students who sleep 7-8 hours usually ________.',
          options: [
            { key: 'A', text: 'perform better' },
            { key: 'B', text: 'feel hungrier' },
            { key: 'C', text: 'wake up late' },
            { key: 'D', text: 'miss classes' },
          ],
          answer: 'A',
          explanation: '末句明确指出睡眠充足的考生通常表现得更好。',
        },
        {
          id: 5,
          stem: 'What can we learn from the passage?',
          options: [
            { key: 'A', text: 'Staying up all night is wise before exams.' },
            { key: 'B', text: 'Enough sleep supports exam performance.' },
            { key: 'C', text: 'Memory has nothing to do with sleep.' },
            { key: 'D', text: 'Students do not need rest.' },
          ],
          answer: 'B',
          explanation: '全文论证充足睡眠有助于记忆与考试发挥。',
        },
      ],
    },
  },
]