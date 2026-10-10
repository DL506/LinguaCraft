package com.linguacraft.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 运行期配置自检:启动完成后检查关键环境变量是否显式注入。
 *
 * <p>本项目的 {@code application.yml} 为便于本地起步,给 AI 端点、JWT 密钥、数据库口令都写了明文默认值。
 * 这些默认值一旦带进对外环境就是可用凭据,因此这里在每次启动时逐项提示"仍在使用内置默认值"。</p>
 *
 * <p>刻意<b>只报变量名、不打印任何值</b>:日志会被收集与截屏,复述凭据等于把密钥从配置文件搬进日志。
 * 判定方式是"环境变量是否设置"而不是"值是否等于某个默认串",这样源码里不必再出现一份默认密钥副本。</p>
 */
@Component
public class RuntimeSafetyReporter {

    private static final Logger log = LoggerFactory.getLogger(RuntimeSafetyReporter.class);

    /** 必须显式注入的变量:缺失即代表正在使用入库的默认值 */
    private static final List<String> REQUIRED_ENV = List.of(
            "LC_AI_API_KEY", "LC_AI_BASE_URL", "LC_AI_MODEL", "LC_JWT_SECRET", "LC_DB_PASSWORD");

    /** 缺失时对本次运行造成的实际影响(用于把 WARN 说成可行动的信息,而不是一句"不安全") */
    private static final String AI_IMPACT = "AI 出题会返回 code 500,作文与无答案客观题将降级为待批改";

    /**
     * 启动完成后输出自检结果。
     *
     * @param event 应用就绪事件(仅用于触发时机)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void report(ApplicationReadyEvent event) {
        List<String> missing = REQUIRED_ENV.stream()
                .filter(name -> {
                    String value = System.getenv(name);
                    return value == null || value.isBlank();
                })
                .toList();
        if (missing.isEmpty()) {
            log.info("配置自检:关键环境变量均已显式注入");
            return;
        }
        for (String name : missing) {
            String impact = name.startsWith("LC_AI_") ? AI_IMPACT : "正在使用 application.yml 内置默认值";
            log.warn("环境变量 {} 未设置 —— {};对外环境必须显式注入(样例见 backend/.env.example)", name, impact);
        }
    }
}
