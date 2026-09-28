package com.linguacraft.backend.config;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 序列化配置(Boot 4 使用 Jackson 3,包名为 tools.jackson.*)。
 *
 * <p>契约要求:所有实体主键 id 对外统一为字符串(前端 TS 类型为 string)。
 * 这里将包装类型 Long(实体主键使用 Long)统一序列化为字符串。</p>
 *
 * <p>注意:普通数值字段请使用原始类型(int/long)或 Integer/BigDecimal,
 * 避免被误序列化为字符串(原始类型 long 不受本配置影响)。</p>
 *
 * <p>踩坑记录:Spring Boot 4 的 Jackson 自动配置只收集 {@code tools.jackson.databind.JacksonModule}
 * 类型的 Bean。若误用 Jackson 2 的 {@code com.fasterxml.jackson.databind.Module},该 Bean 会被静默忽略,
 * 导致 Long→String 规则根本不生效(应用也不会报错,只是 id 仍以数字下发)。</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册 Long -> String 序列化模块(Boot 会自动把 JacksonModule Bean 注册进全局 JsonMapper)。
     *
     * @return Jackson 3 模块
     */
    @Bean
    public JacksonModule longToStringModule() {
        SimpleModule module = new SimpleModule("longToStringModule");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return module;
    }
}