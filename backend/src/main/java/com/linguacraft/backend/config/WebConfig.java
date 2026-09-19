package com.linguacraft.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置:跨域策略。
 *
 * <p>联调期前端 dev server(localhost:5173)经 vite 代理访问后端;
 * 本 CORS 配置作为不经代理直连(例如 Apifox 网页版、其他端口的前端实例)时的兜底。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注册跨域规则:允许本机任意端口的开发前端直连 /api。
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}