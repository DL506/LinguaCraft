package com.linguacraft.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码请求体(POST /api/auth/forgot-password)。
 *
 * <p>说明:无论邮箱是否存在均返回成功(防账号枚举),仅在存在时生成重置令牌并发送邮件。</p>
 */
@Data
public class ForgotPasswordRequest {

    /** 注册邮箱 */
    @Schema(description = "注册邮箱", example = "demo@linguacraft.dev")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}