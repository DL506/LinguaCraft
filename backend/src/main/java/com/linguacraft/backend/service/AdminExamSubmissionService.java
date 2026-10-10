package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminExamSubmissionDetailDto;
import com.linguacraft.backend.dto.admin.AdminExamSubmissionListItemDto;
import com.linguacraft.backend.dto.admin.ExamWritingGradeRequest;
import com.linguacraft.backend.entity.ExamPaper;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端模考批改服务(阶段 A4-②):模考提交队列与作文人工判分。
 *
 * <p>断链位置:{@code ExamService.submit} 在交卷时就算好客观分并落库,写作部分交给
 * {@code ExamAsyncGrader} 异步评分;AI 失败时该批改器只写日志、<b>不改库</b>,提交因此停在
 * {@code partially_graded} 且 {@code writing_graded_at} 永远为空 —— 而 {@code buildReport}
 * 正是用这个时间戳判断"作文已出分",学生于是永远看到「作文待批改」。本服务就是那只人工的手。</p>
 *
 * <p>只判写作:客观题分数、各部分得分在交卷时已定,本服务一个字都不改;写作 JSON 的拼装交给
 * {@link WritingGradeSupport},与练习批改同形状。</p>
 */
@Service
public class AdminExamSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(AdminExamSubmissionService.class);

    /** 已批改 */
    private static final String STATUS_GRADED = "graded";

    /** 待批改 */
    private static final String STATUS_PENDING = "pending";

    /** 客观题已出分、作文待出分 */
    private static final String STATUS_PARTIALLY_GRADED = "partially_graded";

    /** 人工批改 */
    private static final String METHOD_MANUAL = "manual";

    /** 列表状态筛选白名单 */
    private static final Set<String> VALID_STATUS = Set.of(STATUS_PENDING, STATUS_GRADED, STATUS_PARTIALLY_GRADED);

    /** 写作部分满分兜底(与 ExamAsyncGrader 的硬编码一致) */
    private static final BigDecimal WRITING_FALLBACK_CAP = new BigDecimal("15");

    private final ExamSubmissionMapper submissionMapper;
    private final ExamPaperMapper paperMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final WritingGradeSupport writingGradeSupport;

    public AdminExamSubmissionService(ExamSubmissionMapper submissionMapper,
                                      ExamPaperMapper paperMapper,
                                      UserMapper userMapper,
                                      ObjectMapper objectMapper,
                                      WritingGradeSupport writingGradeSupport) {
        this.submissionMapper = submissionMapper;
        this.paperMapper = paperMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.writingGradeSupport = writingGradeSupport;
    }

    // ==================== 查询 ====================

    /**
     * 模考批改队列(GET /api/admin/exam-submissions),按交卷时间正序。
     *
     * @param status  状态筛选(可空)
     * @param keyword 试卷 ID 或昵称关键词(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    public PageResult<AdminExamSubmissionListItemDto> list(String status, String keyword, long page, long size) {
        if (status != null && !status.isBlank() && !VALID_STATUS.contains(status)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "status 仅接受 pending/graded/partially_graded");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<ExamSubmission>lambdaQuery();
        if (status != null && !status.isBlank()) {
            wrapper.eq(ExamSubmission::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ExamSubmission::getExamId, keyword.trim());
        }
        wrapper.orderByAsc(ExamSubmission::getSubmittedAt).orderByAsc(ExamSubmission::getId);

        Page<ExamSubmission> result = submissionMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<Long, String> nicknames = loadNicknames(result.getRecords());
        Map<String, ExamPaper> papers = loadPapers(result.getRecords());

        List<AdminExamSubmissionListItemDto> items = new ArrayList<>();
        for (ExamSubmission submission : result.getRecords()) {
            ExamPaper paper = papers.get(submission.getExamId());
            items.add(AdminExamSubmissionListItemDto.builder()
                    .id(submission.getId())
                    .userId(submission.getUserId())
                    .nickname(nicknames.get(submission.getUserId()))
                    .examId(submission.getExamId())
                    .paperTitle(paper == null ? submission.getExamId() : paper.getTitle())
                    .year(paper == null ? null : paper.getYear())
                    .status(submission.getStatus())
                    .gradingMethod(submission.getGradingMethod())
                    .objectiveScore(submission.getObjectiveScore())
                    .writingScore(submission.getWritingScore())
                    .totalScore(submission.getTotalScore())
                    .writingGraded(submission.getWritingGradedAt() != null)
                    .hasWritingContent(hasWritingContent(submission))
                    .waitingMinutes(waitingMinutes(submission))
                    .submittedAt(submission.getSubmittedAt())
                    .writingGradedAt(submission.getWritingGradedAt())
                    .build());
        }
        return new PageResult<>(items, result.getTotal(), safePage, safeSize);
    }

    /**
     * 判分材料(GET /api/admin/exam-submissions/{id})。
     *
     * @param id 提交 ID
     * @return 详情
     */
    public AdminExamSubmissionDetailDto detail(Long id) {
        ExamSubmission submission = findSubmission(id);
        ExamPaper paper = paperMapper.selectById(submission.getExamId());
        User user = userMapper.selectById(submission.getUserId());
        JsonNode writingSection = writingSection(paper);
        return AdminExamSubmissionDetailDto.builder()
                .id(submission.getId())
                .userId(submission.getUserId())
                .nickname(user == null ? null : user.getNickname())
                .examId(submission.getExamId())
                .paperTitle(paper == null ? submission.getExamId() : paper.getTitle())
                .year(paper == null ? null : paper.getYear())
                .status(submission.getStatus())
                .gradingMethod(submission.getGradingMethod())
                .objectiveScore(submission.getObjectiveScore())
                .writingScore(submission.getWritingScore())
                .totalScore(submission.getTotalScore())
                .writingFullScore(writingFullScore(paper))
                .writingContent(writingContent(submission))
                .writingSection(writingSection)
                .sectionScores(readTree(submission.getSectionScores()))
                .writingResult(readTree(submission.getWritingResult()))
                .writingGraded(submission.getWritingGradedAt() != null)
                .estimatedTime(submission.getEstimatedTime())
                .duration(submission.getDuration())
                .waitingMinutes(waitingMinutes(submission))
                .submittedAt(submission.getSubmittedAt())
                .writingGradedAt(submission.getWritingGradedAt())
                .build();
    }

    // ==================== 人工判分 ====================

    /**
     * 模考作文人工判分(POST /api/admin/exam-submissions/{id}/grade-writing)。
     *
     * <p>落库只写写作相关的四列与状态,客观分原样保留;CAS 条件为
     * {@code writing_graded_at IS NULL AND status <> 'graded'},因此与异步批改器互不覆盖 ——
     * 谁先写入 {@code writing_graded_at},后来者的影响行数就是 0。</p>
     *
     * @param id      提交 ID
     * @param request 判分请求
     * @return 判分后的详情
     */
    @Transactional
    public AdminExamSubmissionDetailDto gradeWriting(Long id, ExamWritingGradeRequest request) {
        ExamSubmission submission = findSubmission(id);
        if (submission.getWritingGradedAt() != null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该提交的作文已出分(" + submission.getWritingScore() + " 分),不能重复判分");
        }
        if (STATUS_GRADED.equals(submission.getStatus())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该提交已是已批改状态,无需再判");
        }
        if (submission.getObjectiveScore() == null) {
            // 客观分是交卷时就算好的,缺它说明数据异常;此时判作文会拼出错误的总分
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该提交还没有客观题得分,请先确认交卷数据");
        }
        String content = writingContent(submission);
        if (content == null || content.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该提交没有作文作答,无需人工批改");
        }

        BigDecimal cap = writingFullScore(paperMapper.selectById(submission.getExamId()));
        String writingResultJson = writingGradeSupport
                .buildResult(request.getDimensions(), request.getSampleAnswer(), request.getOverallComment(),
                        cap, "作文满分")
                .toString();
        BigDecimal score = request.getScore() != null ? request.getScore()
                : writingGradeSupport.dimensionSum(request.getDimensions());
        score = score.setScale(1, RoundingMode.HALF_UP);
        if (score.signum() < 0 || score.compareTo(cap) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "作文得分须在 0~" + cap.stripTrailingZeros().toPlainString() + " 之间,当前为 " + score);
        }

        var update = Wrappers.<ExamSubmission>lambdaUpdate()
                .eq(ExamSubmission::getId, id)
                .isNull(ExamSubmission::getWritingGradedAt)
                .ne(ExamSubmission::getStatus, STATUS_GRADED)
                .set(ExamSubmission::getStatus, STATUS_GRADED)
                .set(ExamSubmission::getGradingMethod, METHOD_MANUAL)
                .set(ExamSubmission::getWritingScore, score)
                .set(ExamSubmission::getWritingResult, writingResultJson)
                .set(ExamSubmission::getWritingGradedAt, LocalDateTime.now())
                // 待批改说明必须清空:报告与列表都会带它,残留会让已判完的卷子仍显示"待批改"
                .set(ExamSubmission::getEstimatedTime, null);
        int rows = submissionMapper.update(null, update);
        if (rows == 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该提交已被批改或作文已出分,请刷新后重试");
        }
        log.info("管理端模考作文人工批改: submissionId={} writingScore={}/{}", id, score, cap);
        return detail(id);
    }

    // ==================== 小工具 ====================

    /**
     * 取试卷写作部分的 content(prompt / requirements / scoringDimensions)。
     *
     * @param paper 试卷(可为 null)
     * @return 节点或 null
     */
    private JsonNode writingSection(ExamPaper paper) {
        JsonNode sections = paperSections(paper);
        for (JsonNode section : sections) {
            if ("writing".equals(section.path("type").asText())) {
                return section.get("content");
            }
        }
        return null;
    }

    /**
     * 取试卷 content 的 sections 数组(试卷缺失或 JSON 不可解析时返回空数组)。
     *
     * @param paper 试卷(可为 null)
     * @return sections 节点
     */
    private JsonNode paperSections(ExamPaper paper) {
        if (paper == null) {
            return objectMapper.createArrayNode();
        }
        JsonNode content = readTree(paper.getContent());
        JsonNode sections = content == null ? null : content.path("sections");
        return sections == null || !sections.isArray() ? objectMapper.createArrayNode() : sections;
    }

    /**
     * 写作部分分值:取试卷 content 里写作 section 的 score,缺失兜底 15。
     *
     * @param paper 试卷(可为 null)
     * @return 满分
     */
    private BigDecimal writingFullScore(ExamPaper paper) {
        for (JsonNode section : paperSections(paper)) {
            if ("writing".equals(section.path("type").asText())) {
                BigDecimal score = section.path("score").decimalValue();
                return score == null || score.signum() <= 0 ? WRITING_FALLBACK_CAP : score;
            }
        }
        return WRITING_FALLBACK_CAP;
    }

    /**
     * 学生作文正文。
     *
     * @param submission 提交
     * @return 正文(可空)
     */
    private String writingContent(ExamSubmission submission) {
        JsonNode node = readTree(submission.getWriting());
        return node == null ? null : node.path("content").asText("");
    }

    /**
     * 是否写了作文(列表里给管理员一个"这条没什么可判"的信号)。
     *
     * @param submission 提交
     * @return 有正文
     */
    private boolean hasWritingContent(ExamSubmission submission) {
        String content = writingContent(submission);
        return content != null && !content.isBlank();
    }

    /**
     * 自交卷起的等待分钟数。
     *
     * @param submission 提交
     * @return 分钟数
     */
    private Long waitingMinutes(ExamSubmission submission) {
        return submission.getSubmittedAt() == null
                ? null
                : Duration.between(submission.getSubmittedAt(), LocalDateTime.now()).toMinutes();
    }

    /**
     * 批量取昵称。
     *
     * @param submissions 当前页提交
     * @return 用户 ID -> 昵称
     */
    private Map<Long, String> loadNicknames(List<ExamSubmission> submissions) {
        Map<Long, String> result = new HashMap<>();
        List<Long> userIds = submissions.stream().map(ExamSubmission::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return result;
        }
        for (User user : userMapper.selectBatchIds(userIds)) {
            result.put(user.getId(), user.getNickname());
        }
        return result;
    }

    /**
     * 批量取试卷(避免逐行查)。
     *
     * @param submissions 当前页提交
     * @return 试卷 ID -> 试卷
     */
    private Map<String, ExamPaper> loadPapers(List<ExamSubmission> submissions) {
        Map<String, ExamPaper> result = new HashMap<>();
        List<String> examIds = submissions.stream().map(ExamSubmission::getExamId).distinct().toList();
        if (examIds.isEmpty()) {
            return result;
        }
        for (ExamPaper paper : paperMapper.selectBatchIds(examIds)) {
            result.put(paper.getId(), paper);
        }
        return result;
    }

    /**
     * 按 ID 取提交,不存在即 404。
     *
     * @param id 提交 ID
     * @return 提交实体
     */
    private ExamSubmission findSubmission(Long id) {
        ExamSubmission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "模考提交不存在");
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
            log.warn("模考批改 JSON 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
