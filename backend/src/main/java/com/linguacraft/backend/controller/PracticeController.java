package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.practice.PracticeItemResponse;
import com.linguacraft.backend.dto.practice.PracticeListItemResponse;
import com.linguacraft.backend.dto.practice.PracticeSubmitRequest;
import com.linguacraft.backend.dto.practice.ProgressRequest;
import com.linguacraft.backend.dto.practice.ProgressResponse;
import com.linguacraft.backend.dto.practice.SubmitResultResponse;
import com.linguacraft.backend.service.PracticeService;
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
 * 刷题接口:五种题型统一的列表、详情、提交与作答进度(前端契约 12.3 + 扩展 E1)。
 *
 * <p>路径歧义规避(任务书 12.8):静态段 /list 与动态段 /detail/{type}/{id} 分开声明;
 * 后续 AI 出题 /generate、智能练习 /smart 为独立静态段,不冲突。</p>
 */
@Tag(name = "刷题", description = "五种题型统一的列表、详情、提交与作答进度(断点续答)")
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    /**
     * 题型来源列表。
     *
     * @param type   题型
     * @param source 来源筛选(可空,仅 real/teacher)
     * @param page   页码
     * @param size   每页条数
     * @return 统一响应:data 为 PageResult<PracticeListItem>
     */
    @Operation(summary = "题型来源列表", description = "按题型与来源分页;source 仅接受 real/teacher(否则 code 1005);题型非法 code 1004;含最近一次提交状态")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/list")
    public R<PageResult<PracticeListItemResponse>> list(
            @Parameter(description = "题型", required = true, example = "reading") @RequestParam("type") String type,
            @Parameter(description = "来源:real/teacher(可空)") @RequestParam(value = "source", required = false) String source,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "10") long size) {
        return R.ok(practiceService.list(type, source, page, size));
    }

    /**
     * 题目详情(不含答案与解析)。
     *
     * @param type 题型
     * @param id   题目 ID
     * @return 统一响应:data 为 PracticeItem
     */
    @Operation(summary = "题目详情", description = "返回不含答案与解析的题目内容(存储层双列设计保证不下发);不存在返回 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 题目不存在")
    @GetMapping("/detail/{type}/{id}")
    public R<PracticeItemResponse> detail(
            @Parameter(description = "题型", required = true) @PathVariable("type") String type,
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id) {
        return R.ok(practiceService.detail(type, id));
    }

    /**
     * 提交作答。
     *
     * @param type 题型
     * @param id   题目 ID
     * @param body 提交请求
     * @return 统一响应:data 为 SubmitResult(三态:graded/partially_graded/pending)
     */
    @Operation(summary = "提交作答", description = "AI 模式且有答案:立即比对出分(graded);人工模式:先 pending,约 1 分钟后自动出分;写作/无答案题转人工 pending;写作题批改后返回评分维度与范文")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 题目不存在,code=400 参数非法")
    @PostMapping("/detail/{type}/{id}/submit")
    public R<SubmitResultResponse> submit(
            @Parameter(description = "题型", required = true) @PathVariable("type") String type,
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id,
            @RequestBody PracticeSubmitRequest body) {
        return R.ok(practiceService.submit(type, id, body));
    }

    /**
     * 读取作答进度(扩展契约 E1,断点续答)。
     *
     * @param type 题型
     * @param id   题目 ID
     * @return 统一响应:data 为进度或无进度时 null
     */
    @Operation(summary = "读取作答进度", description = "断点续答(扩展契约 E1):返回未提交作答;无进度时 data 为 null;题目不存在 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功(data 可为 null)")
    @GetMapping("/detail/{type}/{id}/progress")
    public R<ProgressResponse> getProgress(
            @Parameter(description = "题型", required = true) @PathVariable("type") String type,
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id) {
        return R.ok(practiceService.getProgress(type, id));
    }

    /**
     * 保存作答进度(扩展契约 E1,覆盖式)。
     *
     * @param type    题型
     * @param id      题目 ID
     * @param request 进度内容
     * @return 统一响应(空数据)
     */
    @Operation(summary = "保存作答进度", description = "覆盖式保存未提交作答(扩展契约 E1);提交成功后进度自动清除")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @PutMapping("/detail/{type}/{id}/progress")
    public R<Void> saveProgress(
            @Parameter(description = "题型", required = true) @PathVariable("type") String type,
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id,
            @RequestBody ProgressRequest request) {
        practiceService.saveProgress(type, id, request);
        return R.ok();
    }
}