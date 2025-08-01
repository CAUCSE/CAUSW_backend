package net.causw.app.main.infrastructure.redis.auth;

import java.util.concurrent.TimeUnit;

import net.causw.global.constant.StaticValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthRedisService {

	@Qualifier("authRedisTemplate")
	private final RedisTemplate<String, String> authRedisTemplate;

	/** 로그인 관련 Redis 메서드
	 * 1. RefreshToken
	 * Key: RefreshToken:{RefreshToken 값}
	 * Value: {User ID}
	 *
	 * 2. Blacklist
	 * Key: Blacklist:{AccessToken 값}
	 * Value: "BLACKLISTED"
	 */
	public void setRefreshTokenData(String key, String value, Long expiredTime) {
		String redisKey = "RefreshToken:" + key;
		authRedisTemplate.opsForValue().set(redisKey, value, expiredTime, TimeUnit.MILLISECONDS);
	}

	public String getRefreshTokenData(String key) {
		String redisKey = "RefreshToken:" + key;
		return authRedisTemplate.opsForValue().get(redisKey);
	}

	public void deleteRefreshTokenData(String key) {
		String redisKey = "RefreshToken:" + key;
		authRedisTemplate.delete(redisKey);
	}

	public void addToBlacklist(String token) {
		String redisKey = "Blacklist" + token;
		authRedisTemplate.opsForValue()
			.set(redisKey, "BLACKLISTED", StaticValue.JWT_ACCESS_TOKEN_VALID_TIME, TimeUnit.SECONDS);
	}

	public boolean isTokenBlacklisted(String token) {
		String redisKey = "Blacklist" + token;
		return "BLACKLISTED".equals(authRedisTemplate.opsForValue().get(redisKey));
	}
} 