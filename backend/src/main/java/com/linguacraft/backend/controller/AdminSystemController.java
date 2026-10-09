package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminPingResponse;
import com.linguacraft.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 管理端系统接口:权限探针。
 *
 * <p>整个 {@code /api/admin/**} 命名空间由 SecurityConfig 统一要求 {@code hasRole("ADMIN")},
 * 因此本接口只需关注业务;学生令牌访问会得到 HTTP 403 + code 403,未登录得到 401。</p>
 */
@Tag(name = "管理端-系统", description = "后台权限探针与后续管理端接口的统一入口命名空间")
@RestController
@RequestMapping("/api/admin")
public class AdminSystemController {

    /**
     * 权限探针。
     *
     * @return 统一响应:data 为当前管理员 ID、角色与服务器时间
     */
    @Operation(summary = "权限探针", description = "验证 /api/admin/** 的管理员授权链路;学生令牌返回 code 403,未登录返回 code 401")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @ApiResponse(responseCode = "403", description = "已登录但角色不足:code=403 无权限访问")
    @GetMapping("/ping")
    public R<AdminPingResponse> ping() {
        return R.ok(AdminPingResponse.builder()
                .userId(SecurityUtils.getCurrentUserId())
                .role(SecurityUtils.getCurrentRole())
                .serverTime(LocalDateTime.now())
                .build());
    }
}
