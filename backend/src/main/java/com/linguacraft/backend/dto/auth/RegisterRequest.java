package com.linguacraft.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求体(POST /api/auth/register)。
 *
 * <p>校验规则(任务书 10.2):用户名 3~20 位字母数字、密码 ≥6 位、昵称必填、邮箱格式合法。</p>
 */
@Data
public class RegisterRequest {

    /** 用户名:3~20 位字母数字 */
    @Schema(description = "用户名(3~20 位字母数字)", example = "student01")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,20}$", message = "用户名需为 3~20 位字母数字")
    private String username;

    /** 密码:至少 6 位 */
    @Schema(description = "密码(至少 6 位)", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少 6 位")
    private String password;

    /** 昵称 */
    @Schema(description = "昵称", example = "小明")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 邮箱(用于找回密码) */
    @Schema(description = "邮箱(用于找回密码)", example = "student01@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}