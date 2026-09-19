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
}