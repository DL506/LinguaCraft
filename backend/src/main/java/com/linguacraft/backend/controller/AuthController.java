package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.auth.AuthResult;
import com.linguacraft.backend.dto.auth.ForgotPasswordRequest;
import com.linguacraft.backend.dto.auth.LoginRequest;
import com.linguacraft.backend.dto.auth.RegisterRequest;
import com.linguacraft.backend.dto.auth.ResetPasswordRequest;
import com.linguacraft.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口:登录、注册(注册即登录)、忘记密码、重置密码(前端契约 12.1)。
 */
@Tag(name = "认证", description = "注册、登录、找回密码、重置密码")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录。
     *
     * @param request 登录请求(用户名/密码)
     * @return 统一响应:data 为 {token, expiresIn, user}
     */
    @Operation(summary = "登录", description = "校验用户名密码并签发 JWT;失败返回 code 1001(用户名或密码错误)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=1001 用户名或密码错误")
    @PostMapping("/login")
    public R<AuthResult> login(@RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    /**
     * 注册(注册即登录)。
     *
     * @param request 注册请求(用户名/密码/昵称/邮箱)
     * @return 统一响应:data 同登录
     */
    @Operation(summary = "注册(注册即登录)", description = "创建账号并直接签发 JWT;用户名重复返回 code 1002,参数非法返回 code 400")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=1002 用户名已存在,code=400 参数校验失败")
    @PostMapping("/register")
    public R<AuthResult> register(@RequestBody @Valid RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    /**
     * 忘记密码:发送重置邮件。
     *
     * @param request 忘记密码请求(邮箱)
     * @return 统一响应(空数据)
     */
    @Operation(summary = "忘记密码", description = "邮箱存在时生成一次性重置令牌并发送邮件(30 分钟有效);未配置 SMTP 时后端日志输出链接;邮箱不存在同样返回成功(防账号枚举)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @PostMapping("/forgot-password")
    public R<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return R.ok();
    }

    /**
     * 重置密码:使用邮件中的一次性令牌设置新密码。
     *
     * @param request 重置密码请求(令牌/新密码)
     * @return 统一响应(空数据)
     */
    @Operation(summary = "重置密码", description = "校验一次性令牌(未使用且未过期)后更新密码并作废令牌;令牌无效返回 code 400")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 重置链接无效或已过期")
    @PostMapping("/reset-password")
    public R<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return R.ok();
    }
}