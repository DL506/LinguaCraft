package com.linguacraft.backend.job;

import com.linguacraft.backend.service.ExamService;
import com.linguacraft.backend.service.PracticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待批改流转定时任务:周期扫描人工模式下到期的 pending 提交,执行延迟出分流转。
 *
 * <p>覆盖两类:</p>
 * <ul>
 *   <li>练习客观题:到阈值后执行数据库比对出分(graded);</li>
 *   <li>整卷模考:到阈值后客观题部分流转为 partially_graded(作文保持待批)。</li>
 * </ul>
 *
 * <p>间隔可通过 lc.grading.scan-interval-ms 配置(默认 30 秒);
 * 无答案题目保持 pending 转人工,不在本任务处理范围。</p>
 */
@Component
public class PendingGradingJob {

    private static final Logger log = LoggerFactory.getLogger(PendingGradingJob.class);

    private final PracticeService practiceService;

    private final ExamService examService;

    public PendingGradingJob(PracticeService practiceService, ExamService examService) {
        this.practiceService = practiceService;
        this.examService = examService;
    }

    /**
     * 周期扫描并完成到期批改(练习 + 模考)。
     */
    @Scheduled(fixedDelayString = "${lc.grading.scan-interval-ms:30000}")
    public void scanPendingSubmissions() {
        int processed = practiceService.processDuePendingSubmissions() + examService.processDuePendingSubmissions();
        if (processed > 0) {
            log.info("延迟批改完成 {} 条待批改提交(练习+模考)", processed);
        }
    }
}