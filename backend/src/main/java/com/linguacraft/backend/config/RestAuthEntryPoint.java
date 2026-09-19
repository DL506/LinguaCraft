package com.linguacraft.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证入口点:未认证访问受保护资源时统一返回 HTTP 401 + 统一响应体。
 *
 * <p>与前端契约一致:{code:401, message:"未登录或登录已过期", data:null};
 * 前端拦截后清除 token 并跳转登录页。</p>
 */
@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), R.fail(ResultCode.UNAUTHORIZED));
    }
}