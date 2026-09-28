package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.node.ObjectNode;
import com.linguacraft.backend.ai.AiGradingService;
import com.linguacraft.backend.ai.GradedWriting;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.practice.AnswerKeyDto;
import com.linguacraft.backend.dto.practice.PracticeItemResponse;
import com.linguacraft.backend.dto.practice.PracticeListItemResponse;
import com.linguacraft.backend.dto.practice.PracticeSubmitRequest;
import com.linguacraft.backend.dto.practice.ProgressRequest;
import com.linguacraft.backend.dto.practice.ProgressResponse;
import com.linguacraft.backend.dto.practice.SourceDto;
import com.linguacraft.backend.dto.practice.SubmitDetailDto;
import com.linguacraft.backend.dto.practice.SubmitResultResponse;
import com.linguacraft.backend.dto.practice.WritingDimensionDto;
import com.linguacraft.backend.entity.Mistake;
import com.linguacraft.backend.entity.PracticeProgress;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeProgressMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 刷题服务:五种题型统一的列表 / 详情 / 提交 / 作答进度(断点续答)。
 *
 * <p>批改状态机(与前端 mock 契约一致):</p>
 * <ul>
 *   <li>AI 模式 + 题库有答案:提交即数据库比对出分(Status=graded, Method=ai);</li>
 *   <li>人工模式:先 pending(Method=auto),到达阈值后由定时任务延迟比对出分(稍后出结果);</li>
 *   <li>写作 / 题库无答案(AI 模式):pending 转人工(Step 4 接入 Spring AI 后改为 AI 自动处理);</li>
 *   <li>批改完成后:错误明细自动收录错题本;提交成功后清除作答进度。</li>
 * </ul>
 */
@Service
public class PracticeService {

    private static final Logger log = LoggerFactory.getLogger(PracticeService.class);

    /** 批改状态常量 */
    private static final String STATUS_GRADED = "graded";
    private static final String STATUS_PENDING = "pending";

    /** 批改方式常量 */
    private static final String METHOD_AI = "ai";
    private static final String METHOD_AUTO = "auto";
    private static final String METHOD_MANUAL = "manual";

    /** 待批改提示文案(前端契约固定文案) */
    private static final String ESTIMATED_TIME = "预计 24 小时内出结果";

    /** 合法题型集合 */
    private static final Set<String> VALID_TYPES = Set.of("reading", "matching", "cloze", "grammar-fill", "writing");

    private final QuestionMapper questionMapper;
    private final PracticeSubmissionMapper submissionMapper;
    private final MistakeMapper mistakeMapper;
    private final PracticeProgressMapper progressMapper;
    private final UserSettingMapper userSettingMapper;
    private final ObjectiveGradingService objectiveGradingService;
    private final AiGradingService aiGradingService;
    private final ObjectMapper objectMapper;

    /** 人工模式下客观题延迟出分阈值(秒) */
    @Value("${lc.grading.manual-delay-seconds:60}")
    private long manualDelaySeconds;

    public PracticeService(QuestionMapper questionMapper,
                           PracticeSubmissionMapper submissionMapper,
                           MistakeMapper mistakeMapper,
                           PracticeProgressMapper progressMapper,
                           UserSettingMapper userSettingMapper,
                           ObjectiveGradingService objectiveGradingService,
                           AiGradingService aiGradingService,
                           ObjectMapper objectMapper) {
        this.questionMapper = questionMapper;
        this.submissionMapper = submissionMapper;
        this.mistakeMapper = mistakeMapper;
        this.progressMapper = progressMapper;
        this.userSettingMapper = userSettingMapper;
        this.objectiveGradingService = objectiveGradingService;
        this.aiGradingService = aiGradingService;
        this.objectMapper = objectMapper;
    }

