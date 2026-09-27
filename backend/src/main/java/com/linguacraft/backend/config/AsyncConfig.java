package com.linguacraft.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步任务配置:启用 @Async 并定义 AI 专用线程池。
 *
 * <p>用途:模考交卷后的作文 AI 评分异步执行(交卷响应不被模型耗时阻塞);
 * 线程池容量按本地演示规模设定(核心 2 / 最大 4 / 队列 32)。</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AI 任务线程池(bean 名 aiExecutor,@Async("aiExecutor") 引用)。
     *
     * @return 线程池执行器
     */
    @Bean(name = "aiExecutor")
    public ThreadPoolTaskExecutor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(32);
        executor.setThreadNamePrefix("ai-");
        executor.initialize();
        return executor;
    }
}