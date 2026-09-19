package com.linguacraft.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置:全局元信息与 JWT Bearer 认证方案。
 *
 * <p>项目规范:所有接口必须添加完整注解(@Tag/@Operation/@Parameter/@Schema/@ApiResponse),
 * 保证 swagger-ui 与 Apifox 等工具完整可读。</p>
 */
@Configuration
public class OpenApiConfig {

    /** 安全方案名称(供 SecurityRequirement 引用) */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * 构建 OpenAPI 文档定义。
     *
     * @return 全局 OpenAPI 配置(标题/版本/描述 + JWT 认证方案)
     */
    @Bean
    public OpenAPI linguacraftOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LinguaCraft API")
                        .version("v1")
                        .description("广东专插本公共英语备考工具 后端接口文档"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}