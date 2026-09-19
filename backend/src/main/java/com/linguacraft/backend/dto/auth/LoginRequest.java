package com.linguacraft.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求体(POST /api/auth/login)。
 *
 * <p>说明:不在此做非空校验,用户名/密码错误统一返回 code 1001(与前端 mock 契约一致)。</p>
 */
@Data
public class LoginRequest {

    /** 用户名 */
    @Schema(description = "用户名", example = "demo")
    private String username;

    /** 密码(明文,服务端与 BCrypt 密文比对) */
    @Schema(description = "密码", example = "123456")
    private String password;
}