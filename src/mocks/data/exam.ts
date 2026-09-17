// 真题模考 mock 演示数据(Full 版含答案;handler 下发前经 stripExamPaper 过滤)
// 精简约定:仅保留 1 套演示卷,真实真题由后端契约另行提供
// 结构按任务书 11.7:五个部分(阅读30分/五选五10分/完形30分/语法15分/作文15分)
// 阅读部分契约调整:3 篇,每篇 5 题,题号全局连续 1-15(见 types/exam.d.ts)
// 答题卡编号规则:全局编号=前序 section 题量累加+本节题序(阅读1-15,匹配16-20,完形21-35,语法36-45)
import type { ExamPaperFull } from '@/types/exam'

export const examStore: ExamPaperFull[] = [
  {
    id: 'exam-2026',
    year: 2026,
    title: '2026年广东省普通专升本公共英语真题',
    totalScore: 100,
    duration: 120,
    sections: [
      {
        part: 1,
        type: 'reading',
        score: 30,
        // 阅读部分:3 篇,每篇 5 题,题号全局连续 1-15
        content: [
          {
            id: 'exam-2026-reading-1',
            type: 'reading',
            source: { type: 'real', meta: { year: 2026, region: '广东' } },
            passage:
              'Many workers now choose "quiet quitting" — doing only the basic duties and refusing extra tasks. Supporters say it protects mental health and work-life balance. Critics warn that long-term quiet quitting may slow career growth and hurt team spirit. Experts suggest finding a middle way: set clear boundaries, but keep learning new skills.\n\nIn China, young employees also talk about "lying flat" (tangping). Some reduce overtime and stop chasing promotions after years of pressure. Companies have started to respond by offering mental health programs and more flexible hours. A recent survey found that employees with flexible schedules report higher satisfaction and are less likely to look for new jobs.\n\nThe debate continues, but one thing is clear: a healthy workplace needs respect for both personal time and professional goals. Employees who communicate their needs openly often avoid the extreme choices altogether.',
            questions: [
              {
                id: 1,
                stem: 'What does "quiet quitting" mean in the workplace?',
                options: [
                  { key: 'A', text: 'Leaving a job secretly.' },
                  { key: 'B', text: 'Doing only basic duties.' },
                  { key: 'C', text: 'Refusing to speak at meetings.' },
                  { key: 'D', text: 'Working from home all week.' },
                ],
                answer: 'B',
                explanation: '第一句破折号后对 quiet quitting 的解释是只做基本职责。',
              },
              {
                id: 2,
                stem: 'What do supporters of quiet quitting emphasize?',
                options: [
                  { key: 'A', text: 'Higher salaries.' },
                  { key: 'B', text: 'Better teamwork.' },
                  { key: 'C', text: 'Mental health and balance.' },
                  { key: 'D', text: 'Faster promotion.' },
                ],
                answer: 'C',
                explanation: '第二句指出支持者认为它保护心理健康与工作生活平衡。',
              },
              {
                id: 3,
                stem: 'What do critics warn about quiet quitting?',
                options: [
                  { key: 'A', text: 'It may slow career growth.' },
                  { key: 'B', text: 'It increases salaries.' },
                  { key: 'C', text: 'It improves companies.' },
                  { key: 'D', text: 'It helps team spirit.' },
                ],
                answer: 'A',
                explanation: '第三句批评者警告它可能减缓职业成长、伤害团队精神。',
              },
              {
                id: 4,
                stem: 'What is "tangping" according to the passage?',
                options: [
                  { key: 'A', text: 'A type of online game.' },
                  { key: 'B', text: 'A fashion style among students.' },
                  { key: 'C', text: 'Another word for hardworking.' },
                  { key: 'D', text: 'A Chinese term for lying flat.' },
                ],
                answer: 'D',
                explanation: '第二段开头括号里注明 tangping 即 lying flat。',
              },
              {
                id: 5,
                stem: 'How are some companies responding to the trend?',
                options: [
                  { key: 'A', text: 'By cutting salaries.' },
                  { key: 'B', text: 'By offering mental health programs.' },
                  { key: 'C', text: 'By adding more work.' },
                  { key: 'D', text: 'By canceling holidays.' },
                ],
                answer: 'B',
                explanation: '第二段末句说明企业通过心理健康项目和弹性工时回应。',
              },
            ],
          },
          {
            id: 'exam-2026-reading-2',
            type: 'reading',
            source: { type: 'real', meta: { year: 2026, region: '广东' } },
            passage:
              'Working part-time has become common among college students in China. Some take jobs to cover daily costs, while others hope to gain experience before graduation. A survey at a Guangdong university this spring found that students who worked ten hours a week or less improved their time management without hurting their grades. However, experts warn that working too much can lead to stress and poor sleep. The key, they say, is to set a clear limit and always put study first.',
            questions: [
              {
                id: 6,
                stem: 'What has become common among college students?',
                options: [
                  { key: 'A', text: 'Working part-time.' },
                  { key: 'B', text: 'Studying abroad.' },
                  { key: 'C', text: 'Changing majors.' },
                  { key: 'D', text: 'Skipping breakfast.' },
                ],
                answer: 'A',
                explanation: '第一句指出大学生兼职越来越普遍。',
              },
              {
                id: 7,
                stem: 'Why do some students take part-time jobs?',
                options: [
                  { key: 'A', text: 'To cover costs or gain experience.' },
                  { key: 'B', text: 'To avoid exams.' },
                  { key: 'C', text: 'To meet famous people.' },
                  { key: 'D', text: 'To skip classes.' },
                ],
                answer: 'A',
                explanation: '第二句说明兼职目的:补贴开销与积累经验。',
              },
              {
                id: 8,
                stem: 'What did the survey find about students working ten hours a week or less?',
                options: [
                  { key: 'A', text: 'They improved time management.' },
                  { key: 'B', text: 'They dropped out of school.' },
                  { key: 'C', text: 'They failed all exams.' },
                  { key: 'D', text: 'They slept much more.' },
                ],
                answer: 'A',
                explanation: '第三句:每周不超过十小时的学生时间管理变好且成绩不受影响。',
              },
              {
                id: 9,
                stem: 'What may working too much cause?',
                options: [
                  { key: 'A', text: 'Stress and poor sleep.' },
                  { key: 'B', text: 'More savings.' },
                  { key: 'C', text: 'Better grades.' },
                  { key: 'D', text: 'Stronger friendship.' },
                ],
                answer: 'A',
                explanation: '第四句:过度工作会带来压力与睡眠问题。',
              },
              {
                id: 10,
                stem: 'What is the key according to experts?',
                options: [
                  { key: 'A', text: 'Setting a clear limit.' },
                  { key: 'B', text: 'Working as much as possible.' },
                  { key: 'C', text: 'Quitting all jobs.' },
                  { key: 'D', text: 'Copying friends.' },
                ],
                answer: 'A',
                explanation: '末句:关键是设定明确上限,学业优先。',
              },
            ],
          },
          {
            id: 'exam-2026-reading-3',
            type: 'reading',
            source: { type: 'real', meta: { year: 2026, region: '广东' } },
            passage:
              'Last term, the university library began staying open until midnight during exam weeks. The change came after many students asked for quiet places to study at night. Since then, late-night seats have been almost fully booked. The library also added more power outlets and a coffee corner for late stayers. A librarian said the aim is not just longer hours, but a comfortable space where students want to stay.',
            questions: [
              {
                id: 11,
                stem: 'When does the library stay open until midnight?',
                options: [
                  { key: 'A', text: 'During exam weeks.' },
                  { key: 'B', text: 'All year round.' },
                  { key: 'C', text: 'Only on weekends.' },
                  { key: 'D', text: 'On public holidays.' },
                ],
                answer: 'A',
                explanation: '第一句:考试周期间图书馆开放至午夜。',
              },
              {
                id: 12,
                stem: 'Why did the library make the change?',
                options: [
                  { key: 'A', text: 'Students asked for quiet study places.' },
                  { key: 'B', text: 'Teachers demanded it.' },
                  { key: 'C', text: 'Nobody visited in the daytime.' },
                  { key: 'D', text: 'A donation required it.' },
                ],
                answer: 'A',
                explanation: '第二句:许多学生希望有夜间安静的学习场所。',
              },
              {
                id: 13,
                stem: 'What happened after the change?',
                options: [
                  { key: 'A', text: 'Late-night seats are almost fully booked.' },
                  { key: 'B', text: 'Fewer students came.' },
                  { key: 'C', text: 'The library became empty.' },
                  { key: 'D', text: 'All the books were sold.' },
                ],
                answer: 'A',
                explanation: '第三句:夜间座位几乎被约满。',
              },
              {
                id: 14,
                stem: 'What else did the library add?',
                options: [
                  { key: 'A', text: 'Power outlets and a coffee corner.' },
                  { key: 'B', text: 'A swimming pool.' },
                  { key: 'C', text: 'More parking spaces.' },
                  { key: 'D', text: 'A small cinema.' },
                ],
                answer: 'A',
                explanation: '第四句:图书馆增设了插座与咖啡角。',
              },
              {
                id: 15,
                stem: 'What is the librarian’s aim?',
                options: [
                  { key: 'A', text: 'A comfortable space students want to stay in.' },
                  { key: 'B', text: 'Longer hours only.' },
                  { key: 'C', text: 'Higher fees.' },
                  { key: 'D', text: 'Fewer rules.' },
                ],
                answer: 'A',
                explanation: '末句:目标不只是延长时长,而是让学生愿意待的舒适空间。',
              },
            ],
          },
        ],
      },
      {
        part: 2,
        type: 'matching',
        score: 10,
        content: {
          id: 'exam-2026-matching',
          type: 'matching',
          source: { type: 'real', meta: { year: 2026, region: '广东' } },
          passage:
            'Moving to a new city can be both exciting and stressful. [[1]] First, explore your neighborhood on foot during the first weekend. [[2]] Second, join local interest groups, such as a running club or a reading circle. [[3]] Third, keep a weekly video call with old friends, but do not rely only on the screen. [[4]] Finally, be patient with yourself. [[5]]',
          blanks: [
            { id: 1, answer: 'C', explanation: '空后列举建议,需总起句收下。' },
            { id: 2, answer: 'A', explanation: '解释步行探索的好处,承接 on foot。' },
            { id: 3, answer: 'E', explanation: '说明兴趣小组如何带来新朋友,承接 join groups。' },
            { id: 4, answer: 'B', explanation: '提醒兼顾线上与线下社交,承接 video call。' },
            { id: 5, answer: 'D', explanation: '安抚情绪呼应最后一句耐心,收束全文。' },
          ],
          options: [
            { key: 'A', text: 'Walking helps you remember streets and feel at home.' },
            { key: 'B', text: 'Real face-to-face time matters just as much.' },
            { key: 'C', text: 'Here are four tips to settle in quickly.' },
            { key: 'D', text: 'It takes time to feel you belong somewhere new.' },
            { key: 'E', text: 'Sharing hobbies is the fastest way to meet friends.' },
          ],
        },
      },
      {
        part: 3,
        type: 'cloze',
        score: 30,
        content: {
          id: 'exam-2026-cloze',
          type: 'cloze',
          source: { type: 'real', meta: { year: 2026, region: '广东' } },
          passage:
            'Chen Yu was a quiet student who rarely spoke in class. Everything changed when her English teacher [[1]] her to join the school speech contest. At first, Chen Yu shook her head, [[2]] that her voice was too weak. But the teacher believed in her and promised to help. They practiced every afternoon for a [[3]]. Day by day, Chen Yu learned to control her nerves. The night before the contest, she could not sleep and kept [[4]] her speech. On the stage, her hands were cold, but she [[5]] her head and began. Her voice grew stronger [[6]] she went on. When she finished, the hall [[7]] with applause. She did not win first prize, [[8]] she gained something more valuable: confidence. From that day, Chen Yu [[9]] every chance to speak. She joined the debating team and even hosted the school festival. Looking back, she says, “Growth begins the moment you [[10]] out of your comfort zone.” Teachers also noticed a change in other quiet students, who began raising their hands [[11]]. The school later set up a speaking club, [[12]] now has over one hundred members. Chen Yu often returns as a guest to encourage newcomers. “Do not wait to be perfect,” she tells them. “Start small, [[13]] you will surprise yourself.” Her story [[14]] in the school newspaper and soon spread to other schools. It proves that a single teacher’s trust can [[15]] a student’s path.',
          blanks: [
            {
              id: 1,
              options: [
                { key: 'A', text: 'encouraged' },
                { key: 'B', text: 'forced' },
                { key: 'C', text: 'ordered' },
                { key: 'D', text: 'warned' },
              ],
              answer: 'A',
              explanation: 'encourage sb. to do 鼓励某人做某事,与后文相信她呼应。',
            },
            {
              id: 2,
              options: [
                { key: 'A', text: 'saying' },
                { key: 'B', text: 'said' },
                { key: 'C', text: 'says' },
                { key: 'D', text: 'to say' },
              ],
              answer: 'A',
              explanation: '现在分词作伴随状语,表同时发生的动作。',
            },
            {
              id: 3,
              options: [
                { key: 'A', text: 'week' },
                { key: 'B', text: 'minute' },
                { key: 'C', text: 'kilometer' },
                { key: 'D', text: 'lunch' },
              ],
              answer: 'A',
              explanation: '每天练一周,与下文"一天天进步"呼应。',
            },
            {
              id: 4,
              options: [
                { key: 'A', text: 'repeating' },
                { key: 'B', text: 'reading' },
                { key: 'C', text: 'writing' },
                { key: 'D', text: 'forgetting' },
              ],
              answer: 'A',
              explanation: 'keep doing 反复背诵,与"睡不着+背稿"一致。',
            },
            {
              id: 5,
              options: [
                { key: 'A', text: 'raised' },
                { key: 'B', text: 'lowered' },
                { key: 'C', text: 'covered' },
                { key: 'D', text: 'turned' },
              ],
              answer: 'A',
              explanation: 'raised her head 抬起头,与随后开始演讲衔接。',
            },
            {
              id: 6,
              options: [
                { key: 'A', text: 'as' },
                { key: 'B', text: 'unless' },
                { key: 'C', text: 'though' },
                { key: 'D', text: 'but' },
              ],
              answer: 'A',
              explanation: 'as she went on 随着演讲继续,表渐变。',
            },
            {
              id: 7,
              options: [
                { key: 'A', text: 'broke out' },
                { key: 'B', text: 'burst out' },
                { key: 'C', text: 'was filled' },
                { key: 'D', text: 'was carried' },
              ],
              answer: 'C',
              explanation: 'the hall was filled with applause 大厅响起掌声(被动)。',
            },
            {
              id: 8,
              options: [
                { key: 'A', text: 'and' },
                { key: 'B', text: 'but' },
                { key: 'C', text: 'so' },
                { key: 'D', text: 'or' },
              ],
              answer: 'B',
              explanation: '没有拿第一名,但收获自信;转折用 but。',
            },
            {
              id: 9,
              options: [
                { key: 'A', text: 'took' },
                { key: 'B', text: 'gave' },
                { key: 'C', text: 'missed' },
                { key: 'D', text: 'wasted' },
              ],
              answer: 'A',
              explanation: 'take every chance 抓住每个机会,转机后主动表现。',
            },
            {
              id: 10,
              options: [
                { key: 'A', text: 'step' },
                { key: 'B', text: 'walk' },
                { key: 'C', text: 'run' },
                { key: 'D', text: 'jump' },
              ],
              answer: 'A',
              explanation: 'step out of your comfort zone 迈出舒适区,固定表达。',
            },
            {
              id: 11,
              options: [
                { key: 'A', text: 'bravely' },
                { key: 'B', text: 'angrily' },
                { key: 'C', text: 'sleepily' },
                { key: 'D', text: 'fearfully' },
              ],
              answer: 'A',
              explanation: '受鼓舞勇敢举手,与主题一致。',
            },
            {
              id: 12,
              options: [
                { key: 'A', text: 'which' },
                { key: 'B', text: 'who' },
                { key: 'C', text: 'where' },
                { key: 'D', text: 'what' },
              ],
              answer: 'A',
              explanation: '非限制性定语从句,先行词 club(物),用 which。',
            },
            {
              id: 13,
              options: [
                { key: 'A', text: 'and' },
                { key: 'B', text: 'or' },
                { key: 'C', text: 'but' },
                { key: 'D', text: 'for' },
              ],
              answer: 'A',
              explanation: '祈使句 + and + 结果:"从小开始,你会惊讶于自己"。',
            },
            {
              id: 14,
              options: [
                { key: 'A', text: 'published' },
                { key: 'B', text: 'was published' },
                { key: 'C', text: 'publishes' },
                { key: 'D', text: 'publishing' },
              ],
              answer: 'B',
              explanation: 'story 与 publish 被动,且用过去时。',
            },
            {
              id: 15,
              options: [
                { key: 'A', text: 'change' },
                { key: 'B', text: 'block' },
                { key: 'C', text: 'copy' },
                { key: 'D', text: 'end' },
              ],
              answer: 'A',
              explanation: 'change a student’s path 改变人生道路,点明主题。',
            },
          ],
        },
      },
      {
        part: 4,
        type: 'grammar-fill',
        score: 15,
        content: {
          id: 'exam-2026-grammar',
          type: 'grammar-fill',
          source: { type: 'real', meta: { year: 2026, region: '广东' } },
          passage:
            'In the past few years, short-video platforms [[1]] (become) a major way for young people to learn cooking. Many users say the videos are [[2]] (easy) to follow than written recipes. Watching a dish [[3]] (prepare) step by step gives beginners confidence. However, experts warn that watching is not the same as doing. “You cannot learn to swim by [[4]] (watch) the water,” one chef joked. He suggests learners pick one simple dish each week and repeat it until it becomes [[5]] (nature). Another benefit is that videos connect people. A grandmother in Hunan, for example, [[6]] (gain) over a million followers by sharing home dishes. Her videos are popular not only for the food but also for the warmth of family life, [[7]] many viewers miss in big cities. Cooking from videos also saves money. Students who cook at home spend far [[8]] (little) than those who eat out every day. To many, the kitchen has become a place of rest after busy study. With music playing and vegetables sizzling in the pan, the tiredness of the day seems to [[9]] (gradual) melt away. As one young cook puts it, “The best dish is the one you cook for [[10]] (you).”',
          blanks: [
            { id: 1, hint: 'become', answer: 'have become', explanation: 'In the past few years 与现在完成时连用。' },
            { id: 2, hint: 'easy', answer: 'easier', explanation: 'than 表比较,easy 变 easier。' },
            { id: 3, hint: 'prepare', answer: 'prepared', explanation: 'dish 与 prepare 被动,watch sth. done。' },
            { id: 4, hint: 'watch', answer: 'watching', explanation: 'by + 动名词:通过观看。' },
            { id: 5, hint: 'nature', answer: 'natural', explanation: 'becomes + 形容词,自然的 natural。' },
            { id: 6, hint: 'gain', answer: 'has gained', explanation: '强调至今的结果,用现在完成时。' },
            { id: 7, answer: 'which', explanation: '非限制性定语从句,先行词 warmth of family life。' },
            { id: 8, hint: 'little', answer: 'less', explanation: 'far + 比较级:花费更少。' },
            { id: 9, hint: 'gradual', answer: 'gradually', explanation: '修饰动词 melt 用副词。' },
            { id: 10, hint: 'you', answer: 'yourself', explanation: 'for yourself 为你自己。' },
          ],
        },
      },
      {
        part: 5,
        type: 'writing',
        score: 15,
        content: {
          id: 'exam-2026-writing',
          type: 'writing',
          source: { type: 'real', meta: { year: 2026, region: '广东' } },
          prompt:
            '假定你是李华,学校英文报正在征集关于“人工智能与学习”的短文。请写一篇文章投稿,内容包括:(1)你在学习中如何使用 AI 工具;(2)你对 AI 学习的看法。',
          requirements: ['词数 100-120;', '观点明确,内容积极;', '结构完整。'],
          wordLimit: 120,
          scoringDimensions: ['内容要点', '语言表达', '篇章结构'],
          sampleAnswer:
            'Artificial intelligence has become part of my daily study. I often use translation tools to check my writing and ask AI assistants to explain difficult grammar points. These tools save me a lot of time and let me learn at my own pace.\n\nIn my opinion, AI is a helpful servant, not a master. It can answer questions quickly, but real understanding still comes from thinking by ourselves. So I never copy AI answers directly; I use them as a starting point for my own ideas.\n\nIf we use AI wisely, learning will become more efficient and more interesting.',
        },
      },
    ],
  },
]