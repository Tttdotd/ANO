package com.tdotd.ano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 启用 Spring 的 {@link org.springframework.scheduling.annotation.Async} 支持，并注册线程池。
 * <p>
 * 知识归档会调用大模型与向量服务，耗时长；通过专用线程池与 HTTP 线程隔离，避免占满 Tomcat 工作线程。
 * 未显式指定 executor 名的 {@code @Async} 方法会使用名为 {@code taskExecutor} 的 Bean（Spring 约定）。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("knowledge-archive-");
        // 调用方已返回后，拒绝策略只影响后台任务；CallerRunsPolicy 可在队列满时略降速，避免静默丢弃
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
