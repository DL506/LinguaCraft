package com.linguacraft.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册响应中的用户信息(对应前端 api/auth.ts 的 AuthUser)。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    /** 用户 ID(包装类型 Long,对外序列化为字符串) */
    @Schema(description = "用户 ID(字符串)", example = "1")
    private Long id;

    /** 用户名 */
    @Schema(description = "用户名", example = "demo")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称", example = "小明")
    private String nickname;

    /** 头像地址(可空) */
    @Schema(description = "头像地址(可空)")
    private String avatar;
}