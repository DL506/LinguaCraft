package com.linguacraft.backend;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 外部容器的引导入口(打包成 war 后必需)。
 *
 * <p>同一个 war 有两种跑法:{@code java -jar} 走 {@code BackendApplication.main} 起内嵌 Tomcat;
 * 丢进外部 Tomcat 的 {@code webapps/} 时容器不调用 {@code main},而是回调这里的 {@code onStartup},
 * 由它用 {@link BackendApplication} 作为来源装配 Spring 上下文。</p>
 *
 * <p>本项目的目标容器是 <b>Tomcat 11.0.x</b>:Spring Boot 4.1 走 Servlet 6.1,
 * Tomcat 9 / 10.1 都装不上这份 war。</p>
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * 指定外部容器引导时使用的启动类。
     *
     * @param application Spring 应用构建器
     * @return 构建好的应用
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BackendApplication.class);
    }
}
