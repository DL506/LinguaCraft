package com.linguacraft.backend.config;

import com.linguacraft.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器:解析请求头 {@code Authorization: Bearer <token>},
 * 校验通过后将当前用户 ID(principal)写入安全上下文。
 *
 * <p>令牌缺失或无效时不设置认证信息(保持匿名),由安全配置的入口点对受保护资源
 * 统一返回 HTTP 401 + code 401(前端拦截后清 token 跳登录页)。</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** 认证请求头前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            Long userId = jwtUtil.parseUserId(header.substring(BEARER_PREFIX.length()));
            if (userId != null) {
                // principal 为用户 ID,供 SecurityUtils.getCurrentUserId() 读取
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}