    /**
     * 题型来源列表(GET /api/practice/list)。
     *
     * @param type   题型(必填)
     * @param source 来源筛选(可空,仅接受 real/teacher)
     * @param page   页码(从 1 开始)
     * @param size   每页条数(1~100)
     * @return 分页列表(含当前用户最近一次提交状态)
     */
    public PageResult<PracticeListItemResponse> list(String type, String source, long page, long size) {
        validateType(type);
        if (source != null && !source.isEmpty() && !"real".equals(source) && !"teacher".equals(source)) {
            throw new BizException(ResultCode.SOURCE_INVALID);
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        Long userId = SecurityUtils.getCurrentUserId();

        var wrapper = Wrappers.<Question>lambdaQuery()
                .eq(Question::getType, type)
                .eq(Question::getStatus, 1);
        if (source != null && !source.isEmpty()) {
            wrapper.eq(Question::getSourceType, source);
        }
        wrapper.orderByAsc(Question::getCreatedAt).orderByAsc(Question::getId);
        Page<Question> questionPage = questionMapper.selectPage(new Page<>(safePage, safeSize), wrapper);

        // 批量查询本页题目的最近一次提交(按题归并,取最新一条)
        List<String> questionIds = questionPage.getRecords().stream().map(Question::getId).toList();
        Map<String, PracticeSubmission> latestByQuestion = new HashMap<>();
        if (!questionIds.isEmpty()) {
            List<PracticeSubmission> submissions = submissionMapper.selectList(
                    Wrappers.<PracticeSubmission>lambdaQuery()
                            .eq(PracticeSubmission::getUserId, userId)
                            .in(PracticeSubmission::getQuestionId, questionIds)
                            .orderByDesc(PracticeSubmission::getCreatedAt));
            for (PracticeSubmission submission : submissions) {
                latestByQuestion.putIfAbsent(submission.getQuestionId(), submission);
            }
        }

        List<PracticeListItemResponse> items = new ArrayList<>();
        for (Question question : questionPage.getRecords()) {
            PracticeSubmission latest = latestByQuestion.get(question.getId());
            items.add(PracticeListItemResponse.builder()
                    .id(question.getId())
                    .type(question.getType())
                    .source(toSource(question))
                    .title(question.getTitle())
                    .difficulty(question.getDifficulty())
                    .tags(parseTags(question.getTags()))
                    .submitted(latest != null)
                    .submissionId(latest == null ? null : latest.getId())
                    .submissionStatus(latest == null ? null : latest.getStatus())
                    .score(latest == null ? null : latest.getScore())
                    .totalScore(latest == null ? question.getTotalScore() : latest.getTotalScore())
                    .lastSubmittedAt(latest == null ? null : latest.getSubmittedAt())
                    .build());
        }
        return new PageResult<>(items, questionPage.getTotal(), safePage, safeSize);
    }

    /**
     * 题目详情(GET /api/practice/detail/{type}/{id};不含答案与解析)。
     *
     * @param type 题型
     * @param id   题目 ID
     * @return 题目详情(下发内容来自 question.content,存储层即无答案)
     */
    public PracticeItemResponse detail(String type, String id) {
        Question question = findQuestion(type, id);
        return PracticeItemResponse.builder()
                .id(question.getId())
                .type(question.getType())
                .source(toSource(question))
                .difficulty(question.getDifficulty())
                .tags(parseTags(question.getTags()))
                .content(readTree(question.getContent()))
                .build();
    }

    /**
     * 提交作答(POST /api/practice/detail/{type}/{id}/submit)。
     *
     * @param type 题型
     * @param id   题目 ID
     * @param body 提交请求(answers/duration/gradingMethod)
     * @return 统一提交结果(三态,详见类注释状态机)
     */
    public SubmitResultResponse submit(String type, String id, PracticeSubmitRequest body) {
        Question question = findQuestion(type, id);
        if (body == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "请求体不能为空");
        }
        String gradingMethod = body.getGradingMethod();
        if (gradingMethod != null && !METHOD_AI.equals(gradingMethod) && !METHOD_MANUAL.equals(gradingMethod)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "批改方式仅接受 ai 或 manual");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        String preference = gradingMethod != null ? gradingMethod : gradingPreference(userId);
        LocalDateTime now = LocalDateTime.now();

        PracticeSubmission submission = newSubmissionBase(question, userId, body, now);

        // 写作:AI 模式由 AI 全权评分;失败或人工模式走 pending 转人工(已确认裁决)
        if ("writing".equals(question.getType())) {
            if (METHOD_AI.equals(preference)) {
                try {
                    String content = body.getAnswers() == null ? "" : body.getAnswers().path("content").asText("");
                    GradedWriting gradedWriting = aiGradingService.gradeWriting(question, content);
                    ObjectNode writingResult = objectMapper.createObjectNode();
                    writingResult.set("dimensions", objectMapper.valueToTree(gradedWriting.dimensions()));
                    if (gradedWriting.sampleAnswer() != null) {
                        writingResult.put("sampleAnswer", gradedWriting.sampleAnswer());
                    }
                    if (gradedWriting.overallComment() != null) {
                        writingResult.put("overallComment", gradedWriting.overallComment());
                    }
                    submission.setStatus(STATUS_GRADED);
                    submission.setGradingMethod(METHOD_AI);
                    submission.setScore(gradedWriting.score());
                    submission.setWritingResult(writingResult.toString());
                    submission.setGradedAt(now);
                    submissionMapper.insert(submission);
                    clearProgress(userId, id);
                    return buildResult(submission);
                } catch (BizException e) {
                    log.warn("AI 作文评分失败,降级为人工 pending: {}", e.getMessage());
                }
            }
            submission.setStatus(STATUS_PENDING);
            submission.setGradingMethod(METHOD_MANUAL);
            submission.setEstimatedTime(ESTIMATED_TIME);
            submissionMapper.insert(submission);
            clearProgress(userId, id);
            return buildResult(submission);
        }

        boolean instant = METHOD_AI.equals(preference);
        boolean hasAnswer = Boolean.TRUE.equals(question.getHasAnswer());

        if (instant && hasAnswer) {
            // AI 模式 + 题库有答案:提交即数据库比对,立即出分
            AnswerKeyDto key = parseAnswerKey(question);
            GradedObjective graded = objectiveGradingService.grade(
                    question, key, toAnswerMap(body.getAnswers()), caseSensitive(userId));
            submission.setStatus(STATUS_GRADED);
            submission.setGradingMethod(METHOD_AI);
            submission.setScore(graded.score());
            submission.setCorrectCount(graded.correctCount());
            submission.setTotalCount(graded.totalCount());
            submission.setDetails(writeJson(graded.details()));
            submission.setGradedAt(now);
            submissionMapper.insert(submission);
            recordMistakes(userId, question, graded.details());
            clearProgress(userId, id);
            return buildResult(submission);
        }

        if (instant) {
            // AI 模式但题库无答案:由 AI 批改;失败降级转人工 pending(已确认裁决)
            try {
                GradedObjective graded = aiGradingService.gradeObjective(question, toAnswerMap(body.getAnswers()));
                submission.setStatus(STATUS_GRADED);
                submission.setGradingMethod(METHOD_AI);
                submission.setScore(graded.score());
                submission.setCorrectCount(graded.correctCount());
                submission.setTotalCount(graded.totalCount());
                submission.setDetails(writeJson(graded.details()));
                submission.setGradedAt(now);
                submissionMapper.insert(submission);
                recordMistakes(userId, question, graded.details());
                clearProgress(userId, id);
                return buildResult(submission);
            } catch (BizException e) {
                log.warn("AI 客观题批改失败,降级转人工 pending: {}", e.getMessage());
                submission.setStatus(STATUS_PENDING);
                submission.setGradingMethod(METHOD_MANUAL);
                submission.setEstimatedTime(ESTIMATED_TIME);
                submission.setTotalCount(countObjectiveItems(question));
                submissionMapper.insert(submission);
                clearProgress(userId, id);
                return buildResult(submission);
            }
        }

        // 人工模式:先 pending(稍后出结果);有答案者由定时任务延迟比对出分,无答案保持 pending 转人工
        submission.setStatus(STATUS_PENDING);
        submission.setGradingMethod(METHOD_AUTO);
        submission.setEstimatedTime(ESTIMATED_TIME);
        submission.setTotalCount(countObjectiveItems(question));
        submissionMapper.insert(submission);
        clearProgress(userId, id);
        return buildResult(submission);
    }

