-- ============================================================
-- S05__seed_grammar.sql
-- 目的  :导入语法目录树(6 大考点)与章节内容(12 节 markdown,前端 mocks/data/grammar-tree.ts 迁移)
-- 前置  :V06__create_vocab_grammar_tables.sql 已执行
-- 幂等  :是,可重复执行(ON DUPLICATE KEY UPDATE 覆盖为种子内容)
-- 生成  :本文件由一次性脚本自动生成,生成后请勿手改
-- ============================================================

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('tenses',NULL,'动词时态',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('tenses-overview','tenses','时态总览与用法',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('tenses-perfect','tenses','完成时态的辨析',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('attributive',NULL,'定语从句',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('attributive-basic','attributive','关系代词与关系副词',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('attributive-advanced','attributive','非限制性定语从句',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('noun-clause',NULL,'名词性从句',3) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('noun-clause-subject','noun-clause','主语从句',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('noun-clause-object','noun-clause','宾语从句与表语从句',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('non-finite',NULL,'非谓语动词',4) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('non-finite-ing','non-finite','动名词与现在分词',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('non-finite-inf','non-finite','不定式的用法',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('subjunctive',NULL,'虚拟语气',5) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('subjunctive-if','subjunctive','if 条件句中的虚拟',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('subjunctive-wish','subjunctive','wish 与其他虚拟结构',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('concord',NULL,'主谓一致',6) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('concord-basic','concord','主谓一致三原则',1) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_node` (`id`,`parent_id`,`name`,`sort_order`)
VALUES ('concord-special','concord','特殊主语与数量短语',2) AS new
ON DUPLICATE KEY UPDATE `parent_id`=new.`parent_id`,`name`=new.`name`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('tenses-overview','tenses-overview','时态总览与用法','## 考点精讲

英语时态由“时间”与“状态”两个维度构成:过去、现在、将来 × 一般、进行、完成。专插本高频考点集中在**一般过去时与现在完成时**的对比上。

- 一般过去时:强调动作发生在过去,常与 yesterday、last year 连用
- 现在完成时:强调对现在的影响或延续至今,常与 since、so far 连用

## 真题示例

`He ___ (live) in Guangzhou since 2019.` → 答案 **has lived**(since 提示现在完成时)

## 易错点辨析

看到 ago 用过去时,看到 since 用完成时,不要只看动词本身。

## 专项练习

1. I ___ (see) the film twice so far.
2. She ___ (leave) for Beijing yesterday.',1) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('tenses-perfect','tenses-perfect','完成时态的辨析','## 考点精讲

现在完成时(have/has done)与过去完成时(had done)的关键区别在于“参照时间”:现在完成以**现在**为参照,过去完成以**过去的某一点**为参照。

## 真题示例

`By the time we arrived, the meeting ___ (begin).` → 答案 **had begun**

## 易错点辨析

by the time + 过去时,主句常用过去完成时。

## 专项练习

1. She ___ (finish) her homework before dinner.
2. I ___ (never/be) to Beijing.',2) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('attributive-basic','attributive-basic','关系代词与关系副词','## 考点精讲

定语从句的引导词选择看“先行词”和“从句成分”:

- 先行词指**人**:who(主语)/ whom(宾语)/ whose(定语)
- 先行词指**物**:which / that
- 表**时间**用 when,表**地点**用 where,表**原因**用 why

## 真题示例

`The house ___ he bought last year is very old.` → 答案 **which/that**

## 易错点辨析

从句里已有主语时,引导词仍不能省略(作宾语才可省)。

## 专项练习

1. The man ___ is talking to Li Hua is our teacher.
2. This is the school ___ I studied ten years ago.',3) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('attributive-advanced','attributive-advanced','非限制性定语从句','## 考点精讲

非限制性定语从句前有**逗号**,不能用 that,只能用 which(物)/ who(人);当先行词是整个句子时,只能用 **which**。

## 真题示例

`He passed the exam, ___ made his parents happy.` → 答案 **which**

## 易错点辨析

逗号后绝不写 that。

## 专项练习

1. My brother, ___ works in Shenzhen, will come home.
2. She missed the bus, ___ made her late.',4) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('noun-clause-subject','noun-clause-subject','主语从句','## 考点精讲

主语从句即用一个句子充当主语,常用 **it 作形式主语**:It is important that ...。that 在主语从句中不省略。

## 真题示例

`It is believed ___ hard work pays off.` → 答案 **that**

## 易错点辨析

What 在从句中作成分,that 不作成分,选择时先分析从句是否完整。

## 专项练习

1. ___ he said at the meeting surprised everyone.
2. It is clear ___ the plan works.',5) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('noun-clause-object','noun-clause-object','宾语从句与表语从句','## 考点精讲

宾语从句三要素:**连接词、语序(陈述语序)、时态呼应**。表语从句位于系动词后,结构与宾语从句相同。

## 真题示例

`I wonder ___ he will come tomorrow.` → 答案 **whether**

## 易错点辨析

从句必须用陈述语序:when will he go(误)→ when he will go(正)。

## 专项练习

1. Can you tell me ___ the museum opens?
2. The problem is ___ we can finish on time.',6) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('non-finite-ing','non-finite-ing','动名词与现在分词','## 考点精讲

动名词(-ing)相当于名词,可作主语、宾语;现在分词相当于形容词/副词,表主动进行。记固定搭配:suggest/keep/finish/enjoy + doing。

## 真题示例

`He kept ___ (work) until midnight.` → 答案 **working**

## 易错点辨析

介词后永远接 -ing:be good at doing、look forward to doing。

## 专项练习

1. They suggested ___ (take) a taxi.
2. The girl ___ (sit) there is my classmate.',7) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('non-finite-inf','non-finite-inf','不定式的用法','## 考点精讲

不定式 to do 表目的或将来,记搭配:want/decide/advise sb. to do;不定式作目的状语时可用 in order to / so as to 强调。

## 真题示例

`He advised us ___ (set) a small goal first.` → 答案 **to set**

## 易错点辨析

使役动词 make/let/have 后接**不带 to** 的不定式。

## 专项练习

1. She decided ___ (study) abroad.
2. The teacher made him ___ (clean) the blackboard.',8) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('subjunctive-if','subjunctive-if','if 条件句中的虚拟','## 考点精讲

虚拟语气核心公式:

- 与现在相反:if + did/were,主句 would do
- 与过去相反:if + had done,主句 would have done

## 真题示例

`If I ___ (be) you, I would study harder.` → 答案 **were**

## 易错点辨析

与过去相反时主句用 would have done,不要漏 have。

## 专项练习

1. If he had got up earlier, he ___ (catch) the train.
2. If I had free time, I ___ (join) the club.',9) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('subjunctive-wish','subjunctive-wish','wish 与其他虚拟结构','## 考点精讲

wish 后的从句用虚拟:与现在相反用**过去时**,与过去相反用**过去完成时**。would rather 后同样用过去时表虚拟。

## 真题示例

`I wish I ___ (know) the answer now.` → 答案 **knew**

## 易错点辨析

wish 从句不用 would + 原形表自己,would 只用于希望他人改变。

## 专项练习

1. She wishes she ___ (study) harder last year.
2. I would rather you ___ (stay) at home.',10) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('concord-basic','concord-basic','主谓一致三原则','## 考点精讲

主谓一致遵循三原则:语法一致(单数主语接单数谓语)、意义一致(team 等集合名词看语境)、就近原则(neither ... nor)。

## 真题示例

`Neither the students nor the teacher ___ (be) willing to leave.` → 答案 **is**

## 易错点辨析

Neither A nor B 的谓语与 **B** 保持一致。

## 专项练习

1. The news ___ (be) exciting.
2. Every boy and every girl ___ (have) a book.',11) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

INSERT INTO `grammar_section` (`id`,`node_id`,`title`,`markdown`,`sort_order`)
VALUES ('concord-special','concord-special','特殊主语与数量短语','## 考点精讲

特殊规则:many a / more than one + 单数名词 → 谓语单数;the + 形容词表一类人 → 谓语复数;each/either → 单数。

## 真题示例

`Each of the students ___ (own) a dictionary.` → 答案 **owns**

## 易错点辨析

a number of + 复数谓语;the number of + 单数谓语。

## 专项练习

1. A number of students ___ (be) absent today.
2. The number of the cars ___ (grow) fast.',12) AS new
ON DUPLICATE KEY UPDATE `node_id`=new.`node_id`,`title`=new.`title`,`markdown`=new.`markdown`,`sort_order`=new.`sort_order`;

-- 节点 18 个;章节 12 节