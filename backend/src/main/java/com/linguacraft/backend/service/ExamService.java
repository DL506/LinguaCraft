package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.ai.ExamAsyncGrader;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.exam.ExamListItemDto;
import com.linguacraft.backend.dto.exam.ExamPaperDto;
import com.linguacraft.backend.dto.exam.ExamProgressRequest;
import com.linguacraft.backend.dto.exam.ExamProgressResponse;
import com.linguacraft.backend.dto.exam.ExamReportDto;
import com.linguacraft.backend.dto.exam.ExamReportSectionDto;
import com.linguacraft.backend.dto.exam.ExamSectionDto;
import com.linguacraft.backend.dto.exam.ExamSubmitRequest;
import com.linguacraft.backend.dto.exam.HistoryScoreDto;
import com.linguacraft.backend.dto.practice.SubmitDetailDto;
import com.linguacraft.backend.dto.practice.SubmitResultResponse;
import com.linguacraft.backend.entity.ExamPaper;
import com.linguacraft.backend.entity.ExamProgress;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamProgressMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.util.PracticeRules;
import com.linguacraft.backend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模考服务:真题模考的列表 / 详情 / 交卷 / 成绩报告 / 作答进度(扩展 E2)。
 *
 * <p>批改状态机(与前端 mock 契约一致):</p>
 * <ul>
 *   <li>AI 模式:交卷即完成客观题比对 → partially_graded(客观题出分);随后异步 AI 作文评分,
 *       成功 → graded;失败保持 partially_graded(作文转人工);</li>
 *   <li>人工模式:交卷 → pending;到达阈值由定时任务把客观题部分流转为 partially_graded
 *       (作文保持待批,由后续人工批改通道处理);</li>
 *   <li>report 查询时对 partially_graded + ai 且作文未批的记录做一次防重入的异步兜底重试。</li>
 * </ul>
 */
