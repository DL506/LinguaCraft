package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.user.UserProfileResponse;
import com.linguacraft.backend.dto.user.UserSettingsResponse;
import com.linguacraft.backend.dto.user.UserSettingsUpdateRequest;
import com.linguacraft.backend.service.UserService;
import com.linguacraft.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口:资料与设置(前端契约 12.7);需登录(Bearer 令牌)。
 */
@Tag(name = "用户", description = "用户资料与设置")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
}