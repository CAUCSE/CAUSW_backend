package net.causw.app.main.shared.infra.redis;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RedisUtils {

	private static final String REFRESH_TOKEN_PREFIX = "RefreshToken:";
	// 사용자별 refresh token 목록 key prefix
	private static final String USER_REFRESH_TOKENS_PREFIX = "UserRefreshTokens:";
	private static final String FCM_TOKEN_PREFIX = "FcmToken:";
	private static final String BLACKLIST_PREFIX = "Blacklist";

	private final RedisTemplate<String, Object> redisTemplate;

	/* 로그인 관련 Redis 메서드
	 * 1. RefreshToken
	 * Key : RefreshToken:{RefreshToken 값}
	 * Value: {User ID}
	 *
	 * 2. FcmToken
	 * Key : FcmToken:{FcmToken 값}
	 * Value: {RefreshToken 값}
	 *
	 * 3. Blacklist
	 * Key: Blacklist:{AccessToken 값}
	 * Value: "BLACKLISTED"
	 */

	/**
	 * Refresh Token을 Redis에 저장하고, 사용자 ID 기준 역방향 인덱스에도 등록합니다.
	 * <p>
	 * 저장 구조:
	 * <ul>
	 * <li>{@code RefreshToken:{refreshToken} -> userId}</li>
	 * <li>{@code UserRefreshTokens:{userId} -> Set<refreshToken>}</li>
	 * </ul>
	 */
	public void setRefreshTokenData(String key, String value, Long expiredTime) {
		String redisKey = REFRESH_TOKEN_PREFIX + key;
		redisTemplate.opsForValue().set(redisKey, value, expiredTime, TimeUnit.MILLISECONDS);

		String userRefreshTokensKey = USER_REFRESH_TOKENS_PREFIX + value;
		redisTemplate.opsForSet().add(userRefreshTokensKey, key);
		redisTemplate.expire(userRefreshTokensKey, expiredTime, TimeUnit.MILLISECONDS);

	}

	public String getRefreshTokenData(String key) {
		String redisKey = REFRESH_TOKEN_PREFIX + key;
		return (String)redisTemplate.opsForValue().get(redisKey);
	}

	/**
	 * Refresh Token을 Redis에서 삭제하고, 사용자 ID 기준 인덱스에서도 제거합니다.
	 *
	 * @param key 삭제할 refresh token
	 */
	public void deleteRefreshTokenData(String key) {
		String redisKey = REFRESH_TOKEN_PREFIX + key;
		String userId = (String)redisTemplate.opsForValue().get(redisKey);

		redisTemplate.delete(redisKey);

		if (userId != null) {
			redisTemplate.opsForSet().remove(USER_REFRESH_TOKENS_PREFIX + userId, key);
		}
	}

	/**
	 * 특정 사용자에게 발급된 모든 Refresh Token을 Redis에서 삭제합니다.
	 * <p>
	 * {@code UserRefreshTokens:{userId}}에 저장된 token 목록을 기준으로
	 * 개별 {@code RefreshToken:{refreshToken}} 키와 사용자별 인덱스 키를 함께 삭제합니다.
	 *
	 * @param userId refresh token을 일괄 삭제할 사용자 ID
	 */
	public void deleteAllRefreshTokensByUserId(String userId) {
		String userRefreshTokensKey = USER_REFRESH_TOKENS_PREFIX + userId;
		Set<Object> refreshTokens = redisTemplate.opsForSet().members(userRefreshTokensKey);

		if (refreshTokens != null) {
			for (Object refreshToken : refreshTokens) {
				redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
			}
		}

		redisTemplate.delete(userRefreshTokensKey);
	}

	public boolean existsRefreshToken(String key) {
		String redisKey = REFRESH_TOKEN_PREFIX + key;
		return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
	}

	public void setFcmTokenData(String fcmToken, String refreshToken, Long expiredTime) {
		String redisKey = FCM_TOKEN_PREFIX + fcmToken;
		redisTemplate.opsForValue().set(redisKey, refreshToken, expiredTime, TimeUnit.MILLISECONDS);
	}

	public String getFcmTokenData(String fcmToken) {
		String redisKey = FCM_TOKEN_PREFIX + fcmToken;
		return (String)redisTemplate.opsForValue().get(redisKey);
	}

	public void deleteFcmTokenData(String fcmToken) {
		String redisKey = FCM_TOKEN_PREFIX + fcmToken;
		redisTemplate.delete(redisKey);
	}

	public boolean existsFcmToken(String fcmToken) {
		String redisKey = FCM_TOKEN_PREFIX + fcmToken;
		return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
	}

	public void addToBlacklist(String token) {
		String redisKey = BLACKLIST_PREFIX + token;
		redisTemplate.opsForValue()
			.set(redisKey, "BLACKLISTED", StaticValue.JWT_ACCESS_TOKEN_VALID_TIME, TimeUnit.SECONDS);
	}

	public boolean isTokenBlacklisted(String token) {
		String redisKey = BLACKLIST_PREFIX + token;
		return "BLACKLISTED".equals(redisTemplate.opsForValue().get(redisKey));
	}

	/* UuidFile 관련 Redis 메서드
	 * 1. PageNum
	 * Key: {tableName}PageNum
	 * Value: {PageNum}
	 */
	public void setPageNumData(String tableName, Integer pageNum, Long expiredTime) {
		String redisKey = tableName + "PageNum";
		redisTemplate.opsForValue().set(redisKey, pageNum, expiredTime, TimeUnit.MILLISECONDS);
	}

	public Integer getPageNumData(String tableName) {
		String redisKey = tableName + "PageNum";
		return (Integer)redisTemplate.opsForValue().get(redisKey);
	}
}
