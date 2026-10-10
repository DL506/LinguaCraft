package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminExamSubmissionDetailDto;
import com.linguacraft.backend.dto.admin.AdminExamSubmissionListItemDto;
import com.linguacraft.backend.dto.admin.ExamWritingGradeRequest;
import com.linguacraft.backend.service.AdminExamSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端模考批改接口(阶段 A4-②):队列、判分材料、作文人工判分。
 *
 * <p>与练习批改(A4)分属两套接口:落的是不同表、用的是不同 CAS 条件,
 * 但写作 JSON 由同一个写手产出,学生端两处渲染器读到的形状一致。</p>
 */
@Tag(name = "管理端-模考批改", description = "模考提交队列与作文人工判分(仅管理员)")
@RestController
@RequestMapping("/api/admin/exam-submissions")
public class AdminExamSubmissionController {

    private final AdminExamSubmissionService adminExamSubmissionService;

    public AdminExamSubmissionController(AdminExamSubmissionService adminExamSubmissionService) {
        this.adminExamSubmissionService = adminExamSubmissionService;
    }

    /**
     * 模考批改队列。
     *
     * @param status  状态筛选(可空)
     * @param keyword 试卷 ID 关键词(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 统一响应:data 为分页结果(按交卷时间正序)
     */
    @Operation(summary = "模考批改队列", description = "客观分在交卷时已算好,这里看的是写作部分是否还欠着(writingGraded=false 即待人工)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 状态非法")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping
    public R<PageResult<AdminExamSubmissionListItemDto>> list(
            @Parameter(description = "状态筛选:pending/graded/partially_graded") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "试卷 ID 关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(adminExamSubmissionService.list(status, keyword, page, size));
    }

    /**
     * 判分材料。
     *
     * @param id 提交 ID
     * @return 统一响应:data 为详情(作文正文、写作部分题目、客观各部分得分、既有批改结果)
     */
    @Operation(summary = "模考判分材料", description = "返回学生作文正文、试卷写作部分(prompt/requirements/scoringDimensions)与写作部分满分")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 提交不存在")
    @GetMapping("/{id}")
    public R<AdminExamSubmissionDetailDto> detail(
            @Parameter(description = "提交 ID", required = true, example = "1") @PathVariable("id") Long id) {
        return R.ok(adminExamSubmissionService.detail(id));
    }

    /**
     * 作文人工判分。
     *
     * @param id      提交 ID
     * @param request 判分请求
     * @return 统一响应:data 为判分后的详情
     */
    @Operation(summary = "模考作文人工判分",
            description = "只写写作相关列(不改客观分):writing_score/writing_result/writing_graded_at,status 置为 graded,estimated_time 置空。CAS 条件 writing_graded_at IS NULL,与 AI 异步批改器互不覆盖")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 已出分/无正文/超分值/已被处理,code=404 提交不存在")
    @PostMapping("/{id}/grade-writing")
    public R<AdminExamSubmissionDetailDto> gradeWriting(
            @Parameter(description = "提交 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody ExamWritingGradeRequest request) {
        return R.ok(adminExamSubmissionService.gradeWriting(id, request));
    }
}
