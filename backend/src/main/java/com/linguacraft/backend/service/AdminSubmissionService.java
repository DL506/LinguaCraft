package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminSubmissionDetailDto;
import com.linguacraft.backend.dto.admin.AdminSubmissionListItemDto;
import com.linguacraft.backend.dto.admin.ManualGradeRequest;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.QuestionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.util.PracticeRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端批改服务(阶段 A4):待批改队列、判分材料、人工判分落库。
 *
 * <p>存在的理由:练习提交里 {@code grading_method='manual'} 的那一批(写作题、AI 批改失败件)
 * 此前<b>没有任何代码路径会把它们推进到已批改</b> —— 定时任务 {@code PendingGradingJob} 只扫
 * {@code auto} 那批且跳过题库无答案的题。本服务补上人工这一环,闭掉"学生永远看到待批改"的断链。</p>
 *
 * <p>落库口径刻意与自动判分保持一致,因此学生端「我的提交」无需改动即可显示结果:
 * {@code status='graded'} + {@code grading_method='manual'} + {@code score/correct_count/total_count}
 * + {@code details}(逐题,形状同 {@code SubmitDetailDto})或 {@code writing_result}(四维/范文/总评),
 * 并把 {@code estimated_time} 置空(该字段在学生端无条件下发,残留会显示"预计 24 小时内出结果")。</p>
 *
 * <p>并发保护:更新带 {@code status='pending'} 条件(CAS),若定时任务或另一名管理员已抢先处理,
 * 影响行数为 0 即拒绝,不会覆盖已有判分。</p>
 */