    /**
     * 读取作答进度(扩展契约 E1)。
     *
     * @param type 题型
     * @param id   题目 ID
     * @return 进度(无进度时返回 null,接口 data 为 null)
     */
    public ProgressResponse getProgress(String type, String id) {
        findQuestion(type, id);
        Long userId = SecurityUtils.getCurrentUserId();
        PracticeProgress progress = progressMapper.selectOne(
                Wrappers.<PracticeProgress>lambdaQuery()
                        .eq(PracticeProgress::getUserId, userId)
                        .eq(PracticeProgress::getQuestionId, id));
        if (progress == null) {
            return null;
        }
        return ProgressResponse.builder()
                .answers(readTree(progress.getAnswers()))
                .duration(progress.getDuration())
                .build();
    }

    /**
     * 保存作答进度(覆盖式,扩展契约 E1)。
     *
     * @param type    题型
     * @param id      题目 ID
     * @param request 进度(answers/duration)
     */
    public void saveProgress(String type, String id, ProgressRequest request) {
        findQuestion(type, id);
        Long userId = SecurityUtils.getCurrentUserId();
        String answersJson = request == null || request.getAnswers() == null ? "{}" : writeJson(request.getAnswers());
        Integer duration = request == null || request.getDuration() == null ? 0 : request.getDuration();

        PracticeProgress progress = progressMapper.selectOne(
                Wrappers.<PracticeProgress>lambdaQuery()
                        .eq(PracticeProgress::getUserId, userId)
                        .eq(PracticeProgress::getQuestionId, id));
        if (progress == null) {
            progress = new PracticeProgress();
            progress.setUserId(userId);
            progress.setQuestionId(id);
            progress.setType(type);
            progress.setAnswers(answersJson);
            progress.setDuration(duration);
            progressMapper.insert(progress);
        } else {
            progress.setAnswers(answersJson);
            progress.setDuration(duration);
            progressMapper.updateById(progress);
        }
    }

