package com.linguacraft.backend.job;

import com.linguacraft.backend.service.PracticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待批改流转定时任务:周期扫描人工模式下到期的 pending 客观题提交,
 * 执行数据库比对并出分(对应设置页"人工批改:稍后出结果"语义)。
 *
 * <p>间隔可通过 lc.grading.scan-interval-ms 配置(默认 30 秒);
 * 无答案题目保持 pending 转人工,不在本任务处理范围。</p>
 */
@Component
public class PendingGradingJob {

    private static final Logger log = LoggerFactory.getLogger(PendingGradingJob.class);

    private final PracticeService practiceService;

    public PendingGradingJob(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    /**
     * 周期扫描并完成到期批改。
     */
    @Scheduled(fixedDelayString = "${lc.grading.scan-interval-ms:30000}")
    public void scanPendingSubmissions() {
        int processed = practiceService.processDuePendingSubmissions();
        if (processed > 0) {
            log.info("延迟批改完成 {} 条待批改提交", processed);
        }
    }
}