package egovframework.com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 비동기 기능 활성화
public class AsyncConfig {

	@Bean(name = "smsTaskExecutor")
	public Executor smsTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		
		// 서비스 특성에 맞춰 조절하세요
		executor.setCorePoolSize(10);        // 기본 스레드 수
		executor.setMaxPoolSize(20);       // 최대 스레드 수
		executor.setQueueCapacity(2000);    // 대기 큐 크기
		executor.setThreadNamePrefix("SmsAsync-"); // 로그에서 식별하기 위한 이름
		// 큐까지 꽉 찼을 때 예외를 던지지 않고 호출한 스레드(Main)에서 직접 처리하게 함 (안전장치)
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		
		// 서버 종료 시 진행 중인 작업 완료 후 종료
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		
		
		executor.initialize();
		return executor;
	}
}
