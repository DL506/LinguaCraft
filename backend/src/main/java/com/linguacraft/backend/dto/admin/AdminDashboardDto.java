package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营看板概览(阶段 A5)。
 *
 * <p>字段全部是计数或时长,口径与学生端一致(见各字段说明),看板上出现的每个数字都应能在学生端复现。
 * 故意不含任何用户隐私明细(邮箱/正文),明细在各自的队列页看。</p>
 *
 * <p>类型说明:全部用基本类型数字。项目把 {@code Long} 全局序列化成字符串(前端 id 契约),
 * 而这里的数字要直接渲染,不能变成字符串 —— 与 {@code PageResult} 用 {@code long} 的理由相同。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "运营看板概览")
public class AdminDashboardDto {

    /** 上架题目数(question.status=1,即学生端可见) */
    @Schema(description = "上架题目数")
    private int questionOnline;

    /** 题目总数(含下架) */
    @Schema(description = "题目总数(含下架)")
    private int questionTotal;

    /** 今日新增题目数(created_at 口径,含 AI 出题) */
    @Schema(description = "今日新增题目数")
    private int questionCreatedToday;

    /** 其中 AI 生成的题目数 */
    @Schema(description = "AI 生成题目数(source_type=ai)")
    private int questionAiTotal;

    /** 练习待批改数(与学生端 stats.pendingGradingCount 同口径,仅练习) */
    @Schema(description = "练习待批改数")
    private int practicePending;

    /** 模考待批改数(整卷 pending) */
    @Schema(description = "模考待批改数")
    private int examPending;

    /** 模考作文欠着的数量(客观分已出但写作未出分,即 A4-② 要人工判的那批) */
    @Schema(description = "模考作文待判数")
    private int examWritingUngraded;

    /** 练习待批改里等得最久的分钟数(无待批改时为 0) */
    @Schema(description = "练习待批改最长等待(分钟)")
    private long practiceOldestPendingMinutes;

    /** 用户总数 */
    @Schema(description = "用户总数")
    private int userTotal;

    /** 停用账号数(enabled=0) */
    @Schema(description = "停用账号数")
    private int userDisabled;

    /** 管理员账号数(role=admin) */
    @Schema(description = "管理员账号数")
    private int userAdmin;

    /** 今日练习提交数(created_at 口径,与首页今日任务一致) */
    @Schema(description = "今日练习提交数")
    private int submissionPracticeToday;

    /** 今日模考交卷数 */
    @Schema(description = "今日模考交卷数")
    private int submissionExamToday;

    /** 全站已学长词数(user_word_progress) */
    @Schema(description = "已学长词数(全站累计)")
    private int wordLearnedTotal;

    /** 今日新学词数(first_learned_at 为今日) */
    @Schema(description = "今日新学词数")
    private int wordLearnedToday;

    /** 今日复习次数(user_word_review 记录条数) */
    @Schema(description = "今日复习次数")
    private int wordReviewedToday;

    /** 词库单词总数 */
    @Schema(description = "词库单词总数")
    private int vocabWordTotal;

    /** 词书数量 */
    @Schema(description = "词书数量")
    private int vocabBookTotal;

    /** 语法章节数 */
    @Schema(description = "语法章节数")
    private int grammarSectionTotal;

    /** 真题卷数量 */
    @Schema(description = "真题卷数量")
    private int examPaperTotal;

    /** 错题本条目数 */
    @Schema(description = "错题本条目数")
    private int mistakeTotal;
}