@Service
public class AdminSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(AdminSubmissionService.class);

    /** 待批改 */
    private static final String STATUS_PENDING = "pending";

    /** 已批改 */
    private static final String STATUS_GRADED = "graded";

    /** 人工批改 */
    private static final String METHOD_MANUAL = "manual";

    /** 列表可选的状态筛选 */
    private static final Set<String> VALID_STATUS = Set.of(STATUS_PENDING, STATUS_GRADED, "partially_graded");

    private final PracticeSubmissionMapper submissionMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public AdminSubmissionService(PracticeSubmissionMapper submissionMapper,
                                  QuestionMapper questionMapper,
                                  UserMapper userMapper,
                                  ObjectMapper objectMapper) {
        this.submissionMapper = submissionMapper;
        this.questionMapper = questionMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 查询 ====================

    /**
     * 批改队列分页(GET /api/admin/submissions)。
     *
     * <p>默认按提交时间<b>正序</b>(等得最久的排前面),与题库列表的倒序不同。</p>
     *
     * @param status 状态筛选(可空)
     * @param type   题型筛选(可空)
     * @param method 批改方式筛选(可空;传 manual 即只看真正的断链件)
     * @param keyword 标题/题目 ID 关键词(可空)
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    public PageResult<AdminSubmissionListItemDto> list(String status, String type, String method,
                                                       String keyword, long page, long size) {
        if (status != null && !status.isBlank() && !VALID_STATUS.contains(status)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "status 仅接受 pending/graded/partially_graded");
        }
        if (type != null && !type.isBlank() && !PracticeRules.isValidType(type)) {
            throw new BizException(ResultCode.TYPE_INVALID);
        }
        if (method != null && !method.isBlank() && !Set.of("ai", "auto", METHOD_MANUAL).contains(method)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "method 仅接受 ai/auto/manual");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<PracticeSubmission>lambdaQuery();
        if (status != null && !status.isBlank()) {
            wrapper.eq(PracticeSubmission::getStatus, status);
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(PracticeSubmission::getType, type);
        }
        if (method != null && !method.isBlank()) {
            wrapper.eq(PracticeSubmission::getGradingMethod, method);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(PracticeSubmission::getTitle, kw).or().like(PracticeSubmission::getQuestionId, kw));
        }
        wrapper.orderByAsc(PracticeSubmission::getSubmittedAt).orderByAsc(PracticeSubmission::getId);

        Page<PracticeSubmission> result = submissionMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<Long, String> nicknames = loadNicknames(result.getRecords());

        List<AdminSubmissionListItemDto> items = new ArrayList<>();
        for (PracticeSubmission submission : result.getRecords()) {
            items.add(AdminSubmissionListItemDto.builder()
                    .id(submission.getId())
                    .userId(submission.getUserId())
                    .nickname(nicknames.get(submission.getUserId()))
                    .type(submission.getType())
                    .title(submission.getTitle())
                    .questionId(submission.getQuestionId())
                    .status(submission.getStatus())
                    .gradingMethod(submission.getGradingMethod())
                    .score(submission.getScore())
                    .totalScore(submission.getTotalScore())
                    .answeredCount(countAnswered(submission))
                    .waitingMinutes(waitingMinutes(submission))
                    .submittedAt(submission.getSubmittedAt())
                    .gradedAt(submission.getGradedAt())
                    .build());
        }
        return new PageResult<>(items, result.getTotal(), safePage, safeSize);
    }

    /**
     * 判分材料(GET /api/admin/submissions/{id}):提交 + 作答 + 题目内容 + 参考答案 + 既有判分。
     *
     * @param id 提交 ID
     * @return 详情
     */
    public AdminSubmissionDetailDto detail(Long id) {
        PracticeSubmission submission = findSubmission(id);
        Question question = questionMapper.selectById(submission.getQuestionId());
        User user = userMapper.selectById(submission.getUserId());
        return AdminSubmissionDetailDto.builder()
                .id(submission.getId())
                .userId(submission.getUserId())
                .nickname(user == null ? null : user.getNickname())
                .type(submission.getType())
                .title(submission.getTitle())
                .questionId(submission.getQuestionId())
                .status(submission.getStatus())
                .gradingMethod(submission.getGradingMethod())
                .score(submission.getScore())
                .totalScore(submission.getTotalScore())
                .answers(readTree(submission.getAnswers()))
                .questionContent(question == null ? null : readTree(question.getContent()))
                .answerKey(question == null ? null : readTree(question.getAnswerKey()))
                .questionHasAnswer(question != null && Boolean.TRUE.equals(question.getHasAnswer()))
                .details(readTree(submission.getDetails()))
                .writingResult(readTree(submission.getWritingResult()))
                .estimatedTime(submission.getEstimatedTime())
                .duration(submission.getDuration())
                .waitingMinutes(waitingMinutes(submission))
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .build();
    }

    // ==================== 人工判分 ====================

    /**
     * 人工判分落库(POST /api/admin/submissions/{id}/grade)。
     *
     * @param id      提交 ID
     * @param request 判分请求
     * @return 落库后的详情(学生端将立即读到已批改结果)
     */
    @Transactional
    public AdminSubmissionDetailDto grade(Long id, ManualGradeRequest request) {
        PracticeSubmission submission = findSubmission(id);
        if (!STATUS_PENDING.equals(submission.getStatus())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该提交当前状态为 " + submission.getStatus() + ",只有待批改(pending)可人工判分");
        }
        boolean writing = "writing".equals(submission.getType());
        BigDecimal cap = submission.getTotalScore();

        String detailsJson = null;
        String writingJson = null;
        Integer correctCount = null;
        Integer totalCount = null;
        BigDecimal score;

        if (writing) {
            writingJson = buildWritingResult(request, cap);
            score = request.getScore() != null ? request.getScore() : dimensionSum(request.getDimensions());
        } else {
            Map<Integer, String> answered = parseAnswers(submission.getAnswers());
            ObjectNode details = buildDetails(request, answered);
            detailsJson = details.toString();
            totalCount = answered.size();
            correctCount = countCorrect(details);
            score = request.getScore() != null ? request.getScore() : objectiveScore(submission, correctCount, cap);
        }
        if (score != null && cap != null && score.compareTo(cap) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "得分 " + score + " 超过卷面总分 " + cap);
        }
        if (score != null && score.signum() < 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "得分不能为负");
        }

        // CAS:仅当仍处于 pending 才写入,避免覆盖定时任务或另一名管理员的结果
        var update = Wrappers.<PracticeSubmission>lambdaUpdate()
                .eq(PracticeSubmission::getId, id)
                .eq(PracticeSubmission::getStatus, STATUS_PENDING)
                .set(PracticeSubmission::getStatus, STATUS_GRADED)
                .set(PracticeSubmission::getGradingMethod, METHOD_MANUAL)
                .set(PracticeSubmission::getScore, score)
                .set(PracticeSubmission::getCorrectCount, correctCount)
                .set(PracticeSubmission::getTotalCount, totalCount)
                .set(PracticeSubmission::getDetails, detailsJson)
                .set(PracticeSubmission::getWritingResult, writingJson)
                .set(PracticeSubmission::getGradedAt, LocalDateTime.now())
                // 待批改说明必须清空:学生端详情无条件下发该字段
                .set(PracticeSubmission::getEstimatedTime, null);
        int rows = submissionMapper.update(null, update);
        if (rows == 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该提交已被处理,请刷新后重试");
        }
        log.info("管理端人工批改: submissionId={} type={} score={} correct={}/{}",
                id, submission.getType(), score, correctCount, totalCount);
        return detail(id);
    }

    // ==================== 判分计算 ====================

    /**
     * 组装作文批改结果 JSON(维度/范文/总评),并校验维度分非负。
     *
     * @param request 判分请求
     * @param cap     卷面总分
     * @return writing_result JSON
     */
    private String buildWritingResult(ManualGradeRequest request, BigDecimal cap) {
        List<ManualGradeRequest.Dimension> dimensions = request.getDimensions();
        if (dimensions == null || dimensions.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "作文题必须提交至少一个评分维度(dimensions)");
        }
        ObjectNode root = objectMapper.createObjectNode();
        var array = root.putArray("dimensions");
        for (ManualGradeRequest.Dimension dimension : dimensions) {
            if (dimension.getScore() == null || dimension.getScore().signum() < 0) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "维度「" + dimension.getName() + "」得分不能为空或为负");
            }
            ObjectNode node = array.addObject();
            node.put("name", dimension.getName());
            node.put("score", dimension.getScore().setScale(1, RoundingMode.HALF_UP));
            node.put("comment", dimension.getComment() == null ? "" : dimension.getComment());
        }
        if (request.getSampleAnswer() != null && !request.getSampleAnswer().isBlank()) {
            root.put("sampleAnswer", request.getSampleAnswer());
        }
        if (request.getOverallComment() != null && !request.getOverallComment().isBlank()) {
            root.put("overallComment", request.getOverallComment());
        }
        BigDecimal sum = dimensionSum(dimensions);
        if (cap != null && sum.compareTo(cap) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "维度分之和 " + sum + " 超过卷面总分 " + cap + ",请调整各维度得分");
        }
        return root.toString();
    }

    /**
     * 维度分之和(保留 1 位小数)。
     *
     * @param dimensions 维度列表
     * @return 合计
     */
    private BigDecimal dimensionSum(List<ManualGradeRequest.Dimension> dimensions) {
        return dimensions.stream()
                .map(ManualGradeRequest.Dimension::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 组装逐题判分明细 JSON 数组,并核对题号集合与学生作答一致。
     *
     * @param request  判分请求
     * @param answered 学生作答(题号 -> 原文)
     * @return details 数组节点
     */
    private ObjectNode buildDetails(ManualGradeRequest request, Map<Integer, String> answered) {
        List<ManualGradeRequest.Item> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "客观题必须提交逐题判定(items)");
        }
        if (answered.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该提交没有可判分的作答记录");
        }
        Set<Integer> requestIds = new HashSet<>();
        for (ManualGradeRequest.Item item : items) {
            if (!answered.containsKey(item.getId())) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "items 含题号 " + item.getId() + ",但学生并未作答该题");
            }
            if (!requestIds.add(item.getId())) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "items 存在重复题号:" + item.getId());
            }
        }
        if (requestIds.size() != answered.size()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "items 题号集合必须与学生作答一致(作答:" + new ArrayList<>(answered.keySet())
                            + ",判定:" + new ArrayList<>(requestIds) + ")");
        }

        // 数组顺序按题号升序,与学生端逐题表一致
        ObjectNode root = objectMapper.createObjectNode();
        var array = root.putArray("details");
        answered.keySet().stream().sorted(Comparator.naturalOrder()).forEach(id -> {
            ManualGradeRequest.Item matched = items.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElseThrow();
            ObjectNode node = array.addObject();
            node.put("id", id);
            node.put("userAnswer", answered.get(id));
            node.put("correctAnswer", matched.getCorrectAnswer() == null ? "" : matched.getCorrectAnswer());
            node.put("isCorrect", Boolean.TRUE.equals(matched.getIsCorrect()));
            node.put("explanation", matched.getExplanation() == null ? "" : matched.getExplanation());
        });
        return root;
    }

    /**
     * 统计明细中的答对数。
     *
     * @param root 含 details 数组的节点
     * @return 正确数
     */
    private int countCorrect(ObjectNode root) {
        int count = 0;
        for (JsonNode node : root.get("details")) {
            if (node.path("isCorrect").asBoolean(false)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 客观题得分:单题分值 × 正确数,封顶卷面总分(与自动判分同口径)。
     *
     * @param submission    提交
     * @param correctCount  正确数
     * @param cap           卷面总分
     * @return 得分
     */
    private BigDecimal objectiveScore(PracticeSubmission submission, int correctCount, BigDecimal cap) {
        BigDecimal perQuestion = BigDecimal.ONE;
        Question question = questionMapper.selectById(submission.getQuestionId());
        if (question != null && question.getPerQuestionScore() != null) {
            perQuestion = question.getPerQuestionScore();
        } else if (PracticeRules.perQuestionScore(submission.getType()) != null) {
            perQuestion = PracticeRules.perQuestionScore(submission.getType());
        }
        BigDecimal score = perQuestion.multiply(BigDecimal.valueOf(correctCount)).setScale(1, RoundingMode.HALF_UP);
        return cap != null && score.compareTo(cap) > 0 ? cap.setScale(1, RoundingMode.HALF_UP) : score;
    }

    // ==================== 小工具 ====================

    /**
     * 解析学生作答为(题号 -> 原文),按题号升序。
     *
     * @param answersJson answers 列
     * @return 题号与作答
     */
    private Map<Integer, String> parseAnswers(String answersJson) {
        JsonNode node = readTree(answersJson);
        Map<Integer, String> answers = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return answers;
        }
        for (var entry : node.properties()) {
            try {
                int id = Integer.parseInt(entry.getKey());
                String value = entry.getValue().asText("");
                answers.put(id, value);
            } catch (NumberFormatException ignored) {
                // 写作题的 answers 形如 {content:"..."},非数字键在此被跳过,由写作分支单独处理
            }
        }
        return answers;
    }

    /**
     * 已作答题数(写作按 1 题计)。
     *
     * @param submission 提交
     * @return 题数
     */
    private int countAnswered(PracticeSubmission submission) {
        if ("writing".equals(submission.getType())) {
            JsonNode node = readTree(submission.getAnswers());
            return node != null && node.path("content").asText("").isBlank() ? 0 : 1;
        }
        return parseAnswers(submission.getAnswers()).size();
    }

    /**
     * 自提交起的等待分钟数。
     *
     * @param submission 提交
     * @return 分钟数(提交时间为空时返回 null)
     */
    private Long waitingMinutes(PracticeSubmission submission) {
        return submission.getSubmittedAt() == null
                ? null
                : Duration.between(submission.getSubmittedAt(), LocalDateTime.now()).toMinutes();
    }

    /**
     * 批量取提交用户昵称(避免逐行查用户表)。
     *
     * @param submissions 当前页提交
     * @return 用户 ID -> 昵称
     */
    private Map<Long, String> loadNicknames(List<PracticeSubmission> submissions) {
        Map<Long, String> result = new HashMap<>();
        List<Long> userIds = submissions.stream().map(PracticeSubmission::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return result;
        }
        for (User user : userMapper.selectBatchIds(userIds)) {
            result.put(user.getId(), user.getNickname());
        }
        return result;
    }

    /**
     * 按 ID 取提交,不存在即 404。
     *
     * @param id 提交 ID
     * @return 提交实体
     */
    private PracticeSubmission findSubmission(Long id) {
        PracticeSubmission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "提交不存在");
        }
        return submission;
    }

    /**
     * 解析 JSON 字符串为节点(容错返回 null)。
     *
     * @param json JSON 字符串
     * @return 节点或 null
     */
    private JsonNode readTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("管理端批改 JSON 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
