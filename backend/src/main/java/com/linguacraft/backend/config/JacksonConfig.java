package com.linguacraft.backend.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 序列化配置。
 *
 * <p>契约要求:所有实体主键 id 对外统一为字符串(前端 TS 类型为 string)。
 * 这里将包装类型 Long(实体主键使用 Long)统一序列化为字符串。</p>
 *
 * <p>注意:普通数值字段请使用原始类型(int/long)或 Integer/BigDecimal,
 * 避免被误序列化为字符串(原始类型 long 不受本配置影响)。</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册 Long -> String 序列化模块(Boot 会自动把 Module Bean 注册进全局 ObjectMapper)。
     *
     * @return Jackson 模块
     */
    @Bean
    public Module longToStringModule() {
        SimpleModule module = new SimpleModule("longToStringModule");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return module;
    }
}