package com.linguacraft.backend.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.linguacraft.backend.entity.ExamPaper;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.entity.Question;
import com.linguacraft.backend.mapper.ExamPaperMapper;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模考作文异步评分器(独立 Bean,供 @Async 代理生效)。
 *
 * <p>流程:交卷(AI 模式)→ 客观题已出分(partially_graded)→ 本器异步调用 Spring AI 评分,
 * 成功则更新为 graded(写 writingScore/writingResult/writingGradedAt);
 * 失败保持 partially_graded(作文转人工,与已确认口径一致)。
 * 内存去重集合防重入(report 查询兜底与交卷触发可能并发)。</p>
 */
@Component
public class ExamAsyncGrader {

    private static final Logger log = LoggerFactory.getLogger(ExamAsyncGrader.class);

    /** 批改状态:部分批改(客观题已出分,作文待批) */
    private static final String STATUS_PARTIALLY_GRADED = "partially_graded";

    /** 批改状态:已批改 */
    private static final String STATUS_GRADED = "graded";

    private final ExamSubmissionMapper submissionMapper;
    private final ExamPaperMapper paperMapper;
    private final AiGradingService aiGradingService;
    private final ObjectMapper objectMapper;

    /** 进行中的提交 ID(防重入) */
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

    public ExamAsyncGrader(ExamSubmissionMapper submissionMapper,
                           ExamPaperMapper paperMapper,
                           AiGradingService aiGradingService,
                           ObjectMapper objectMapper) {
        this.submissionMapper = submissionMapper;
        this.paperMapper = paperMapper;
        this.aiGradingService = aiGradingService;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步批改模考作文(幂等:已批改/进行中/无作文内容则跳过)。
     *
     * @param submissionId 模考提交 ID
     */
    @Async("aiExecutor")
    public void gradeWritingAsync(Long submissionId) {
        if (submissionId == null || !inFlight.add(submissionId)) {
            return;
        }
        try {
            ExamSubmission submission = submissionMapper.selectById(submissionId);
            if (submission == null || submission.getWriting() == null || submission.getWritingGradedAt() != null) {
                return;
            }
            String content = objectMapper.readTree(submission.getWriting()).path("content").asText("");
            if (content.isBlank()) {
                return;
            }
            ExamPaper paper = paperMapper.selectById(submission.getExamId());
            if (paper == null) {
                return;
            }

            // 构造写作题承载对象(题目内容取整卷 part5 的下发内容;满分 15)
            Question writingQuestion = new Question();
            writingQuestion.setType("writing");
            writingQuestion.setTotalScore(new BigDecimal("15"));
            writingQuestion.setContent(writingSectionContent(paper));

            GradedWriting graded = aiGradingService.gradeWriting(writingQuestion, content);

            ObjectNode writingResult = objectMapper.createObjectNode();
            writingResult.set("dimensions", objectMapper.valueToTree(graded.dimensions()));
            if (graded.sampleAnswer() != null) {
                writingResult.put("sampleAnswer", graded.sampleAnswer());
            }
            if (graded.overallComment() != null) {
                writingResult.put("overallComment", graded.overallComment());
            }

            submissionMapper.update(null, Wrappers.<ExamSubmission>lambdaUpdate()
                    .eq(ExamSubmission::getId, submissionId)
                    .eq(ExamSubmission::getStatus, STATUS_PARTIALLY_GRADED)
                    .isNull(ExamSubmission::getWritingGradedAt)
                    .set(ExamSubmission::getStatus, STATUS_GRADED)
                    .set(ExamSubmission::getWritingScore, graded.score())
                    .set(ExamSubmission::getWritingResult, writingResult.toString())
                    .set(ExamSubmission::getWritingGradedAt, LocalDateTime.now()));
            log.info("模考作文 AI 评分完成: submissionId={} score={}", submissionId, graded.score());
        } catch (Exception e) {
            log.warn("模考作文 AI 评分失败(保持 partially_graded 转人工): submissionId={} 原因={}",
                    submissionId, e.getMessage());
        } finally {
            inFlight.remove(submissionId);
        }
    }

    /**
     * 取整卷写作 section 的下发内容 JSON(作为评分提示词中的题目信息)。
     *
     * @param paper 试卷
     * @return 写作部分内容 JSON 字符串(缺省 "{}")
     */
    private String writingSectionContent(ExamPaper paper) {
        try {
            JsonNode sections = objectMapper.readTree(paper.getContent()).path("sections");
            for (JsonNode section : sections) {
                if ("writing".equals(section.path("type").asText())) {
                    return section.path("content").toString();
                }
            }
        } catch (Exception ignored) {
            // 内容解析失败时退化为空题目
        }
        return "{}";
    }
}