package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminUserDetailDto;
import com.linguacraft.backend.dto.admin.AdminUserListItemDto;
import com.linguacraft.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户接口(阶段 A5):用户查询与启用停用、角色调整。
 *
 * <p>V08 的两列(role/enabled)至此才有后台入口。响应永不含密码。</p>
 */
@Tag(name = "管理端-用户", description = "用户列表与详情、启用停用、角色调整(仅管理员)")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 用户列表。
     *
     * @param keyword 登录名/昵称/邮箱关键词(可空)
     * @param role    角色筛选(可空)
     * @param enabled 启用状态筛选(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 统一响应:data 为分页结果
     */
    @Operation(summary = "用户列表", description = "按注册时间倒序;可按角色、启用状态与关键词筛选")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 role 非法")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping
    public R<PageResult<AdminUserListItemDto>> list(
            @Parameter(description = "登录名/昵称/邮箱关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "角色筛选:student/admin") @RequestParam(value = "role", required = false) String role,
            @Parameter(description = "启用状态筛选") @RequestParam(value = "enabled", required = false) Boolean enabled,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(adminUserService.list(keyword, role, enabled, page, size));
    }

    /**
     * 用户详情。
     *
     * @param id 用户 ID
     * @return 统一响应:data 为详情
     */
    @Operation(summary = "用户详情", description = "资料 + 学习量小结(练习/模考提交数、待批改数、错题数、已学长词数、批改方式偏好)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 用户不存在")
    @GetMapping("/{id}")
    public R<AdminUserDetailDto> detail(
            @Parameter(description = "用户 ID", required = true, example = "1") @PathVariable("id") Long id) {
        return R.ok(adminUserService.detail(id));
    }

    /**
     * 启用/停用。
     *
     * @param id      用户 ID
     * @param enabled true 启用,false 停用
     * @return 统一响应(空数据)
     */
    @Operation(summary = "启用/停用账号",
            description = "拒绝停用自己与停用最后一个可用管理员。注意:已签发令牌不受影响(过滤器只读声明),停用最长 2 小时后才对旧会话生效,新登录立即被拒(code 1001)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 护栏拒绝或参数缺失,code=404 用户不存在")
    @PatchMapping("/{id}/status")
    public R<Void> setEnabled(
            @Parameter(description = "用户 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "true 启用,false 停用", required = true, example = "false")
            @RequestParam("enabled") Boolean enabled) {
        adminUserService.setEnabled(id, enabled);
        return R.ok();
    }

    /**
     * 角色调整。
     *
     * @param id   用户 ID
     * @param role 目标角色
     * @return 统一响应(空数据)
     */
    @Operation(summary = "调整角色", description = "只接受 student/admin;拒绝把自己降权(会失去后台入口)与降权最后一个管理员")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 护栏拒绝或角色非法,code=404 用户不存在")
    @PatchMapping("/{id}/role")
    public R<Void> setRole(
            @Parameter(description = "用户 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "目标角色:student/admin", required = true, example = "admin")
            @RequestParam("role") String role) {
        adminUserService.setRole(id, role);
        return R.ok();
    }
}
