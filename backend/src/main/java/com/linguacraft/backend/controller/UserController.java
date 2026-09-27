package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.practice.SubmitResultResponse;
import com.linguacraft.backend.dto.user.FavoriteCreateRequest;
import com.linguacraft.backend.dto.user.FavoriteDto;
import com.linguacraft.backend.dto.user.MistakeDto;
import com.linguacraft.backend.dto.user.SubmissionListItemDto;
import com.linguacraft.backend.dto.user.UserProfileResponse;
import com.linguacraft.backend.dto.user.UserSettingsResponse;
import com.linguacraft.backend.dto.user.UserSettingsUpdateRequest;
import com.linguacraft.backend.dto.user.UserStatsDto;
import com.linguacraft.backend.service.UserService;
import com.linguacraft.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口:资料与设置、我的提交、错题本、收藏、学习统计(前端契约 12.7);需登录。
 */
@Tag(name = "用户", description = "用户资料与设置、我的提交、错题本、收藏、学习统计")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 资料与设置 ====================

    /**
     * 获取用户资料。
     *
     * @return 统一响应:data 为 UserProfile
     */
    @Operation(summary = "获取用户资料", description = "返回等级/连续学习天数等资料;未登录返回 HTTP 401 + code 401")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/profile")
    public R<UserProfileResponse> getProfile() {
        return R.ok(userService.getProfile(SecurityUtils.getCurrentUserId()));
    }

    /**
     * 获取用户设置。
     *
     * @return 统一响应:data 为 UserSettings(不含 themeMode)
     */
    @Operation(summary = "获取用户设置", description = "返回判分偏好与学习目标(不含 themeMode,主题仅存前端)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/settings")
    public R<UserSettingsResponse> getSettings() {
        return R.ok(userService.getSettings(SecurityUtils.getCurrentUserId()));
    }

    /**
     * 更新用户设置(部分更新:未提供字段保持不变)。
     *
     * @param request 更新请求
     * @return 统一响应(空数据)
     */
    @Operation(summary = "更新用户设置", description = "部分更新语义:未提供的字段保持不变;accent 仅接受 us/uk,gradingMethod 仅接受 ai/manual")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 参数校验失败")
    @PutMapping("/settings")
    public R<Void> updateSettings(@RequestBody UserSettingsUpdateRequest request) {
        userService.updateSettings(SecurityUtils.getCurrentUserId(), request);
        return R.ok();
    }

    // ==================== 我的提交 ====================

    /**
     * 提交记录列表(四态筛选 + 分页)。
     *
     * @param status 筛选:all/graded/partially_graded/pending(缺省 all)
     * @param page   页码
     * @param size   每页条数
     * @return 统一响应:data 为 PageResult<SubmissionListItem>
     */
    @Operation(summary = "提交记录列表", description = "四态筛选(缺省 all)+ 分页;仅练习提交(模考独立于本列表);首页待批改提示的「查看」跳转带 status=pending")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 status 非法")
    @GetMapping("/submissions")
    public R<PageResult<SubmissionListItemDto>> submissions(
            @Parameter(description = "状态筛选:all/graded/partially_graded/pending", example = "pending")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "10") long size) {
        return R.ok(userService.submissions(status, page, size));
    }

    /**
     * 提交详情(details 仅 graded 下发;写作题附评分维度/范文/总评)。
     *
     * @param id 提交记录 ID
     * @return 统一响应:data 为 SubmitResult / WritingSubmitResult
     */
    @Operation(summary = "提交详情", description = "复用统一 SubmitResult 结构:details 仅 graded 下发;pending 时含 estimatedTime;写作题附 dimensions/sampleAnswer/overallComment;不存在 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 提交记录不存在")
    @GetMapping("/submissions/{id}")
    public R<SubmitResultResponse> submissionDetail(
            @Parameter(description = "提交记录 ID", required = true) @PathVariable("id") Long id) {
        return R.ok(userService.submissionDetail(id));
    }

    // ==================== 错题本 ====================

    /**
     * 错题列表(来源 + 题型筛选 + 分页)。
     *
     * @param source 来源筛选(可空)
     * @param type   题型筛选(可空)
     * @param page   页码
     * @param size   每页条数
     * @return 统一响应:data 为 PageResult<MistakeItem>
     */
    @Operation(summary = "错题列表", description = "按来源与题型筛选 + 分页(批改完成时自动收录的错误明细,追加式)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/mistakes")
    public R<PageResult<MistakeDto>> mistakes(
            @Parameter(description = "来源筛选:real/teacher/ai(可空)") @RequestParam(value = "source", required = false) String source,
            @Parameter(description = "题型筛选(可空)") @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "10") long size) {
        return R.ok(userService.mistakes(source, type, page, size));
    }

    /**
     * 删除错题。
     *
     * @param id 错题记录 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除错题", description = "删除指定错题记录;不存在或不属于当前用户返回 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 错题不存在")
    @DeleteMapping("/mistakes/{id}")
    public R<Void> deleteMistake(
            @Parameter(description = "错题记录 ID", required = true) @PathVariable("id") Long id) {
        userService.deleteMistake(id);
        return R.ok();
    }

    // ==================== 收藏 ====================

    /**
     * 收藏列表。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 统一响应:data 为 PageResult<FavoriteItem>
     */
    @Operation(summary = "收藏列表", description = "按收藏时间倒序分页")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/favorites")
    public R<PageResult<FavoriteDto>> favorites(
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "10") long size) {
        return R.ok(userService.favorites(page, size));
    }

    /**
     * 新增收藏(单词/句子)。
     *
     * @param request 收藏请求
     * @return 统一响应:data 为新增的 FavoriteItem
     */
    @Operation(summary = "新增收藏", description = "收藏单词或句子;type 仅接受 word/sentence,content 必填,均非法返回 code 400")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 参数非法")
    @PostMapping("/favorites")
    public R<FavoriteDto> addFavorite(@RequestBody FavoriteCreateRequest request) {
        return R.ok(userService.addFavorite(request));
    }

    /**
     * 删除收藏。
     *
     * @param id 收藏记录 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除收藏", description = "删除指定收藏;不存在或不属于当前用户返回 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 收藏不存在")
    @DeleteMapping("/favorites/{id}")
    public R<Void> deleteFavorite(
            @Parameter(description = "收藏记录 ID", required = true) @PathVariable("id") Long id) {
        userService.deleteFavorite(id);
        return R.ok();
    }

    // ==================== 学习统计 ====================

    /**
     * 学习统计(口径:见服务类注释)。
     *
     * @return 统一响应:data 为 UserStats
     */
    @Operation(summary = "学习统计", description = "totalStudyTime=Σ时长/60;totalWords=有进度单词数;accuracy=Σ正确/Σ题;typeAccuracy(grammar 映射语法填空,writing 按得分率);scoreTrend 近 7 天累计得分率;sourceAccuracy 无数据 -1;pendingGradingCount=练习待批改数")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/stats")
    public R<UserStatsDto> stats() {
        return R.ok(userService.stats());
    }
}