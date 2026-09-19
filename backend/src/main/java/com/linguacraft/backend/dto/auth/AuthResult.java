package com.linguacraft.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册响应(POST /api/auth/login、/api/auth/register 共用;注册即登录)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {

    /** JWT 访问令牌(前端持久化到 localStorage,后续请求以 Bearer 携带) */
    @Schema(description = "JWT 访问令牌")
    private String token;

    /** 有效期(秒),与前端契约 expiresIn=7200 一致 */
    @Schema(description = "有效期(秒)", example = "7200")
    private Integer expiresIn;

    /** 登录用户信息 */
    @Schema(description = "登录用户信息")
    private AuthUser user;
}