    /**
     * 处理到期的待批改提交(由定时任务调用):人工模式下有答案的客观题延迟比对出分。
     *
     * <p>无答案题目保持 pending 转人工;作文(pending/manual)不在扫描范围。</p>
     *
     * @return 本次完成批改的提交数
     */
    public int processDuePendingSubmissions() {
        LocalDateTime deadline = LocalDateTime.now().minusSeconds(manualDelaySeconds);
        List<PracticeSubmission> due = submissionMapper.selectList(
                Wrappers.<PracticeSubmission>lambdaQuery()
                        .eq(PracticeSubmission::getStatus, STATUS_PENDING)
                        .eq(PracticeSubmission::getGradingMethod, METHOD_AUTO)
                        .le(PracticeSubmission::getSubmittedAt, deadline));
        int processed = 0;
        for (PracticeSubmission submission : due) {
            Question question = questionMapper.selectById(submission.getQuestionId());
            if (question == null || !Boolean.TRUE.equals(question.getHasAnswer())) {
                continue; // 无答案:保持 pending 转人工
            }
            try {
                AnswerKeyDto key = parseAnswerKey(question);
                GradedObjective graded = objectiveGradingService.grade(
                        question, key, parseAnswers(submission.getAnswers()), caseSensitive(submission.getUserId()));
                submissionMapper.update(null,
                        Wrappers.<PracticeSubmission>lambdaUpdate()
                                .eq(PracticeSubmission::getId, submission.getId())
                                .set(PracticeSubmission::getStatus, STATUS_GRADED)
                                .set(PracticeSubmission::getScore, graded.score())
                                .set(PracticeSubmission::getCorrectCount, graded.correctCount())
                                .set(PracticeSubmission::getTotalCount, graded.totalCount())
                                .set(PracticeSubmission::getDetails, writeJson(graded.details()))
                                .set(PracticeSubmission::getGradedAt, LocalDateTime.now())
                                .set(PracticeSubmission::getEstimatedTime, null));
                recordMistakes(submission.getUserId(), question, graded.details());
                processed++;
            } catch (Exception e) {
                log.error("延迟批改失败: submissionId={}", submission.getId(), e);
            }
        }
        return processed;
    }

