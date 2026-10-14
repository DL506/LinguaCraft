package com.linguacraft.backend.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置:无状态 JWT 认证 + 单角色授权。
 *
 * <p>授权面就是 <b>/api/**</b>:其中 /api/auth/**(登录注册等)与 /api/system/**(冒烟)放行,
 * /api/admin/** 额外要求管理员,其余 /api/** 必须携带有效 Bearer 令牌,未认证统一返回 HTTP 401 + code 401。
 * <b>非 /api 的请求一律放行</b> —— war 里带着前端 dist,首页与前端路由(如 /admin/exam-papers 的直接访问或刷新)
 * 属于静态资源与 SPA 页面,不放行会让 {@code java -jar} 一打开就是 401。</p>
 *
 * <p>后台权限:/api/admin/** 除认证外还要求 {@code hasRole("ADMIN")}(角色由 JWT 声明经
 * {@link JwtAuthFilter} 转为 ROLE_ADMIN);已登录但角色不足时由
 * {@link RestAccessDeniedHandler} 统一返回 HTTP 403 + code 403。</p>
 *
 * <p>注意:前端页面路径放行不等于数据放行 —— 页面拿不到任何业务数据,数据只在 /api 后面。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthEntryPoint restAuthEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          RestAuthEntryPoint restAuthEntryPoint,
                          RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.restAuthEntryPoint = restAuthEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    /**
     * 安全过滤链:禁用 CSRF(前后端分离+无状态令牌)、启用 CORS、JWT 过滤器前置。
     *
     * @param http HttpSecurity 构建器
     * @return 安全过滤链
     * @throws Exception 构建异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/system/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/**").permitAll()
                        // 后台接口:必须为管理员角色(顺序必须在 /api/** 兜底之前)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 业务接口一律要登录:全部控制器都在 /api 下(16 个 @RequestMapping 实测无例外)
                        .requestMatchers("/api/**").authenticated()
                        // 非 /api 的其余请求是 war 自带的前端静态资源与 SPA 页面路由,不含业务数据,必须放行:
                        // 放行前 "/";/assets/* 全部 401,java -jar 打开首页就是白屏
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .anyRequest().permitAll())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(restAuthEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 密码编码器:BCrypt(注册加密、登录比对)。
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 配置源(供 Spring Security 的 .cors() 使用,与 WebConfig 的 MVC 级配置一致)。
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}