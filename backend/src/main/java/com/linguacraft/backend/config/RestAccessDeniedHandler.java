package com.linguacraft.backend.config;

import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 权限不足处理器:已登录但角色不匹配时统一返回 HTTP 403 + 统一响应体。
 *
 * <p>注:Spring Security 7 把 {@code AccessDeniedHandler} 从
 * {@code org.springframework.security.web} 移到了 {@code org.springframework.security.web.access},
 * 与 {@code AuthenticationEntryPoint}(仍在原包)不再同包。</p>
 *
 * <p>与 401 的区别:401 表示"没有登录",前端会清 token 跳登录页;
 * 403 表示"登录了但不该访问"(例如学生令牌请求 /api/admin/**),
 * 前端只提示无权限、保留登录态,不清 token。</p>
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    /** 全局 JSON 序列化器(Jackson 3) */
    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), R.fail(ResultCode.FORBIDDEN));
    }
}
