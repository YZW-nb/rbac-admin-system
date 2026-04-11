package com.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 使用 Spring 默认的 SimpleAsyncTaskExecutor
    // 如需线程池，可在 @Async 后指定 executor bean 名称
}