@Service
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);

    /** 批改状态常量 */
    private static final String STATUS_GRADED = "graded";
    private static final String STATUS_PARTIALLY_GRADED = "partially_graded";
    private static final String STATUS_PENDING = "pending";

    /** 批改方式常量 */
    private static final String METHOD_AI = "ai";
    private static final String METHOD_AUTO = "auto";
    private static final String METHOD_MANUAL = "manual";

    /** 待批改提示文案(前端契约固定文案) */
    private static final String ESTIMATED_TIME = "预计 24 小时内出结果";

    private final ExamPaperMapper paperMapper;
    private final ExamSubmissionMapper submissionMapper;
    private final ExamProgressMapper progressMapper;
    private final UserSettingMapper userSettingMapper;
    private final ObjectiveGradingService objectiveGradingService;
    private final ExamAsyncGrader examAsyncGrader;
    private final ObjectMapper objectMapper;

    /** 人工模式下客观题延迟出分阈值(秒) */
    @Value("${lc.grading.manual-delay-seconds:60}")
    private long manualDelaySeconds;

    public ExamService(ExamPaperMapper paperMapper,
                       ExamSubmissionMapper submissionMapper,
                       ExamProgressMapper progressMapper,
                       UserSettingMapper userSettingMapper,
                       ObjectiveGradingService objectiveGradingService,
                       ExamAsyncGrader examAsyncGrader,
                       ObjectMapper objectMapper) {
        this.paperMapper = paperMapper;
        this.submissionMapper = submissionMapper;
        this.progressMapper = progressMapper;
        this.userSettingMapper = userSettingMapper;
        this.objectiveGradingService = objectiveGradingService;
        this.examAsyncGrader = examAsyncGrader;
        this.objectMapper = objectMapper;
    }

    /**
     * 模考列表(GET /api/exam/list):按年份与 ID 倒序;含当前用户最近一次得分。
     *
     * @param year 年份筛选(可空)
     * @param page 页码
     * @param size 每页条数(1~100)
     * @return 分页列表
     */
    public PageResult<ExamListItemDto> list(Integer year, long page, long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<ExamPaper>lambdaQuery()
                .orderByDesc(ExamPaper::getYear)
                .orderByDesc(ExamPaper::getId);
        if (year != null) {
            wrapper.eq(ExamPaper::getYear, year);
        }
        Page<ExamPaper> paperPage = paperMapper.selectPage(new Page<>(safePage, safeSize), wrapper);

        List<String> examIds = paperPage.getRecords().stream().map(ExamPaper::getId).toList();
        Map<String, ExamSubmission> latestByExam = new HashMap<>();
        if (!examIds.isEmpty()) {
            List<ExamSubmission> submissions = submissionMapper.selectList(
                    Wrappers.<ExamSubmission>lambdaQuery()
                            .eq(ExamSubmission::getUserId, userId)
                            .in(ExamSubmission::getExamId, examIds)
                            .orderByDesc(ExamSubmission::getCreatedAt));
            for (ExamSubmission submission : submissions) {
                latestByExam.putIfAbsent(submission.getExamId(), submission);
            }
        }

        List<ExamListItemDto> items = new ArrayList<>();
        for (ExamPaper paper : paperPage.getRecords()) {
            ExamSubmission latest = latestByExam.get(paper.getId());
            items.add(ExamListItemDto.builder()
                    .id(paper.getId())
                    .year(paper.getYear())
                    .title(paper.getTitle())
                    .totalScore(paper.getTotalScore())
                    .duration(paper.getDuration())
                    .lastScore(latestScore(latest))
                    .lastSubmittedAt(latest == null ? null : latest.getSubmittedAt())
                    .build());
        }
        return new PageResult<>(items, paperPage.getTotal(), safePage, safeSize);
    }

    /**
     * 整卷详情(GET /api/exam/{id};不含答案与解析)。
     *
     * @param id 试卷 ID
     * @return 整卷(阅读 section 的 content 为 3 篇数组)
     */
    public ExamPaperDto detail(String id) {
        ExamPaper paper = findPaper(id);
        JsonNode sectionsNode = readTree(paper.getContent()).path("sections");
        List<ExamSectionDto> sections = new ArrayList<>();
        for (JsonNode section : sectionsNode) {
            sections.add(ExamSectionDto.builder()
                    .part(section.path("part").asInt())
                    .type(section.path("type").asText())
                    .score(section.hasNonNull("score") ? section.get("score").decimalValue() : BigDecimal.ZERO)
                    .content(section.get("content"))
                    .build());
        }
        return ExamPaperDto.builder()
                .id(paper.getId())
                .year(paper.getYear())
                .title(paper.getTitle())
                .totalScore(paper.getTotalScore())
                .duration(paper.getDuration())
                .sections(sections)
                .build();
    }

    /**
     * 交卷(POST /api/exam/{id}/submit)。
     *
     * <p>objective 的 key 为答题卡全局编号(1~45);判分始终在交卷时完成并落库,
     * 状态按批改方式分流(详见类注释状态机)。响应 type 使用 "reading" 占位
     * (整卷无单一题型,与前端 mock / 前端类型约定一致)。</p>
     *
     * @param id   试卷 ID
     * @param body 交卷请求
     * @return 统一提交结果(SubmitResult)
     */
    public SubmitResultResponse submit(String id, ExamSubmitRequest body) {
        ExamPaper paper = findPaper(id);
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

        // 客观题判分(全局编号)与落库
        ExamGradingOutcome graded = gradeObjective(paper, toAnswerMap(body.getObjective()), caseSensitive(userId));
        ExamSubmission submission = new ExamSubmission();
        submission.setUserId(userId);
        submission.setExamId(paper.getId());
        submission.setTotalScore(paper.getTotalScore());
        submission.setObjective(body.getObjective() == null ? "{}" : writeJson(body.getObjective()));
        submission.setWriting(body.getWriting() == null ? null : writeJson(body.getWriting()));
        submission.setDetails(writeJson(graded.details()));
        submission.setSectionScores(writeJson(graded.sectionScores()));
        submission.setObjectiveScore(graded.objectiveScore());
        submission.setCorrectCount(graded.correctCount());
        submission.setDuration(body.getDuration() == null ? 0 : body.getDuration());
        submission.setSubmittedAt(now);

        if (METHOD_AI.equals(preference)) {
            // AI 模式:客观题已出分;异步批改作文
            submission.setStatus(STATUS_PARTIALLY_GRADED);
            submission.setGradingMethod(METHOD_AI);
            submission.setGradedAt(now);
            submissionMapper.insert(submission);
            clearProgress(userId, paper.getId());

            String writingContent = body.getWriting() == null ? "" : body.getWriting().path("content").asText("");
            if (!writingContent.isBlank()) {
                examAsyncGrader.gradeWritingAsync(submission.getId());
            }
            return buildSubmitResult(submission, graded.totalCount());
        }

        // 人工模式:整卷待批改;客观题由定时任务延迟流转出分
        submission.setStatus(STATUS_PENDING);
        submission.setGradingMethod(METHOD_AUTO);
        submission.setEstimatedTime(ESTIMATED_TIME);
        submissionMapper.insert(submission);
        clearProgress(userId, paper.getId());
        return buildSubmitResult(submission, graded.totalCount());
    }

    /**
     * 成绩报告(GET /api/exam/submission/{submissionId}/report)。
     *
     * @param submissionId 提交记录 ID(字符串)
     * @return 成绩报告(含各 section 得分/薄弱项/历年对比)
     */
    public ExamReportDto report(String submissionId) {
        Long id = parseId(submissionId);
        ExamSubmission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }
        ExamPaper paper = findPaper(submission.getExamId());

        // 兜底:AI 作文评分未完成时补触发一次(去重由评分器内部保证)
        if (STATUS_PARTIALLY_GRADED.equals(submission.getStatus()) && METHOD_AI.equals(submission.getGradingMethod())
                && submission.getWritingGradedAt() == null) {
            examAsyncGrader.gradeWritingAsync(submission.getId());
        }
        return buildReport(submission, paper);
    }

    /**
     * 读取作答进度(扩展契约 E2)。
     *
     * @param examId 试卷 ID
     * @return 进度(无进度时返回 null,接口 data 为 null)
     */
    public ExamProgressResponse getProgress(String examId) {
        findPaper(examId);
        Long userId = SecurityUtils.getCurrentUserId();
        ExamProgress progress = progressMapper.selectOne(
                Wrappers.<ExamProgress>lambdaQuery()
                        .eq(ExamProgress::getUserId, userId)
                        .eq(ExamProgress::getExamId, examId));
        if (progress == null) {
            return null;
        }
        return ExamProgressResponse.builder()
                .objective(readTree(progress.getObjective()))
                .writing(readTree(progress.getWriting()))
                .duration(progress.getDuration())
                .build();
    }

    /**
     * 保存作答进度(覆盖式,扩展契约 E2)。
     *
     * @param examId  试卷 ID
     * @param request 进度(objective/writing/duration)
     */
    public void saveProgress(String examId, ExamProgressRequest request) {
        findPaper(examId);
        Long userId = SecurityUtils.getCurrentUserId();
        String objectiveJson = request == null || request.getObjective() == null ? "{}" : writeJson(request.getObjective());
        String writingJson = request == null || request.getWriting() == null ? null : writeJson(request.getWriting());
        Integer duration = request == null || request.getDuration() == null ? 0 : request.getDuration();

        ExamProgress progress = progressMapper.selectOne(
                Wrappers.<ExamProgress>lambdaQuery()
                        .eq(ExamProgress::getUserId, userId)
                        .eq(ExamProgress::getExamId, examId));
        if (progress == null) {
            progress = new ExamProgress();
            progress.setUserId(userId);
            progress.setExamId(examId);
            progress.setObjective(objectiveJson);
            progress.setWriting(writingJson);
            progress.setDuration(duration);
            progressMapper.insert(progress);
        } else {
            // 覆盖式保存(含显式置空写作进度)
            progressMapper.update(null, Wrappers.<ExamProgress>lambdaUpdate()
                    .eq(ExamProgress::getId, progress.getId())
                    .set(ExamProgress::getObjective, objectiveJson)
                    .set(ExamProgress::getWriting, writingJson)
                    .set(ExamProgress::getDuration, duration));
        }
    }

    /**
     * 处理到期的模考待批改提交(由定时任务调用):人工模式下客观题部分流转为 partially_graded。
     *
     * @return 本次流转的提交数
     */
    public int processDuePendingSubmissions() {
        LocalDateTime deadline = LocalDateTime.now().minusSeconds(manualDelaySeconds);
        List<ExamSubmission> due = submissionMapper.selectList(
                Wrappers.<ExamSubmission>lambdaQuery()
                        .eq(ExamSubmission::getStatus, STATUS_PENDING)
                        .eq(ExamSubmission::getGradingMethod, METHOD_AUTO)
                        .le(ExamSubmission::getSubmittedAt, deadline));
        int processed = 0;
        for (ExamSubmission submission : due) {
            submissionMapper.update(null, Wrappers.<ExamSubmission>lambdaUpdate()
                    .eq(ExamSubmission::getId, submission.getId())
                    .set(ExamSubmission::getStatus, STATUS_PARTIALLY_GRADED)
                    .set(ExamSubmission::getGradedAt, LocalDateTime.now())
                    .set(ExamSubmission::getEstimatedTime, null));
            processed++;
        }
        if (processed > 0) {
            log.info("模考客观题延迟流转完成 {} 条(作文保持待批)", processed);
        }
        return processed;
    }

    // ==================== 内部方法 ====================

    /**
     * 整卷客观题判分:按 answer_key 的 sections 逐题比对(全局编号),按 section 汇总得分。
     *
     * @param paper         试卷
     * @param answers       客观题作答(全局编号 -> 作答)
     * @param caseSensitive 语法填空是否区分大小写
     * @return 判分结果
     */
    private ExamGradingOutcome gradeObjective(ExamPaper paper, Map<Integer, String> answers, boolean caseSensitive) {
        JsonNode sections = readTree(paper.getAnswerKey()).path("sections");
        List<SubmitDetailDto> details = new ArrayList<>();
        Map<Integer, BigDecimal> sectionScores = new LinkedHashMap<>();
        int correctCount = 0;
        int totalCount = 0;

        for (JsonNode section : sections) {
            String type = section.path("type").asText();
            if ("writing".equals(type)) {
                continue;
            }
            JsonNode items = section.get("items");
            if (items == null || !items.isArray() || items.isEmpty()) {
                continue;
            }
            int part = section.path("part").asInt();
            BigDecimal sectionTotal = section.hasNonNull("score") ? section.get("score").decimalValue() : BigDecimal.ZERO;
            int correctInSection = 0;

            for (JsonNode item : items) {
                int globalId = item.path("id").asInt();
                String answer = item.path("answer").asText("");
                String userAnswer = answers.getOrDefault(globalId, "");
                boolean isCorrect;
                if ("grammar-fill".equals(type)) {
                    List<String> alternatives = new ArrayList<>();
                    JsonNode altNode = item.get("acceptAlternatives");
                    if (altNode != null && altNode.isArray()) {
                        for (JsonNode alt : altNode) {
                            alternatives.add(alt.asText(""));
                        }
                    }
                    isCorrect = objectiveGradingService.judgeGrammarBlank(userAnswer, answer, alternatives, caseSensitive);
                } else {
                    isCorrect = userAnswer.equals(answer);
                }
                if (isCorrect) {
                    correctInSection++;
                    correctCount++;
                }
                details.add(SubmitDetailDto.builder()
                        .id(globalId)
                        .userAnswer(userAnswer)
                        .correctAnswer(answer)
                        .isCorrect(isCorrect)
                        .explanation(item.path("explanation").asText(""))
                        .build());
                totalCount++;
            }

            BigDecimal sectionScore = sectionTotal.multiply(BigDecimal.valueOf(correctInSection))
                    .divide(BigDecimal.valueOf(items.size()), 1, RoundingMode.HALF_UP);
            sectionScores.put(part, sectionScore);
        }

        BigDecimal objectiveScore = sectionScores.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
        return new ExamGradingOutcome(details, objectiveScore, correctCount, totalCount, sectionScores);
    }

    /**
     * 构建交卷响应(SubmitResult)。
     *
     * @param submission 提交记录
     * @param totalCount 客观题总题数
     * @return 统一提交结果
     */
    private SubmitResultResponse buildSubmitResult(ExamSubmission submission, int totalCount) {
        boolean pending = STATUS_PENDING.equals(submission.getStatus());
        boolean writingGraded = submission.getWritingGradedAt() != null;
        BigDecimal score = null;
        if (!pending) {
            score = submission.getObjectiveScore();
            if (writingGraded && submission.getWritingScore() != null) {
                score = score.add(submission.getWritingScore()).setScale(1, RoundingMode.HALF_UP);
            }
        }
        return SubmitResultResponse.builder()
                .submissionId(submission.getId())
                // 整卷无单一题型:与前端 mock 一致用 reading 占位(报告见 report 接口)
                .type("reading")
                .status(submission.getStatus())
                .gradingMethod(submission.getGradingMethod())
                .objectiveGraded(!pending)
                .writingGraded(writingGraded)
                .score(score)
                .totalScore(submission.getTotalScore())
                .correctCount(pending ? null : submission.getCorrectCount())
                .totalCount(totalCount)
                .estimatedTime(submission.getEstimatedTime())
                .build();
    }

    /**
     * 构建成绩报告。
     *
     * @param submission 提交记录
     * @param paper      试卷
     * @return 报告
     */
    private ExamReportDto buildReport(ExamSubmission submission, ExamPaper paper) {
        boolean writingGraded = submission.getWritingGradedAt() != null;
        BigDecimal writingScore = writingGraded ? submission.getWritingScore() : null;
        Map<Integer, BigDecimal> sectionScores = parseSectionScores(submission.getSectionScores());

        JsonNode sectionsNode = readTree(paper.getAnswerKey()).path("sections");
        List<ExamReportSectionDto> sections = new ArrayList<>();
        for (JsonNode section : sectionsNode) {
            int part = section.path("part").asInt();
            String type = section.path("type").asText();
            BigDecimal total = section.hasNonNull("score") ? section.get("score").decimalValue() : BigDecimal.ZERO;
            BigDecimal score;
            if ("writing".equals(type)) {
                score = writingGraded && writingScore != null ? writingScore : BigDecimal.ZERO;
            } else {
                score = sectionScores.getOrDefault(part, BigDecimal.ZERO);
            }
            sections.add(ExamReportSectionDto.builder().part(part).type(type).score(score).total(total).build());
        }

        // 薄弱项:得分率 <60% 的部分(与前端 mock 文案口径一致)
        List<String> weakPoints = new ArrayList<>();
        for (ExamReportSectionDto section : sections) {
            if (section.getTotal() == null || section.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal rate = section.getScore().divide(section.getTotal(), 2, RoundingMode.HALF_UP);
            if (rate.compareTo(new BigDecimal("0.6")) < 0) {
                weakPoints.add(PracticeRules.typeName(section.getType()) + "得分率 "
                        + Math.round(rate.doubleValue() * 100) + "%,建议加强专项练习");
            }
        }
        if (weakPoints.isEmpty()) {
            weakPoints.add("整体表现不错,保持练习节奏");
        }

        BigDecimal score = writingGraded && writingScore != null
                ? submission.getObjectiveScore().add(writingScore).setScale(1, RoundingMode.HALF_UP)
                : submission.getObjectiveScore();
        return ExamReportDto.builder()
                .score(score)
                .totalScore(paper.getTotalScore())
                .sections(sections)
                .weakPoints(weakPoints)
                .compareWithHistory(historyScores(submission.getUserId()))
                .objectiveGraded(!STATUS_PENDING.equals(submission.getStatus()))
                .writingGraded(writingGraded)
                .writingScore(writingScore)
                .build();
    }

    /**
     * 历年成绩对比:该用户已出分的模考(同年取最近一次),按年份升序。
     *
     * @param userId 用户 ID
     * @return 对比列表
     */
    private List<HistoryScoreDto> historyScores(Long userId) {
        List<ExamSubmission> submissions = submissionMapper.selectList(
                Wrappers.<ExamSubmission>lambdaQuery()
                        .eq(ExamSubmission::getUserId, userId)
                        .ne(ExamSubmission::getStatus, STATUS_PENDING)
                        .orderByDesc(ExamSubmission::getCreatedAt));
        Map<Integer, HistoryScoreDto> byYear = new HashMap<>();
        for (ExamSubmission submission : submissions) {
            ExamPaper paper = paperMapper.selectById(submission.getExamId());
            if (paper == null) {
                continue;
            }
            byYear.putIfAbsent(paper.getYear(), HistoryScoreDto.builder()
                    .year(paper.getYear())
                    .score(latestScore(submission))
                    .build());
        }
        return byYear.values().stream()
                .sorted(Comparator.comparing(HistoryScoreDto::getYear))
                .toList();
    }

    /**
     * 最近一次得分:未出分(pending)返回 null;作文已批返回总分,否则返回客观分。
     *
     * @param submission 提交记录(可空)
     * @return 得分或 null
     */
    private BigDecimal latestScore(ExamSubmission submission) {
        if (submission == null || STATUS_PENDING.equals(submission.getStatus())) {
            return null;
        }
        if (submission.getWritingGradedAt() != null && submission.getWritingScore() != null) {
            return submission.getObjectiveScore().add(submission.getWritingScore()).setScale(1, RoundingMode.HALF_UP);
        }
        return submission.getObjectiveScore();
    }

    /**
     * 查询试卷(不存在抛 404)。
     *
     * @param id 试卷 ID
     * @return 试卷实体
     */
    private ExamPaper findPaper(String id) {
        ExamPaper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "试卷不存在");
        }
        return paper;
    }

    /**
     * 解析提交记录 ID(非数字按不存在处理)。
     *
     * @param submissionId 字符串 ID
     * @return Long ID
     */
    private Long parseId(String submissionId) {
        try {
            return Long.parseLong(submissionId);
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }
    }

    /**
     * 解析 section_scores JSON({part: score})。
     *
     * @param json JSON 字符串
     * @return Map
     */
    private Map<Integer, BigDecimal> parseSectionScores(String json) {
        Map<Integer, BigDecimal> scores = new HashMap<>();
        JsonNode node = readTree(json);
        if (node != null && node.isObject()) {
            // Jackson 3:ObjectNode.fields() 已改名为 properties(),返回 Set<Entry<String,JsonNode>>
            node.properties().forEach(entry -> {
                try {
                    scores.put(Integer.parseInt(entry.getKey()), entry.getValue().decimalValue());
                } catch (Exception ignored) {
                    // 忽略异常项
                }
            });
        }
        return scores;
    }

    /**
     * 请求 objective JSON -> Map(全局编号 -> 作答);非数字键跳过。
     *
     * @param objective 作答 JSON
     * @return Map
     */
    private Map<Integer, String> toAnswerMap(JsonNode objective) {
        Map<Integer, String> map = new HashMap<>();
        if (objective == null || !objective.isObject()) {
            return map;
        }
        objective.properties().forEach(entry -> {
            try {
                map.put(Integer.parseInt(entry.getKey()), entry.getValue().asText(""));
            } catch (NumberFormatException ignored) {
                // 忽略非数字键
            }
        });
        return map;
    }

    /**
     * 读取用户"语法填空区分大小写"判分偏好(默认 false)。
     *
     * @param userId 用户 ID
     * @return 是否区分大小写
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
     * 清除作答进度(交卷成功后调用)。
     *
     * @param userId 用户 ID
     * @param examId 试卷 ID
     */
    private void clearProgress(Long userId, String examId) {
        progressMapper.delete(Wrappers.<ExamProgress>lambdaQuery()
                .eq(ExamProgress::getUserId, userId)
                .eq(ExamProgress::getExamId, examId));
    }

    /**
     * JSON 字符串解析(容错:失败返回空节点)。
     *
     * @param json JSON 字符串
     * @return JsonNode(失败时为空对象)
     */
    private JsonNode readTree(String json) {
        if (json == null || json.isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
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