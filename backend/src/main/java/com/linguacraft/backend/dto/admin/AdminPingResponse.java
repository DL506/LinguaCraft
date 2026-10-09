package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端权限探针响应(GET /api/admin/ping)。
 *
 * <p>用途:验证"已登录 + 管理员角色"这条授权链路是否通畅,并为后续管理端接口提供统一的
 * 调试入口(学生令牌请求本接口应得 HTTP 403 + code 403;未登录应得 401)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理端权限探针响应")
public class AdminPingResponse {

    /** 当前管理员用户 ID(包装类型 Long,对外序列化为字符串) */
    @Schema(description = "当前管理员用户 ID(字符串)", example = "2")
    private Long userId;

    /** 当前角色(恒为 admin,否则请求不会到达业务层) */
    @Schema(description = "当前角色", example = "admin")
    private String role;

    /** 服务器当前时间(证明请求真正进入了管理端控制器;与既有 DTO 一致的 ISO 口径) */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "服务器当前时间(ISO 本地日期时间)", example = "2026-10-10T07:20:31")
    private LocalDateTime serverTime;
}
