package com.linguacraft.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求体(POST /api/auth/reset-password)。
 *
 * <p>令牌为一次性使用,有效期 30 分钟,使用后立即作废。</p>
 */
@Data
public class ResetPasswordRequest {

    /** 重置令牌(来自重置邮件链接的 query 参数) */
    @Schema(description = "重置令牌(来自邮件链接的 token 参数)")
    @NotBlank(message = "重置令牌不能为空")
    private String token;

    /** 新密码:至少 6 位 */
    @Schema(description = "新密码(至少 6 位)", example = "654321")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度至少 6 位")
    private String newPassword;
}