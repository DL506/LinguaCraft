package com.linguacraft.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置:跨域策略 + SPA 历史模式回退。
 *
 * <p>联调期前端 dev server(localhost:5173)经 vite 代理访问后端;
 * 本 CORS 配置作为不经代理直连(例如 Apifox 网页版、其他端口的前端实例)时的兜底。</p>
 *
 * <p>历史模式回退:{@code frontend/src/router/routes.ts} 用的是 {@code createWebHistory()},
 * war 自带 dist 时直接访问或刷新 {@code /admin/exam-papers} 这类前端路径,服务器侧并没有对应文件,
 * 需要转发到 {@code index.html} 交给前端路由。这里<b>只列前端顶层前缀</b>,
 * 不用 {@code /&#42;*&#47;} 之类的通配 —— 那会把 {@code /api/xxx} 的 404 也吞成"200 + 一段 HTML",
 * 前端 {@code res.json()} 会炸在解析上,极难定位。<b>新增前端顶层路由时必须同步加一行。</b></p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 前端 SPA 的顶层路由前缀(与 routes.ts 一致);带子路径的用 {*rest} 兜住 */
    private static final String[] SPA_PATHS = {
            "/login", "/register", "/forgot-password", "/reset-password", "/dashboard",
            "/practice", "/practice/{*rest}",
            "/exam", "/exam/{*rest}",
            "/learn", "/learn/{*rest}",
            "/user", "/user/{*rest}",
            "/admin", "/admin/{*rest}",
    };

    /**
     * 注册跨域规则:允许本机任意端口的开发前端直连 /api。
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 前端路由回退:命中 SPA 顶层路径时转发到 war 内的 index.html。
     *
     * <p>转发发生在 FORWARD 派发上,{@link SecurityConfig} 已对该派发放行;
     * 而 {@code /index.html} 与 {@code /assets/**} 属于非 /api 路径,同样在放行范围内。</p>
     *
     * @param registry 视图控制器注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String path : SPA_PATHS) {
            // 视图名带 forward: 前缀由默认视图解析器转成容器内转发(Spring 7 已移除 registry.addForward)
            registry.addViewController(path).setViewName("forward:/index.html");
        }
    }
}