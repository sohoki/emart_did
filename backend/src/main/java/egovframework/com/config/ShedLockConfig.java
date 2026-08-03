package egovframework.com.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 다중 인스턴스(K8s 레플리카) 환경에서 @Scheduled 배치(DashBoardManageService, ReservationStatCollectJob 등)가
 * 인스턴스마다 독립적으로 실행되어 중복 실행되는 것을 막는다. Redis(RedisConfig의 redisConnectionFactory 빈
 * 재사용)에 락을 걸어, 같은 시각에 여러 인스턴스가 깨어나도 한 곳만 실제로 실행하도록 보장한다.
 *
 * environment 값을 활성 프로필로 구분하는 이유: local/prod/server 프로필이 전부 같은 Redis 서버
 * (application.yml의 spring.data.redis.host)를 바라보고 있어서, 구분 없이 고정 문자열을 쓰면 로컬
 * 개발 인스턴스가 실서버 배치의 락을 가로채거나 그 반대가 될 수 있다.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class ShedLockConfig {

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Bean
	public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
		return new RedisLockProvider(connectionFactory, "hotelPms-" + activeProfile);
	}
}
