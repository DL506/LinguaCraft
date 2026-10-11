package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminExamPaperDto;
import com.linguacraft.backend.dto.admin.ExamPaperUpsertRequest;
import com.linguacraft.backend.service.AdminExamPaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端真题卷接口(阶段 A6-③):整卷维护、上下架、引用保护删除。
 *
 * <p>详情会下发 {@code answer_key},因此与学生端的 {@code /api/exam/**} 严格分 namespace。</p>
 */
@Tag(name = "管理端-真题卷", description = "真题卷整卷维护、上下架与删除(仅管理员)")
@RestController
@RequestMapping("/api/admin/exam-papers")
public class AdminExamPaperController {

    private final AdminExamPaperService adminExamPaperService;

    public AdminExamPaperController(AdminExamPaperService adminExamPaperService) {
        this.adminExamPaperService = adminExamPaperService;
    }

    /**
     * 试卷列表。
     *
     * @param year    年份筛选(可空)
     * @param status  状态筛选(可空)
     * @param keyword 标题关键词(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 统一响应:data 为分页结果
     */
    @Operation(summary = "真题卷列表", description = "含下架卷;每行附带分值合计、客观题卡数与交卷次数(>0 不可删)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 status 非法")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping
    public R<PageResult<AdminExamPaperDto>> list(
            @Parameter(description = "年份筛选") @RequestParam(value = "year", required = false) Integer year,
            @Parameter(description = "状态:1 上架,0 下架") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "标题关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(adminExamPaperService.list(year, status, keyword, page, size));
    }

    /**
     * 试卷详情。
     *
     * @param id 试卷 ID
     * @return 统一响应:data 为详情(含两份 JSON)
     */
    @Operation(summary = "真题卷详情", description = "下发 content 与 answer_key 两份 JSON,供整卷编辑")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 试卷不存在")
    @GetMapping("/{id}")
    public R<AdminExamPaperDto> detail(
            @Parameter(description = "试卷 ID", required = true, example = "exam-2026") @PathVariable("id") String id) {
        return R.ok(adminExamPaperService.detail(id));
    }

    /**
     * 新建试卷。
     *
     * @param request 请求
     * @return 统一响应:data 为新卷详情
     */
    @Operation(summary = "新建真题卷", description = "落库前过 ExamPaperValidator:部分号唯一、分值合计等于总分、每部分答案编号与卷面编号一致、写作部分恰好一个且 15 分")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败(含部分号),code=1004 题型非法")
    @PostMapping
    public R<AdminExamPaperDto> create(@Valid @RequestBody ExamPaperUpsertRequest request) {
        return R.ok(adminExamPaperService.save(null, request));
    }

    /**
     * 整卷更新。
     *
     * @param id      试卷 ID
     * @param request 请求
     * @return 统一响应:data 为更新后详情
     */
    @Operation(summary = "更新真题卷", description = "整卷覆盖两份 JSON;题号一旦错位会导致客观题静默判错,故校验不过直接拒绝保存")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败,code=404 试卷不存在")
    @PutMapping("/{id}")
    public R<AdminExamPaperDto> update(
            @Parameter(description = "试卷 ID", required = true) @PathVariable("id") String id,
            @Valid @RequestBody ExamPaperUpsertRequest request) {
        return R.ok(adminExamPaperService.save(id, request));
    }

    /**
     * 上下架。
     *
     * @param id     试卷 ID
     * @param status 目标状态
     * @return 统一响应(空数据)
     */
    @Operation(summary = "上架/下架", description = "下架只停止新的作答:学生端列表与取卷过滤 status=1;成绩报告不看状态,已交卷的仍可查分")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 status 非法,code=404 试卷不存在")
    @PatchMapping("/{id}/status")
    public R<Void> changeStatus(
            @Parameter(description = "试卷 ID", required = true) @PathVariable("id") String id,
            @Parameter(description = "目标状态:1 上架,0 下架", required = true, example = "0")
            @RequestParam("status") Integer status) {
        adminExamPaperService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 删除试卷。
     *
     * @param id 试卷 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除真题卷", description = "被交卷引用时拒绝删除并提示改用下架(历史报告依赖这张卷)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 存在交卷引用,code=404 试卷不存在")
    @DeleteMapping("/{id}")
    public R<Void> delete(
            @Parameter(description = "试卷 ID", required = true) @PathVariable("id") String id) {
        adminExamPaperService.delete(id);
        return R.ok();
    }
}
