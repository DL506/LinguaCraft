package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminExamPaperDto;
import com.linguacraft.backend.dto.admin.ExamPaperUpsertRequest;
import com.linguacraft.backend.entity.ExamPaper;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端真题卷服务(阶段 A6-③):列表、详情、整卷编辑、上下架与引用保护删除。
 *
 * <p>与题库(A2)最不一样的一点:<b>试卷把题目整份复制进自己的 JSON</b>,不引用 {@code question} 表,
 * 所以改题不会弄坏已下发的卷子,删题也不会影响历史卷 —— 反过来,卷内的题号一旦错位是<b>无法事后发现</b>的错,
 * 因此所有写路径都先过 {@link ExamPaperValidator}。</p>
 *
 * <p>下架只作用于"新的作答":学生端列表与取卷都过滤 {@code status=1},
 * 但成绩报告刻意不看状态,已交卷的考生仍能查分,否则历史成绩会凭空消失。</p>
 */
@Service
public class AdminExamPaperService {

    private static final Logger log = LoggerFactory.getLogger(AdminExamPaperService.class);

    /** 上架 */
    private static final int STATUS_ON = 1;

    /** 下架 */
    private static final int STATUS_OFF = 0;

    /** 新 ID 的时间格式 */
    private static final DateTimeFormatter ID_STAMP = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final ExamPaperMapper paperMapper;
    private final ExamSubmissionMapper submissionMapper;
    private final ObjectMapper objectMapper;
    private final ExamPaperValidator examPaperValidator;

    public AdminExamPaperService(ExamPaperMapper paperMapper,
                                ExamSubmissionMapper submissionMapper,
                                ObjectMapper objectMapper,
                                ExamPaperValidator examPaperValidator) {
        this.paperMapper = paperMapper;
        this.submissionMapper = submissionMapper;
        this.objectMapper = objectMapper;
        this.examPaperValidator = examPaperValidator;
    }

    // ==================== 查询 ====================

    /**
     * 试卷分页列表(GET /api/admin/exam-papers),含下架卷。
     *
     * @param year   年份筛选(可空)
     * @param status 状态筛选(可空)
     * @param keyword 标题关键词(可空)
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    public PageResult<AdminExamPaperDto> list(Integer year, Integer status, String keyword, long page, long size) {
        if (status != null && status != STATUS_ON && status != STATUS_OFF) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "status 仅接受 1 或 0");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<ExamPaper>lambdaQuery();
        if (year != null) {
            wrapper.eq(ExamPaper::getYear, year);
        }
        if (status != null) {
            wrapper.eq(ExamPaper::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ExamPaper::getTitle, keyword.trim());
        }
        wrapper.orderByDesc(ExamPaper::getYear).orderByDesc(ExamPaper::getId);

        Page<ExamPaper> result = paperMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<String, Integer> submissions = countSubmissions(result.getRecords().stream().map(ExamPaper::getId).toList());

        List<AdminExamPaperDto> items = new ArrayList<>();
        for (ExamPaper paper : result.getRecords()) {
            items.add(toDto(paper, submissions.getOrDefault(paper.getId(), 0), false));
        }
        return new PageResult<>(items, result.getTotal(), safePage, safeSize);
    }

    /**
     * 试卷详情(GET /api/admin/exam-papers/{id}),含两份 JSON。
     *
     * @param id 试卷 ID
     * @return 详情
     */
    public AdminExamPaperDto detail(String id) {
        ExamPaper paper = findPaper(id);
        return toDto(paper, countSubmissions(List.of(id)).getOrDefault(id, 0), true);
    }

    // ==================== 写入 ====================

