package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.exam.ExamListItemDto;
import com.linguacraft.backend.dto.exam.ExamPaperDto;
import com.linguacraft.backend.dto.exam.ExamProgressRequest;
import com.linguacraft.backend.dto.exam.ExamProgressResponse;
import com.linguacraft.backend.dto.exam.ExamReportDto;
import com.linguacraft.backend.dto.exam.ExamSubmitRequest;
import com.linguacraft.backend.dto.practice.SubmitResultResponse;
import com.linguacraft.backend.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 真题模考接口:列表 / 整卷 / 交卷 / 成绩报告 / 作答进度(前端契约 12.4 + 扩展 E2)。
 *
 * <p>路径歧义规避:GET /list 静态段先于 /{id};/submission/{submissionId}/report 段数不同不冲突。</p>
 */
@Tag(name = "真题模考", description = "模考列表、整卷答题、交卷、成绩报告与作答进度(断点续答)")
@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    /**
     * 模考列表。
     *
     * @param year 年份筛选(可空)
     * @param page 页码
     * @param size 每页条数
     * @return 统一响应:data 为 PageResult<ExamListItem>(含最近一次得分)
     */
    @Operation(summary = "模考列表", description = "按年份与 ID 倒序;year 可空筛选;含当前用户最近一次得分(待批改时不返回分数)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/list")
    public R<PageResult<ExamListItemDto>> list(
            @Parameter(description = "年份筛选(可空)", example = "2026") @RequestParam(value = "year", required = false) Integer year,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "10") long size) {
        return R.ok(examService.list(year, page, size));
    }

    /**
     * 整卷详情(不含答案与解析)。
     *
     * @param id 试卷 ID
     * @return 统一响应:data 为 ExamPaper(阅读 section 为 3 篇数组)
     */
    @Operation(summary = "整卷详情", description = "返回不含答案与解析的整卷(阅读部分为 3 篇数组);不存在返回 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 试卷不存在")
    @GetMapping("/{id}")
    public R<ExamPaperDto> detail(
            @Parameter(description = "试卷 ID", required = true, example = "exam-2026") @PathVariable("id") String id) {
        return R.ok(examService.detail(id));
    }

    /**
     * 交卷。
     *
     * @param id   试卷 ID
     * @param body 交卷请求(objective 全局编号 1~45 + writing + duration + gradingMethod?)
     * @return 统一响应:data 为 SubmitResult(三态:partially_graded/pending/graded)
     */
    @Operation(summary = "交卷", description = "客观题按答题卡全局编号判分;AI 模式交卷即出客观分(partially_graded)并异步批改作文,成功后 graded;人工模式先 pending,约 1 分钟后客观部分自动流转(作文待批)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 试卷不存在,code=400 参数非法")
    @PostMapping("/{id}/submit")
    public R<SubmitResultResponse> submit(
            @Parameter(description = "试卷 ID", required = true) @PathVariable("id") String id,
            @RequestBody ExamSubmitRequest body) {
        return R.ok(examService.submit(id, body));
    }

    /**
     * 成绩报告。
     *
     * @param submissionId 提交记录 ID(来自交卷响应 submissionId)
     * @return 统一响应:data 为 ExamReport(各部分得分/薄弱项/历年对比)
     */
    @Operation(summary = "成绩报告", description = "按当前批改进度返回:含各部分得分、薄弱项(得分率<60%)、历年对比、客观/写作批改状态与写作得分;AI 作文未批时查询会兜底触发一次异步评分")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 提交记录或试卷不存在")
    @GetMapping("/submission/{submissionId}/report")
    public R<ExamReportDto> report(
            @Parameter(description = "提交记录 ID(字符串)", required = true) @PathVariable("submissionId") String submissionId) {
        return R.ok(examService.report(submissionId));
    }

    /**
     * 读取模考作答进度(扩展契约 E2)。
     *
     * @param id 试卷 ID
     * @return 统一响应:data 为进度或无进度时 null
     */
    @Operation(summary = "读取模考作答进度", description = "断点续答(扩展契约 E2):返回未交卷的作答进度;无进度时 data 为 null")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功(data 可为 null)")
    @GetMapping("/{id}/progress")
    public R<ExamProgressResponse> getProgress(
            @Parameter(description = "试卷 ID", required = true) @PathVariable("id") String id) {
        return R.ok(examService.getProgress(id));
    }

    /**
     * 保存模考作答进度(扩展契约 E2,覆盖式)。
     *
     * @param id      试卷 ID
     * @param request 进度内容
     * @return 统一响应(空数据)
     */
    @Operation(summary = "保存模考作答进度", description = "覆盖式保存未交卷作答(扩展契约 E2);交卷成功后进度自动清除")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @PutMapping("/{id}/progress")
    public R<Void> saveProgress(
            @Parameter(description = "试卷 ID", required = true) @PathVariable("id") String id,
            @RequestBody ExamProgressRequest request) {
        examService.saveProgress(id, request);
        return R.ok();
    }
}