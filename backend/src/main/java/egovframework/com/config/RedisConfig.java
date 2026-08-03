package egovframework.com.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
@EnableRedisRepositories
public class RedisConfig {

	/**
	 * Redis 서버가 없어도(로컬 개발 환경 등) @Cacheable/@CacheEvict/@CachePut이 붙은 메서드가
	 * 예외를 던지지 않고 원래 로직(캐시 미스처럼 DB 직접 조회)을 계속 수행하게 함. Spring Boot의
	 * 캐시 자동 구성이 이 빈을 자동으로 감지해서 전역 CacheInterceptor에 적용한다.
	 */
	@Bean
	public CacheErrorHandler cacheErrorHandler() {
		return new CacheErrorHandler() {
			@Override
			public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
				log.warn("Redis cache GET error(cache={}, key={}): {}", cache.getName(), key, e.toString());
			}
			@Override
			public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
				log.warn("Redis cache PUT error(cache={}, key={}): {}", cache.getName(), key, e.toString());
			}
			@Override
			public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
				log.warn("Redis cache EVICT error(cache={}, key={}): {}", cache.getName(), key, e.toString());
			}
			@Override
			public void handleCacheClearError(RuntimeException e, Cache cache) {
				log.warn("Redis cache CLEAR error(cache={}): {}", cache.getName(), e.toString());
			}
		};
	}

	@Value("${spring.data.redis.host}")
	private String redisHost;
	
	@Value("${spring.data.redis.port}")
	private int redisPort;
	
	@Value("${spring.data.redis.password}")
	private String password;
	
	
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// default typing 비활성화 ObjectMapper 사용:
		// GenericJackson2JsonRedisSerializer 기본 생성자는 activateDefaultTyping 을 켜서
		// ["java.util.ArrayList",[...]] 형태로 저장하지만, 이미 Redis에 저장된 데이터나
		// List<Map<String,Object>> 반환 타입은 순수 JSON 으로도 역직렬화 가능하므로
		// 타입 메타데이터 없는 순수 JSON 직렬화기를 사용한다.
		ObjectMapper mapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
				.entryTtl(Duration.ofMinutes(30)) //30분 마다 변경
				.disableCachingNullValues();
		
	
		// 특정 캐시 이름별로 TTL 다르게 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		//cacheConfigurations.put("findLeftMenu", config.entryTtl(Duration.ofHours(1))); // 1시간
		//cacheConfigurations.put("DETAIL_CODE_ALL", config.entryTtl(Duration.ofDays(1))); // 1일
	
		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(config)
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}
	
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
		config.setPassword(password);
		return new LettuceConnectionFactory(config);
	}
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		
		// Key와 Value 모두 문자열 기반으로 직렬화
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		
		// Hash 구조를 사용한다면 HashKey/Value도 설정하는 것이 안전합니다
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());
		
		return redisTemplate;
	}
}
