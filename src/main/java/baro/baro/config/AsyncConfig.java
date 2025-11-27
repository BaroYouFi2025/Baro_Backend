package baro.baro.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// 비동기 처리 설정
// - 이벤트 리스너의 @Async 메서드를 위한 설정
// - 사진 처리 등 시간이 오래 걸리는 작업을 별도 스레드풀에서 처리
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    // 비동기 작업용 스레드풀 설정
    // - 코어 스레드: CPU 코어 수
    // - 최대 스레드: CPU 코어 수 * 2
    // - 큐 용량: 500 (대기 작업 최대 500개)
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 수 (항상 유지)
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // 최대 스레드 수 (부하 시 확장)
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 대기 큐 크기
        executor.setQueueCapacity(500);

        // 스레드 이름 prefix
        executor.setThreadNamePrefix("async-photo-");

        // 종료 시 대기 중인 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 종료 대기 시간 (30초)
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    // 비동기 작업 중 발생한 예외 처리
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 작업 중 예외 발생 - 메서드: {}, 파라미터: {}", method.getName(), params, ex);
        };
    }
}
