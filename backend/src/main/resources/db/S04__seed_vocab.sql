-- ============================================================
-- S04__seed_vocab.sql
-- 目的  :导入词书(3 本)与演示单词(20 词,由前端 mocks/data/words.ts 迁移)
-- 前置  :V06__create_vocab_grammar_tables.sql 已执行
-- 幂等  :是,可重复执行(ON DUPLICATE KEY UPDATE 覆盖为种子内容)
-- 生成  :本文件由一次性脚本自动生成,生成后请勿手改
-- ============================================================

INSERT INTO `vocab_book` (`id`,`name`,`total`,`filter_level`,`sort_order`)
VALUES ('book-syllabus','广东专插本考纲词汇',3000,NULL,1) AS new
ON DUPLICATE KEY UPDATE `name`=new.`name`,`total`=new.`total`,`filter_level`=new.`filter_level`,`sort_order`=new.`sort_order`;

INSERT INTO `vocab_book` (`id`,`name`,`total`,`filter_level`,`sort_order`)
VALUES ('book-high','高频核心词汇',800,'high',2) AS new
ON DUPLICATE KEY UPDATE `name`=new.`name`,`total`=new.`total`,`filter_level`=new.`filter_level`,`sort_order`=new.`sort_order`;

INSERT INTO `vocab_book` (`id`,`name`,`total`,`filter_level`,`sort_order`)
VALUES ('book-core','核心进阶词汇',1200,'core',3) AS new
ON DUPLICATE KEY UPDATE `name`=new.`name`,`total`=new.`total`,`filter_level`=new.`filter_level`,`sort_order`=new.`sort_order`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-001','abandon','/əˈbændən/','v.','["放弃;抛弃"]','He abandoned his car in the snow.','他把车丢弃在雪地里。','They had to abandon the plan because of the heavy rain.','2023年真题·完形填空','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-002','absorb','/əbˈzɔːb/','v.','["吸收;使全神贯注"]','Plants absorb water through their roots.','植物通过根部吸收水分。','He was completely absorbed in his reading.','2022年真题·阅读理解','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-003','access','/ˈækses/','n./v.','["进入;使用权;访问"]','Every student has access to the online library.','每个学生都能访问在线图书馆。','The website gives students access to free courses.','2021年真题·语法填空','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-004','accompany','/əˈkʌmpəni/','v.','["陪伴;伴随"]','She will accompany me to the airport.','她会陪我去机场。','Children must be accompanied by an adult in the lab.','2022年真题·阅读理解','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-005','achieve','/əˈtʃiːv/','v.','["达到;实现"]','You can achieve your goal with hard work.','通过努力你能实现目标。','She achieved first place in the speech contest.','2021年真题·完形填空','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-006','aware','/əˈweə(r)/','adj.','["意识到的;知道的"]','Be aware of the traffic when crossing the street.','过马路时注意来往车辆。','Most students are not aware of their bad reading habits.','2023年真题·阅读理解','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-007','benefit','/ˈbenɪfɪt/','n./v.','["益处;受益"]','Regular exercise brings many health benefits.','规律锻炼带来许多健康益处。','Volunteering benefits both the giver and the receiver.','2021年真题·阅读理解','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-008','challenge','/ˈtʃælɪndʒ/','n./v.','["挑战;质疑"]','Learning a new language is an exciting challenge.','学一门新语言是令人兴奋的挑战。','The exam challenged every student in the room.','2022年真题·语法填空','high',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-101','academic','/ˌækəˈdemɪk/','adj.','["学术的;学业的"]','Her academic performance improved this term.','她的学业成绩这学期提高了。',NULL,NULL,'core',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-102','adapt','/əˈdæpt/','v.','["适应;改编"]','It takes time to adapt to college life.','适应大学生活需要时间。','New students must adapt to a faster learning pace.','2023年真题·作文','core',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-103','approach','/əˈprəʊtʃ/','n./v.','["方法;接近"]','We need a new approach to this problem.','我们需要解决这个问题的新方法。',NULL,NULL,'core',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-104','concentrate','/ˈkɒnsntreɪt/','v.','["集中;专心"]','I cannot concentrate with so much noise.','这么吵我无法集中注意力。','Students should concentrate on the main points.','2021年真题·阅读理解','core',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-105','decline','/dɪˈklaɪn/','v./n.','["下降;婉拒"]','Book sales declined by ten percent last year.','图书销量去年下降了 10%。',NULL,NULL,'core',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-106','essential','/ɪˈsenʃl/','adj.','["必不可少的;本质的"]','Water is essential for all living things.','水对所有生物都必不可少。','Good planning is essential before writing an essay.','2022年真题·作文','core',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-201','accomplish','/əˈkʌmplɪʃ/','v.','["完成;实现"]','She accomplished the task ahead of time.','她提前完成了任务。',NULL,NULL,'cognitive',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-202','anticipate','/ænˈtɪsɪpeɪt/','v.','["预期;预料"]','We anticipate a rise in temperature tomorrow.','我们预计明天气温会上升。',NULL,NULL,'cognitive',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-203','circumstance','/ˈsɜːkəmstəns/','n.','["情况;环境"]','Under no circumstances should you give up.','无论如何你都不该放弃。','The results depend on the circumstances of each test.','2021年真题·语法填空','cognitive',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-204','demonstrate','/ˈdemənstreɪt/','v.','["证明;演示"]','The teacher demonstrated the experiment in class.','老师在课堂上演示了实验。',NULL,NULL,'cognitive',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-205','emphasis','/ˈemfəsɪs/','n.','["强调;重点"]','The school puts emphasis on reading skills.','学校重视阅读技能。',NULL,NULL,'cognitive',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

INSERT INTO `vocab_word` (`id`,`spelling`,`phonetic`,`part_of_speech`,`meanings`,`example_sentence`,`example_translation`,`real_exam_sentence`,`real_exam_source`,`level`,`audio_url`)
VALUES ('w-206','phenomenon','/fəˈnɒmɪnən/','n.','["现象"]','The internet is a global phenomenon.','互联网是一种全球现象。',NULL,NULL,'cognitive',NULL) AS new
ON DUPLICATE KEY UPDATE `spelling`=new.`spelling`,`phonetic`=new.`phonetic`,`part_of_speech`=new.`part_of_speech`,`meanings`=new.`meanings`,`example_sentence`=new.`example_sentence`,`example_translation`=new.`example_translation`,`real_exam_sentence`=new.`real_exam_sentence`,`real_exam_source`=new.`real_exam_source`,`level`=new.`level`,`audio_url`=new.`audio_url`;

-- 词书 3 本;单词 20 个