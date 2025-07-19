package net.causw.config.async;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);           // 기본 스레드 수
        executor.setMaxPoolSize(20);           // 최대 스레드 수
        executor.setQueueCapacity(100);        // 큐에 쌓이는 작업 수
        executor.setThreadNamePrefix("Async-"); // 로그 확인용 쓰레드 이름 접두사
        executor.initialize();
        return executor;
    }
}