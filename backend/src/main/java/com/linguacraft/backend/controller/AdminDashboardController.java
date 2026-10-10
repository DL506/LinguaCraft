package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminDashboardDto;
import com.linguacraft.backend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端运营看板接口(阶段 A5)。
 *
 * <p>命名空间由 SecurityConfig 统一要求管理员角色;返回的都是聚合计数,不含用户隐私明细。</p>
 */
@Tag(name = "管理端-看板", description = "后台概览计数(仅管理员)")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * 看板概览。
     *
     * @return 统一响应:data 为各项计数
     */
    @Operation(summary = "运营看板概览",
            description = "题目/提交/用户/内容四类计数;「今日」沿用 created_at 口径(与学生端统计与首页一致),模考的两类欠账分开列(整卷 pending 与作文未判)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping
    public R<AdminDashboardDto> summary() {
        return R.ok(adminDashboardService.summary());
    }
}