    /**
     * 新建或整卷更新(POST / PUT /api/admin/exam-papers/{id})。
     *
     * @param id      路径 ID(新建为空)
     * @param request 请求
     * @return 保存后的详情
     */
    @Transactional
    public AdminExamPaperDto save(String id, ExamPaperUpsertRequest request) {
        JsonNode content = requireObject(request.getContent(), "content");
        JsonNode answerKey = requireObject(request.getAnswerKey(), "answer_key");
        examPaperValidator.validate(content, answerKey, request.getTotalScore());

        ExamPaper paper = id == null || id.isBlank() ? new ExamPaper() : findPaper(id);
        if (paper.getId() == null) {
            paper.setId(request.getYear() + "-" + LocalDateTime.now().format(ID_STAMP));
            if (request.getId() != null && !request.getId().isBlank()) {
                paper.setId(request.getId().trim());
            }
            if (paperMapper.selectById(paper.getId()) != null) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "试卷 ID 已存在:" + paper.getId());
            }
        }
        paper.setYear(request.getYear());
        paper.setTitle(request.getTitle().trim());
        paper.setDuration(request.getDuration() == null ? 120 : Math.max(1, request.getDuration()));
        paper.setTotalScore(request.getTotalScore());
        paper.setContent(content.toString());
        paper.setAnswerKey(answerKey.toString());
        if (request.getStatus() != null) {
            paper.setStatus(requireStatus(request.getStatus()));
        } else if (paper.getStatus() == null) {
            paper.setStatus(STATUS_ON);
        }

        if (paperMapper.selectById(paper.getId()) == null) {
            paperMapper.insert(paper);
            log.info("管理端新建真题卷: id={} year={}", paper.getId(), paper.getYear());
        } else {
            paperMapper.updateById(paper);
            log.info("管理端更新真题卷: id={}", paper.getId());
        }
        return toDto(paper, countSubmissions(List.of(paper.getId())).getOrDefault(paper.getId(), 0), true);
    }

    /**
     * 上下架(PATCH /api/admin/exam-papers/{id}/status)。
     *
     * <p>只作用于新的作答:学生端列表与取卷过滤 {@code status=1};成绩报告不看状态。</p>
     *
     * @param id     试卷 ID
     * @param status 目标状态
     */
    @Transactional
    public void changeStatus(String id, Integer status) {
        // 先验参数再查库:非法状态不该白跑一次查询,更不该把"状态非法"报成 404
        int target = requireStatus(status);
        ExamPaper paper = findPaper(id);
        paper.setStatus(target);
        paperMapper.updateById(paper);
        log.info("管理端真题卷上下架: id={} status={}", id, status);
    }

    /**
     * 删除试卷(带引用保护)。
     *
     * @param id 试卷 ID
     */
    @Transactional
    public void delete(String id) {
        findPaper(id);
        int submissions = countSubmissions(List.of(id)).getOrDefault(id, 0);
        if (submissions > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该试卷已被 " + submissions + " 次交卷引用,不能删除;如需停止投放请使用下架");
        }
        paperMapper.deleteById(id);
        log.info("管理端删除真题卷: id={}", id);
    }

    // ==================== 小工具 ====================

    /**
     * 实体 -> DTO。
     *
     * @param paper       试卷
     * @param submissions 交卷次数
     * @param withJson    是否下发两份大 JSON(详情才带)
     * @return DTO
     */
    private AdminExamPaperDto toDto(ExamPaper paper, int submissions, boolean withJson) {
        JsonNode content = readTree(paper.getContent());
        JsonNode answerKey = readTree(paper.getAnswerKey());
        AdminExamPaperDto.AdminExamPaperDtoBuilder builder = AdminExamPaperDto.builder()
                .id(paper.getId())
                .year(paper.getYear())
                .title(paper.getTitle())
                .totalScore(paper.getTotalScore())
                .sectionScoreSum(sumSectionScores(content))
                .duration(paper.getDuration())
                .status(paper.getStatus() == null ? STATUS_ON : paper.getStatus())
                .sectionCount(sizeOfSections(content))
                .objectiveItemCount(countAnswerItems(answerKey))
                .submissionCount(submissions)
                .createdAt(paper.getCreatedAt())
                .updatedAt(paper.getUpdatedAt());
        if (withJson) {
            builder.content(content).answerKey(answerKey);
        }
        return builder.build();
    }

    /**
     * 各部分分值之和(供界面与 total_score 并列展示,便于发现漂移)。
     *
     * @param content 卷面节点
     * @return 合计
     */
    private BigDecimal sumSectionScores(JsonNode content) {
        BigDecimal sum = BigDecimal.ZERO;
        if (content == null) {
            return sum;
        }
        for (JsonNode section : content.path("sections")) {
            BigDecimal score = section.path("score").decimalValue();
            if (score != null) {
                sum = sum.add(score);
            }
        }
        return sum;
    }

    /**
     * 部分数。
     *
     * @param content 卷面节点
     * @return 数量
     */
    private int sizeOfSections(JsonNode content) {
        JsonNode sections = content == null ? null : content.get("sections");
        return sections == null || !sections.isArray() ? 0 : sections.size();
    }

    /**
     * 答案侧 items 总数(即客观题卡数)。
     *
     * @param answerKey 答案节点
     * @return 数量
     */
    private int countAnswerItems(JsonNode answerKey) {
        JsonNode sections = answerKey == null ? null : answerKey.get("sections");
        if (sections == null || !sections.isArray()) {
            return 0;
        }
        int count = 0;
        for (JsonNode section : sections) {
            JsonNode items = section.get("items");
            if (items != null && items.isArray()) {
                count += items.size();
            }
        }
        return count;
    }

    /**
     * 批量统计交卷次数(避免逐行查)。
     *
     * @param examIds 试卷 ID 集合
     * @return ID -> 次数
     */
    private Map<String, Integer> countSubmissions(List<String> examIds) {
        Map<String, Integer> result = new HashMap<>();
        if (examIds.isEmpty()) {
            return result;
        }
        for (String examId : examIds) {
            Long count = submissionMapper.selectCount(
                    Wrappers.<ExamSubmission>lambdaQuery().eq(ExamSubmission::getExamId, examId));
            result.put(examId, count == null ? 0 : count.intValue());
        }
        return result;
    }

    /**
     * 状态值校验。
     *
     * @param status 状态
     * @return 状态
     */
    private int requireStatus(int status) {
        if (status != STATUS_ON && status != STATUS_OFF) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "status 仅接受 1(上架)或 0(下架)");
        }
        return status;
    }

    /**
     * 断言节点是 JSON 对象。
     *
     * @param node  节点
     * @param field 字段名
     * @return 节点
     */
    private JsonNode requireObject(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), field + " 必须是 JSON 对象");
        }
        return node;
    }

    /**
     * 按 ID 取卷,不存在即 404。
     *
     * @param id 试卷 ID
     * @return 试卷
     */
    private ExamPaper findPaper(String id) {
        ExamPaper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "试卷不存在");
        }
        return paper;
    }

    /**
     * 解析 JSON(容错返回 null)。
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
            log.warn("真题卷 JSON 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
