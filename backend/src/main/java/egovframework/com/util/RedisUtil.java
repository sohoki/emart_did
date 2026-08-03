package egovframework.com.util;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public Boolean isKeyNullCheck(String key) {
		try {
			if (redisTemplate.hasKey(key)) {
				if (redisTemplate.getExpire(key) < 0) {
					redisTemplate.delete(key);
					return false;
				}else {
					return true;
				}
			}else {
				return false;
			}
		} catch (Exception e) {
			// Redis 서버가 없어도 이 체크에 의존하는 기능(블랙리스트/리프레시 토큰 확인)이
			// 전체 요청을 500으로 죽이지 않게 함(로컬 개발 환경 등)
			log.warn("isKeyNullCheck error: {}", e.toString());
			return false;
		}
	}

	/*
	 * Redis key 저장
	 * List<Map<String, Object>> list 로 데이터 저장
	 * KEY_NM 으로 키 생성
	 *
	 */
	public Boolean commonCodeRedisRest(List<Map<String, Object>> list, String KEY_NM) {
		try {
			String jsonCodes = objectMapper.writeValueAsString(list);
			redisTemplate.opsForValue().set(KEY_NM, jsonCodes);
			return true;
		}catch(JsonProcessingException  e) {
			log.error("Redis 캐시 갱신 실패", e);
			return false;
		}catch(Exception e) {
			// Redis 서버가 없어도 콤보/코드 조회 자체는 계속 진행되게 함(캐시 갱신만 스킵)
			log.warn("Redis unavailable, common code cache update skipped: {}", e.toString());
			return false;
		}
	}
	public Boolean keyDelete(String key) {
		try {
			if (redisTemplate.hasKey(key))
			redisTemplate.delete(key);
			return true;
		}catch (NullPointerException e) {
			log.error("keyDelete error:" + e.toString());
			return false;
		}catch (Exception e) {
			log.error("keyDelete error:" + e.toString());
			return false;
		}
	}
}
