package com.linguacraft.backend.config;

import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器:解析请求头 {@code Authorization: Bearer <token>},
 * 校验通过后将当前用户 ID(principal)与角色权限写入安全上下文。
 *
 * <p>角色映射:令牌 role 声明(admin/student)转为 {@code ROLE_ADMIN}/{@code ROLE_STUDENT}
 * 权限,供安全配置对 /api/admin/** 做 hasRole 判定;旧令牌无 role 声明时按 student 兜底。</p>
 *
 * <p>令牌缺失或无效时不设置认证信息(保持匿名),由安全配置的入口点对受保护资源
 * 统一返回 HTTP 401 + code 401(前端拦截后清 token 跳登录页)。</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** 认证请求头前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** Spring Security 权限前缀(hasRole("ADMIN") 实际匹配 ROLE_ADMIN) */
    private static final String AUTHORITY_PREFIX = "ROLE_";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            JwtUtil.JwtPrincipal principal = jwtUtil.parsePrincipal(header.substring(BEARER_PREFIX.length()));
            if (principal != null) {
                // 角色兜底:旧令牌未携带 role 时视为学生(不能因令牌版本差异而提权)
                String role = principal.role() == null || principal.role().isBlank()
                        ? User.ROLE_STUDENT : principal.role();
                // principal 为用户 ID(供 SecurityUtils.getCurrentUserId 读取),authorities 为角色权限
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal.userId(), null,
                                List.of(new SimpleGrantedAuthority(AUTHORITY_PREFIX + role.toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}