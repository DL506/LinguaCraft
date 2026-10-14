package com.linguacraft.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * war 自带前端 dist 后的授权面与 SPA 回退测试(打包改造)。
 *
 * <p>守两件事,方向相反:</p>
 * <ol>
 *   <li><b>页面必须能匿名打开</b>。此前 {@code anyRequest().authenticated()} 把 {@code /}、
 *       {@code /assets/*}、前端路由一起吃掉,war 里就算带着 dist,{@code java -jar} 打开也是 401 白屏;</li>
 *   <li><b>业务接口一格都不能松</b>。所有控制器都在 {@code /api} 下,放行页面不等于放行数据。</li>
 * </ol>
 *
 * <p>断言只写"是不是 401",不写具体状态:页面路径最终落到 {@code index.html},
 * 而 {@code dist} 是否存在取决于本次构建有没有跑过前端 —— 那不该让这条测试变红。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpaDeploymentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SPA 顶层路由匿名可访问(不再 401)")
    void spaRoutesArePubliclyReachable() throws Exception {
        for (String path : new String[]{"/login", "/dashboard", "/exam/exam-2026", "/admin/exam-papers", "/user/settings"}) {
            mockMvc.perform(get(path))
                    .andExpect(result -> org.assertj.core.api.Assertions
                            .assertThat(result.getResponse().getStatus())
                            .as("前端路径 %s 不该被安全层拒绝", path)
                            .isNotEqualTo(401));
        }
    }

    @Test
    @DisplayName("静态资源匿名可访问(前端产物入口)")
    void staticAssetsArePubliclyReachable() throws Exception {
        for (String path : new String[]{"/", "/index.html", "/assets/app.js", "/manifest.webmanifest"}) {
            mockMvc.perform(get(path))
                    .andExpect(result -> org.assertj.core.api.Assertions
                            .assertThat(result.getResponse().getStatus())
                            .as("静态路径 %s 不该被安全层拒绝", path)
                            .isNotEqualTo(401));
        }
    }

    @Test
    @DisplayName("业务接口仍要令牌:学生端与后台都是 401")
    void apiStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/exam/papers")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/questions")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/user/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api 下的未知路径不会被 SPA 回退吞成页面(仍是 401,而不是 200 + HTML)")
    void unknownApiPathIsNotSwallowedBySpaForward() throws Exception {
        // 刻意用 /api 前缀:若回退写成通配,这里会以"页面"的身份被放行并返回 200
        mockMvc.perform(get("/api/does-not-exist")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("冒烟与元数据端点保持原样放行")
    void smokeAndMetadataUnchanged() throws Exception {
        mockMvc.perform(get("/api/system/ping")).andExpect(status().isOk());
        // health 含数据源指示器:本机没起 MySQL 时会是 503,这条只关心"有没有被安全层拒绝"
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> org.assertj.core.api.Assertions
                        .assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }
}
