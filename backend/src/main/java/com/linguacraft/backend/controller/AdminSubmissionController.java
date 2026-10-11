package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminSubmissionDetailDto;
import com.linguacraft.backend.dto.admin.AdminSubmissionListItemDto;
import com.linguacraft.backend.dto.admin.ManualGradeRequest;
import com.linguacraft.backend.dto.admin.WritingPreviewDto;
import com.linguacraft.backend.service.AdminSubmissionService;
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
 * 管理端批改接口(阶段 A4):待批改队列与人工判分。
 *
 * <p>整个命名空间由 SecurityConfig 要求 {@code hasRole("ADMIN")}。本组接口会下发参考答案与
 * 学生作答,因此只服务于后台,学生端读接口不受影响。</p>
 */
@Tag(name = "管理端-批改", description = "待批改队列、判分材料与人工判分落库(仅管理员)")
@RestController
@RequestMapping("/api/admin/submissions")
public class AdminSubmissionController {

    private final AdminSubmissionService adminSubmissionService;

    public AdminSubmissionController(AdminSubmissionService adminSubmissionService) {
        this.adminSubmissionService = adminSubmissionService;
    }

    /**
     * 批改队列分页。
     *
     * @param status  状态筛选(可空)
     * @param type    题型筛选(可空)
     * @param method  批改方式筛选(可空)
     * @param keyword 标题/题目 ID 关键词(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 统一响应:data 为分页结果(按等待时长正序)
     */
    @Operation(summary = "批改队列", description = "默认按提交时间正序(等得最久的在前);method=manual 可只看必须人工处理的断链件")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=1004 题型非法,code=400 状态或方式非法")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping
    public R<PageResult<AdminSubmissionListItemDto>> list(
            @Parameter(description = "状态筛选:pending/graded/partially_graded") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "题型筛选", example = "writing") @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "批改方式筛选:ai/auto/manual") @RequestParam(value = "method", required = false) String method,
            @Parameter(description = "标题或题目 ID 关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(adminSubmissionService.list(status, type, method, keyword, page, size));
    }

    /**
     * 判分材料详情。
     *
     * @param id 提交 ID
     * @return 统一响应:data 为详情(含作答、题目内容、参考答案与既有判分)
     */
    @Operation(summary = "判分材料", description = "一次返回学生作答、题目下发内容、题库参考答案与既有 AI/自动判分结果")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 提交不存在")
    @GetMapping("/{id}")
    public R<AdminSubmissionDetailDto> detail(
            @Parameter(description = "提交 ID", required = true, example = "17") @PathVariable("id") Long id) {
        return R.ok(adminSubmissionService.detail(id));
    }

    /**
     * 人工判分落库。
     *
     * @param id      提交 ID
     * @param request 判分请求
     * @return 统一响应:data 为落库后的详情
     */
    @Operation(summary = "人工判分", description = "客观题提交逐题判定(items,题号须与学生作答一致),作文提交四维得分;落库为 graded + manual 并清空待批改说明,更新带 pending 条件防并发覆盖")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败/非待批改/已被处理,code=404 提交不存在")
    @PostMapping("/{id}/grade")
    public R<AdminSubmissionDetailDto> grade(
            @Parameter(description = "提交 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody ManualGradeRequest request) {
        return R.ok(adminSubmissionService.grade(id, request));
    }

    /**
     * AI 预评写作件(草稿,不落库)。
     *
     * @param id 提交 ID
     * @return 统一响应:data 为 AI 建议的分项得分与评语
     */
    @Operation(summary = "AI 预评(练习写作件)",
            description = "把学生作文交给 AI 起草四维分/范文/总评,前端回填到判分抽屉供人工审阅。"
                    + "**只读不写**:不改 status、不写 writing_result,学生仍显示待批改;管理员改完必须调 /grade 才生效。"
                    + "满分按题面口径(提交快照的卷面总分),不采信 AI 自报满分")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 非写作题/非待批改/无正文,code=404 提交不存在,code=500 AI 调用失败(抽屉内容不受影响,可重试)")
    @PostMapping("/{id}/ai-preview")
    public R<WritingPreviewDto> aiPreview(
            @Parameter(description = "提交 ID", required = true) @PathVariable("id") Long id) {
        return R.ok(adminSubmissionService.aiPreview(id));
    }
}
