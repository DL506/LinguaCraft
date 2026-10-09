package com.linguacraft.backend.util;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具:从 Spring Security 上下文中读取当前登录用户。
 *
 * <p>当前用户 ID 由 {@code JwtAuthFilter} 解析令牌后写入认证信息(principal = Long userId)。</p>
 */
public final class SecurityUtils {

    /** Spring Security 角色权限前缀 */
    private static final String ROLE_PREFIX = "ROLE_";

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID
     * @throws BizException 未认证时抛出(统一响应 code 401)
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
            return userId;
        }
        throw new BizException(ResultCode.UNAUTHORIZED);
    }

    /**
     * 获取当前登录用户的角色(来自 JWT 声明转换的权限)。
     *
     * <p>权限名带 Spring Security 的 {@code ROLE_} 前缀,本方法去掉前缀后返回
     * 小写形式(student/admin),以便与管理端接口响应体里的 {@code role} 字段保持一致口径。</p>
     *
     * @return 角色字符串;未认证或无角色权限时返回 student(安全兜底,不提升权限)
     */
    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return com.linguacraft.backend.entity.User.ROLE_STUDENT;
        }
        for (org.springframework.security.core.GrantedAuthority authority : authentication.getAuthorities()) {
            String name = authority.getAuthority();
            if (name != null && name.startsWith(ROLE_PREFIX)) {
                return name.substring(ROLE_PREFIX.length()).toLowerCase(java.util.Locale.ROOT);
            }
        }
        return com.linguacraft.backend.entity.User.ROLE_STUDENT;
    }
}