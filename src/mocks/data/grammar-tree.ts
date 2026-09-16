// 学语法 mock 演示数据:目录树 + 各章节 Markdown 内容
// 精简约定:6 大考点(每个含 2 小节),内容按任务书 10.14 结构:考点精讲 → 真题示例 → 易错点辨析 → 专项练习
import type { GrammarNode, GrammarSection } from '@/types/grammar'

/** 语法目录树(按考点组织,任务书 10.14) */
export const grammarTree: GrammarNode[] = [
  {
    id: 'tenses',
    name: '动词时态',
    children: [
      { id: 'tenses-overview', name: '时态总览与用法' },
      { id: 'tenses-perfect', name: '完成时态的辨析' },
    ],
  },
  {
    id: 'attributive',
    name: '定语从句',
    children: [
      { id: 'attributive-basic', name: '关系代词与关系副词' },
      { id: 'attributive-advanced', name: '非限制性定语从句' },
    ],
  },
  {
    id: 'noun-clause',
    name: '名词性从句',
    children: [
      { id: 'noun-clause-subject', name: '主语从句' },
      { id: 'noun-clause-object', name: '宾语从句与表语从句' },
    ],
  },
  {
    id: 'non-finite',
    name: '非谓语动词',
    children: [
      { id: 'non-finite-ing', name: '动名词与现在分词' },
      { id: 'non-finite-inf', name: '不定式的用法' },
    ],
  },
  {
    id: 'subjunctive',
    name: '虚拟语气',
    children: [
      { id: 'subjunctive-if', name: 'if 条件句中的虚拟' },
      { id: 'subjunctive-wish', name: 'wish 与其他虚拟结构' },
    ],
  },
  {
    id: 'concord',
    name: '主谓一致',
    children: [
      { id: 'concord-basic', name: '主谓一致三原则' },
      { id: 'concord-special', name: '特殊主语与数量短语' },
    ],
  },
]

