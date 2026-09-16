// 作文 mock 演示数据(Full 版含范文)
// 精简约定:真题 1 篇 + 老师卷 1 篇
// 批改约定:作文一律人工批改(设计原则 6),提交后进入 pending,批改完成后下发评分维度与范文
import type { StoredPracticeItem } from '@/types/practice'

export const writingStore: StoredPracticeItem<'writing'>[] = [
  {
    id: 'writing-real-2023-1',
    title: '2023年真题·写作',
    difficulty: 4,
    tags: ['书信写作', '建议信'],
    content: {
      id: 'writing-real-2023-1',
      type: 'writing',
      source: { type: 'real', meta: { year: 2023, region: '广东' } },
      prompt:
        '假定你是李华,你的英国笔友 Peter 计划来中国学习,写信向你咨询如何适应中国的校园生活。请给他写一封回信。',
      requirements: [
        '内容须包含三点建议(如语言交流、社团活动、饮食习惯等);',
        '书信格式完整(称呼、正文、结束语、署名);',
        '可适当增加细节,使行文连贯。',
      ],
      wordLimit: 120,
      scoringDimensions: ['内容要点', '语言表达', '篇章结构'],
      sampleAnswer:
        'Dear Peter,\n\nI am glad to hear that you are coming to study in China. Here are three tips to help you settle in quickly.\n\nFirst, try to speak Chinese in daily life. Even simple greetings will help you make friends and improve your language skills. Second, join a student club, such as a basketball team or a calligraphy group, where you can meet people who share your interests. Third, give yourself time to get used to the food. Many canteens offer both Chinese and Western dishes, so you will always find something you like.\n\nI hope these suggestions are helpful. Looking forward to seeing you soon!\n\nYours sincerely,\nLi Hua',
    },
  },
  {
    id: 'writing-teacher-1',
    title: '王老师·模拟写作 1',
    difficulty: 4,
    tags: ['议论文', '观点对比'],
    content: {
      id: 'writing-teacher-1',
      type: 'writing',
      source: { type: 'teacher', meta: { teacherName: '王老师', school: '启航班工作室' } },
      prompt:
        '现在越来越多的大学生选择在毕业前考取各种职业证书。有人认为这有助于就业,也有人认为会耽误学业。请就此话题写一篇短文,陈述你的观点。',
      requirements: [
        '词数 100-120;',
        '必须有明确的个人观点;',
        '至少给出两条支撑理由。',
      ],
      wordLimit: 120,
      scoringDimensions: ['内容要点', '语言表达', '篇章结构'],
      sampleAnswer:
        'More and more college students are taking professional certificates before graduation. In my opinion, the advantages outweigh the disadvantages.\n\nFirst, certificates show employers that a student has practical skills beyond textbooks, which helps in job hunting. Second, preparing for a certificate exam forces students to manage their time well and keep learning. As long as students do not let certificates replace their major courses, the process is rewarding.\n\nOf course, students should choose certificates related to their career plans instead of collecting random ones. With a clear goal and balanced effort, certificates can open more doors after graduation.',
    },
  },
]