    /**
     * 智能练习(POST /api/practice/smart):优先未做过的题目,按题型得分率低者加权(默认口径)。
     *
     * @return 推荐的一道题目(ForAnswer,type 取自题目本身)
     */
    public PracticeItemResponse smart() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Question> all = questionMapper.selectList(
                Wrappers.<Question>lambdaQuery().eq(Question::getStatus, 1));
        if (all.isEmpty()) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "题库为空");
        }
        Set<String> doneQuestionIds = submissionMapper.selectList(
                        Wrappers.<PracticeSubmission>lambdaQuery().eq(PracticeSubmission::getUserId, userId))
                .stream().map(PracticeSubmission::getQuestionId).collect(Collectors.toSet());
        List<Question> candidates = all.stream()
                .filter(question -> !doneQuestionIds.contains(question.getId()))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            candidates = all; // 全部做过:从全量中随机
        }
        Question picked = weightedRandom(candidates, typeWeights(userId));
        return detail(picked.getType(), picked.getId());
    }

    /**
     * 计算题型权重:用户得分率越低权重越高(无数据按 0.5 估)。
     *
     * @param userId 用户 ID
     * @return 题型 -> 权重
     */
    private Map<String, Double> typeWeights(Long userId) {
        List<PracticeSubmission> gradedSubmissions = submissionMapper.selectList(
                Wrappers.<PracticeSubmission>lambdaQuery()
                        .eq(PracticeSubmission::getUserId, userId)
                        .eq(PracticeSubmission::getStatus, STATUS_GRADED));
        Map<String, BigDecimal> scoreSum = new HashMap<>();
        Map<String, BigDecimal> totalSum = new HashMap<>();
        for (PracticeSubmission submission : gradedSubmissions) {
            if (submission.getScore() == null || submission.getTotalScore() == null
                    || submission.getTotalScore().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            scoreSum.merge(submission.getType(), submission.getScore(), BigDecimal::add);
            totalSum.merge(submission.getType(), submission.getTotalScore(), BigDecimal::add);
        }
        Map<String, Double> weights = new HashMap<>();
        for (String type : VALID_TYPES) {
            BigDecimal total = totalSum.get(type);
            double accuracy = total == null ? 0.5
                    : scoreSum.get(type).doubleValue() / total.doubleValue();
            weights.put(type, 1.0 + (1.0 - Math.max(0.0, Math.min(1.0, accuracy))));
        }
        return weights;
    }

    /**
     * 按题型权重随机选择题目。
     *
     * @param candidates 候选题目
     * @param weights    题型权重
     * @return 选中的题目
     */
    private Question weightedRandom(List<Question> candidates, Map<String, Double> weights) {
        double totalWeight = candidates.stream()
                .mapToDouble(question -> weights.getOrDefault(question.getType(), 1.0)).sum();
        double random = Math.random() * totalWeight;
        double cumulative = 0;
        for (Question question : candidates) {
            cumulative += weights.getOrDefault(question.getType(), 1.0);
            if (random <= cumulative) {
                return question;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    // ==================== 内部工具 ====================

    /**
     * 校验题型参数(非法抛 code 1004)。
     *
     * @param type 题型
     */
    private void validateType(String type) {
        if (type == null || !VALID_TYPES.contains(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
    }

    /**
     * 查询题目并校验(不存在/题型不符/已停用均按 404 处理)。
     *
     * @param type 题型
     * @param id   题目 ID
     * @return 题目实体
     */
    private Question findQuestion(String type, String id) {
        validateType(type);
        Question question = questionMapper.selectById(id);
        if (question == null || !type.equals(question.getType()) || question.getStatus() == null || question.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "题目不存在");
        }
        return question;
    }

    /**
     * 构建来源 DTO(sourceMeta 存的是完整 source 对象,拆出 meta 下发给前端契约结构)。
     *
     * @param question 题目实体
     * @return 来源 DTO
     */
    private SourceDto toSource(Question question) {
        JsonNode node = readTree(question.getSourceMeta());
        return SourceDto.builder()
                .type(node != null && node.hasNonNull("type") ? node.get("type").asText() : question.getSourceType())
                .meta(node == null ? null : node.get("meta"))
                .build();
    }

    /**
     * 解析标签 JSON。
     *
     * @param tagsJson 标签数组 JSON
     * @return 标签列表
     */
    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("标签 JSON 解析失败: {}", tagsJson);
            return List.of();
        }
    }

    /**
     * 解析答案集合(question.answer_key)。
     *
     * @param question 题目实体
     * @return 答案集合(解析失败抛服务异常)
     */
    private AnswerKeyDto parseAnswerKey(Question question) {
        try {
            return objectMapper.readValue(question.getAnswerKey(), AnswerKeyDto.class);
        } catch (Exception e) {
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "题库答案数据异常");
        }
    }

    /**
     * 答案项数量(用于 pending 响应的总题数)。
     *
     * @param question 题目实体
     * @return 题数(无法解析时返回 0)
     */
    private int countObjectiveItems(Question question) {
        if (!Boolean.TRUE.equals(question.getHasAnswer()) || question.getAnswerKey() == null) {
            return 0;
        }
        try {
            AnswerKeyDto key = objectMapper.readValue(question.getAnswerKey(), AnswerKeyDto.class);
            if (key.getQuestions() != null) {
                return key.getQuestions().size();
            }
            if (key.getBlanks() != null) {
                return key.getBlanks().size();
            }
        } catch (Exception ignored) {
            // 忽略:数量仅用于展示
        }
        return 0;
    }

    /**
     * 请求 answers JSON -> Map(题号 -> 作答);非数字键跳过。
     *
     * @param answers 作答 JSON
     * @return 作答映射
     */
    private Map<Integer, String> toAnswerMap(JsonNode answers) {
        Map<Integer, String> map = new HashMap<>();
        if (answers == null || !answers.isObject()) {
            return map;
        }
        // Jackson 3:JsonNode.fields() 已改名为 properties(),返回 Set<Entry<String,JsonNode>>
        answers.properties().forEach(entry -> {
            try {
                map.put(Integer.parseInt(entry.getKey()), entry.getValue().asText(""));
            } catch (NumberFormatException ignored) {
                // 非数字键(如写作 content)不参与客观题判分
            }
        });
        return map;
    }

    /**
     * 存储的 answers JSON -> Map。
     *
     * @param answersJson 存储的作答 JSON
     * @return 作答映射
     */
    private Map<Integer, String> parseAnswers(String answersJson) {
        return toAnswerMap(readTree(answersJson));
    }

    /**
     * 读取用户"语法填空区分大小写"判分偏好。
     *
     * @param userId 用户 ID
     * @return true 区分大小写(默认 false)
     */
    private boolean caseSensitive(Long userId) {
        UserSetting setting = userSettingMapper.selectById(userId);
        return setting != null && Boolean.TRUE.equals(setting.getGrammarFillCaseSensitive());
    }

    /**
     * 读取用户"批改方式"偏好(默认 ai)。
     *
     * @param userId 用户 ID
     * @return ai/manual
     */
    private String gradingPreference(Long userId) {
        UserSetting setting = userSettingMapper.selectById(userId);
        return setting != null && METHOD_MANUAL.equals(setting.getGradingMethod()) ? METHOD_MANUAL : METHOD_AI;
    }

    /**
     * 构建提交记录基础字段(不含批改结果)。
     *
     * @param question 题目
     * @param userId   用户 ID
     * @param body     提交请求
     * @param now      提交时间
     * @return 提交实体
     */
    private PracticeSubmission newSubmissionBase(Question question, Long userId, PracticeSubmitRequest body, LocalDateTime now) {
        PracticeSubmission submission = new PracticeSubmission();
        submission.setUserId(userId);
        submission.setQuestionId(question.getId());
        submission.setType(question.getType());
        submission.setTitle(question.getTitle());
        submission.setSourceType(question.getSourceType());
        submission.setSourceMeta(question.getSourceMeta());
        submission.setTotalScore(question.getTotalScore());
        submission.setAnswers(body.getAnswers() == null ? "{}" : writeJson(body.getAnswers()));
        submission.setDuration(body.getDuration() == null ? 0 : body.getDuration());
        submission.setSubmittedAt(now);
        return submission;
    }

    /**
     * 提交实体 -> 统一提交结果响应(details 仅 graded 下发;写作扩展字段从 writingResult 提取)。
     *
     * <p>public 供“我的提交”详情(GET /api/user/submissions/:id)复用。</p>
     *
     * @param submission 提交实体
     * @return 响应
     */
    public SubmitResultResponse buildResult(PracticeSubmission submission) {
        SubmitResultResponse.SubmitResultResponseBuilder builder = SubmitResultResponse.builder()
                .submissionId(submission.getId())
                .type(submission.getType())
                .status(submission.getStatus())
                .gradingMethod(submission.getGradingMethod())
                .score(submission.getScore())
                .totalScore(submission.getTotalScore())
                .correctCount(submission.getCorrectCount())
                .totalCount(submission.getTotalCount())
                .estimatedTime(submission.getEstimatedTime());
        if (STATUS_GRADED.equals(submission.getStatus()) && submission.getDetails() != null) {
            try {
                builder.details(objectMapper.readValue(submission.getDetails(), new TypeReference<List<SubmitDetailDto>>() {
                }));
            } catch (Exception e) {
                log.warn("批改明细解析失败: submissionId={}", submission.getId());
            }
        }
        if ("writing".equals(submission.getType()) && submission.getWritingResult() != null) {
            JsonNode writing = readTree(submission.getWritingResult());
            if (writing != null) {
                if (writing.hasNonNull("dimensions")) {
                    builder.dimensions(objectMapper.convertValue(writing.get("dimensions"),
                            new TypeReference<List<WritingDimensionDto>>() {
                            }));
                }
                if (writing.hasNonNull("sampleAnswer")) {
                    builder.sampleAnswer(writing.get("sampleAnswer").asText());
                }
                if (writing.hasNonNull("overallComment")) {
                    builder.overallComment(writing.get("overallComment").asText());
                }
            }
        }
        return builder.build();
    }

    /**
     * 批改完成后收录错题(追加式,仅收录答错明细)。
     *
     * @param userId  用户 ID
     * @param question 题目
     * @param details 批改明细
     */
    private void recordMistakes(Long userId, Question question, List<SubmitDetailDto> details) {
        for (SubmitDetailDto detail : details) {
            if (Boolean.TRUE.equals(detail.getIsCorrect())) {
                continue;
            }
            Mistake mistake = new Mistake();
            mistake.setUserId(userId);
            mistake.setQuestionId(question.getId());
            mistake.setType(question.getType());
            mistake.setSourceType(question.getSourceType());
            mistake.setSourceMeta(question.getSourceMeta());
            mistake.setStem(buildStem(question, detail.getId()));
            mistake.setUserAnswer(detail.getUserAnswer() == null ? "" : detail.getUserAnswer());
            mistake.setCorrectAnswer(detail.getCorrectAnswer() == null ? "" : detail.getCorrectAnswer());
            mistake.setExplanation(detail.getExplanation());
            mistakeMapper.insert(mistake);
        }
    }

    /**
     * 构建题干摘要:阅读取对应题干;其余题型为"标题 第 N 空"(与前端 mock 口径一致)。
     *
     * @param question 题目
     * @param id       题号/空位编号
     * @return 题干摘要
     */
    private String buildStem(Question question, Integer id) {
        if ("reading".equals(question.getType())) {
            JsonNode content = readTree(question.getContent());
            if (content != null && content.has("questions")) {
                for (JsonNode item : content.get("questions")) {
                    if (item.path("id").asInt() == id) {
                        return abbreviate(item.path("stem").asText(""), 500);
                    }
                }
            }
        }
        return abbreviate(question.getTitle() + " 第 " + id + " 空", 500);
    }

    /**
     * 截断超长文本(适配 varchar 长度上限)。
     *
     * @param text   原文
     * @param maxLen 最大长度
     * @return 截断后文本
     */
    private String abbreviate(String text, int maxLen) {
        return text != null && text.length() > maxLen ? text.substring(0, maxLen) : text;
    }

    /**
     * 清除作答进度(提交成功后调用)。
     *
     * @param userId     用户 ID
     * @param questionId 题目 ID
     */
    private void clearProgress(Long userId, String questionId) {
        progressMapper.delete(Wrappers.<PracticeProgress>lambdaQuery()
                .eq(PracticeProgress::getUserId, userId)
                .eq(PracticeProgress::getQuestionId, questionId));
    }

    /**
     * JSON 字符串解析(容错:失败返回 null)。
     *
     * @param json JSON 字符串
     * @return JsonNode 或 null
     */
    private JsonNode readTree(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 对象序列化为 JSON 字符串。
     *
     * @param value 对象
     * @return JSON 字符串
     */
    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "数据序列化失败");
        }
    }
}