/** 各章节 Markdown 内容 */
export const grammarSections: GrammarSection[] = [
  {
    id: 'tenses-overview',
    title: '时态总览与用法',
    markdown:
      '## 考点精讲\n\n英语时态由“时间”与“状态”两个维度构成:过去、现在、将来 × 一般、进行、完成。专插本高频考点集中在**一般过去时与现在完成时**的对比上。\n\n- 一般过去时:强调动作发生在过去,常与 yesterday、last year 连用\n- 现在完成时:强调对现在的影响或延续至今,常与 since、so far 连用\n\n## 真题示例\n\n`He ___ (live) in Guangzhou since 2019.` → 答案 **has lived**(since 提示现在完成时)\n\n## 易错点辨析\n\n看到 ago 用过去时,看到 since 用完成时,不要只看动词本身。\n\n## 专项练习\n\n1. I ___ (see) the film twice so far.\n2. She ___ (leave) for Beijing yesterday.',
  },
  {
    id: 'tenses-perfect',
    title: '完成时态的辨析',
    markdown:
      '## 考点精讲\n\n现在完成时(have/has done)与过去完成时(had done)的关键区别在于“参照时间”:现在完成以**现在**为参照,过去完成以**过去的某一点**为参照。\n\n## 真题示例\n\n`By the time we arrived, the meeting ___ (begin).` → 答案 **had begun**\n\n## 易错点辨析\n\nby the time + 过去时,主句常用过去完成时。\n\n## 专项练习\n\n1. She ___ (finish) her homework before dinner.\n2. I ___ (never/be) to Beijing.',
  },
  {
    id: 'attributive-basic',
    title: '关系代词与关系副词',
    markdown:
      '## 考点精讲\n\n定语从句的引导词选择看“先行词”和“从句成分”:\n\n- 先行词指**人**:who(主语)/ whom(宾语)/ whose(定语)\n- 先行词指**物**:which / that\n- 表**时间**用 when,表**地点**用 where,表**原因**用 why\n\n## 真题示例\n\n`The house ___ he bought last year is very old.` → 答案 **which/that**\n\n## 易错点辨析\n\n从句里已有主语时,引导词仍不能省略(作宾语才可省)。\n\n## 专项练习\n\n1. The man ___ is talking to Li Hua is our teacher.\n2. This is the school ___ I studied ten years ago.',
  },
  {
    id: 'attributive-advanced',
    title: '非限制性定语从句',
    markdown:
      '## 考点精讲\n\n非限制性定语从句前有**逗号**,不能用 that,只能用 which(物)/ who(人);当先行词是整个句子时,只能用 **which**。\n\n## 真题示例\n\n`He passed the exam, ___ made his parents happy.` → 答案 **which**\n\n## 易错点辨析\n\n逗号后绝不写 that。\n\n## 专项练习\n\n1. My brother, ___ works in Shenzhen, will come home.\n2. She missed the bus, ___ made her late.',
  },
  {
    id: 'noun-clause-subject',
    title: '主语从句',
    markdown:
      '## 考点精讲\n\n主语从句即用一个句子充当主语,常用 **it 作形式主语**:It is important that ...。that 在主语从句中不省略。\n\n## 真题示例\n\n`It is believed ___ hard work pays off.` → 答案 **that**\n\n## 易错点辨析\n\nWhat 在从句中作成分,that 不作成分,选择时先分析从句是否完整。\n\n## 专项练习\n\n1. ___ he said at the meeting surprised everyone.\n2. It is clear ___ the plan works.',
  },
  {
    id: 'noun-clause-object',
    title: '宾语从句与表语从句',
    markdown:
      '## 考点精讲\n\n宾语从句三要素:**连接词、语序(陈述语序)、时态呼应**。表语从句位于系动词后,结构与宾语从句相同。\n\n## 真题示例\n\n`I wonder ___ he will come tomorrow.` → 答案 **whether**\n\n## 易错点辨析\n\n从句必须用陈述语序:when will he go ✗ → when he will go ✓。\n\n## 专项练习\n\n1. Can you tell me ___ the museum opens?\n2. The problem is ___ we can finish on time.',
  },
  {
    id: 'non-finite-ing',
    title: '动名词与现在分词',
    markdown:
      '## 考点精讲\n\n动名词(-ing)相当于名词,可作主语、宾语;现在分词相当于形容词/副词,表主动进行。记固定搭配:suggest/keep/finish/enjoy + doing。\n\n## 真题示例\n\n`He kept ___ (work) until midnight.` → 答案 **working**\n\n## 易错点辨析\n\n介词后永远接 -ing:be good at doing、look forward to doing。\n\n## 专项练习\n\n1. They suggested ___ (take) a taxi.\n2. The girl ___ (sit) there is my classmate.',
  },
  {
    id: 'non-finite-inf',
    title: '不定式的用法',
    markdown:
      '## 考点精讲\n\n不定式 to do 表目的或将来,记搭配:want/decide/advise sb. to do;不定式作目的状语时可用 in order to / so as to 强调。\n\n## 真题示例\n\n`He advised us ___ (set) a small goal first.` → 答案 **to set**\n\n## 易错点辨析\n\n使役动词 make/let/have 后接**不带 to** 的不定式。\n\n## 专项练习\n\n1. She decided ___ (study) abroad.\n2. The teacher made him ___ (clean) the blackboard.',
  },
  {
    id: 'subjunctive-if',
    title: 'if 条件句中的虚拟',
    markdown:
      '## 考点精讲\n\n虚拟语气核心公式:\n\n- 与现在相反:if + did/were,主句 would do\n- 与过去相反:if + had done,主句 would have done\n\n## 真题示例\n\n`If I ___ (be) you, I would study harder.` → 答案 **were**\n\n## 易错点辨析\n\n与过去相反时主句用 would have done,不要漏 have。\n\n## 专项练习\n\n1. If he had got up earlier, he ___ (catch) the train.\n2. If I had free time, I ___ (join) the club.',
  },
  {
    id: 'subjunctive-wish',
    title: 'wish 与其他虚拟结构',
    markdown:
      '## 考点精讲\n\nwish 后的从句用虚拟:与现在相反用**过去时**,与过去相反用**过去完成时**。would rather 后同样用过去时表虚拟。\n\n## 真题示例\n\n`I wish I ___ (know) the answer now.` → 答案 **knew**\n\n## 易错点辨析\n\nwish 从句不用 would + 原形表自己,would 只用于希望他人改变。\n\n## 专项练习\n\n1. She wishes she ___ (study) harder last year.\n2. I would rather you ___ (stay) at home.',
  },
  {
    id: 'concord-basic',
    title: '主谓一致三原则',
    markdown:
      '## 考点精讲\n\n主谓一致遵循三原则:语法一致(单数主语接单数谓语)、意义一致(team 等集合名词看语境)、就近原则(neither ... nor)。\n\n## 真题示例\n\n`Neither the students nor the teacher ___ (be) willing to leave.` → 答案 **is**\n\n## 易错点辨析\n\nNeither A nor B 的谓语与 **B** 保持一致。\n\n## 专项练习\n\n1. The news ___ (be) exciting.\n2. Every boy and every girl ___ (have) a book.',
  },
  {
    id: 'concord-special',
    title: '特殊主语与数量短语',
    markdown:
      '## 考点精讲\n\n特殊规则:many a / more than one + 单数名词 → 谓语单数;the + 形容词表一类人 → 谓语复数;each/either → 单数。\n\n## 真题示例\n\n`Each of the students ___ (own) a dictionary.` → 答案 **owns**\n\n## 易错点辨析\n\na number of + 复数谓语;the number of + 单数谓语。\n\n## 专项练习\n\n1. A number of students ___ (be) absent today.\n2. The number of the cars ___ (grow) fast.',
  },
]