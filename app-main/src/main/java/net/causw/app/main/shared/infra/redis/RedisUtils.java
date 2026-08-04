package net.causw.app.main.shared.infra.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RedisUtils {

	private static final String REFRESH_TOKEN_PREFIX = "RefreshToken:";
	// 사용자별 refresh token 목록 key prefix
	private static final String USER_REFRESH_TOKENS_PREFIX = "UserRefreshTokens:";
	private static final String REFRESH_TOKEN_USER_INDEX_MIGRATION_KEY = "Migration:RefreshTokenUserIndex:v1";
	private static final String COLLIDED_REFRESH_TOKEN_CLEANUP_KEY = "Migration:CollidedRefreshTokenCleanup:v1";
	private static final String BLACKLIST_PREFIX = "Blacklist";

	private final RedisTemplate<String, Object> redisTemplate;

	/* 로그인 관련 Redis 메서드
	 * 1. RefreshToken
	 * Key : RefreshToken:{RefreshToken 값}
	 * Value: {User ID}
	 *
	 * 2. Blacklist
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
		syncUserRefreshTokenIndexTtl(redisKey, userRefreshTokensKey);
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
		Object userId = redisTemplate.opsForValue().get(redisKey);

		redisTemplate.delete(redisKey);

		if (userId instanceof String userIdValue) {
			redisTemplate.opsForSet().remove(USER_REFRESH_TOKENS_PREFIX + userIdValue, key);
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

		if (refreshTokens != null && !refreshTokens.isEmpty()) {
			Set<String> refreshTokenKeys = refreshTokens.stream()
				.map(refreshToken -> REFRESH_TOKEN_PREFIX + refreshToken)
				.collect(Collectors.toSet());
			redisTemplate.delete(refreshTokenKeys);
		}

		redisTemplate.delete(userRefreshTokensKey);
	}

	/**
	 * 기존 {@code RefreshToken:{refreshToken} -> userId} 데이터에서 사용자별 refresh token 인덱스를 생성합니다.
	 * <p>
	 * 배포 전 Redis에 이미 존재하던 refresh token은 {@code UserRefreshTokens:{userId}} 인덱스에 포함되지 않으므로,
	 * 애플리케이션 기동 시 1회 백필합니다.
	 *
	 * @return 인덱스에 추가된 refresh token 개수
	 */
	public int migrateRefreshTokenUserIndex() {
		if (redisTemplate.hasKey(REFRESH_TOKEN_USER_INDEX_MIGRATION_KEY)) {
			return 0;
		}

		int migratedCount = scanRefreshTokenKeysAndBuildUserIndex();
		redisTemplate.opsForValue().set(REFRESH_TOKEN_USER_INDEX_MIGRATION_KEY, "DONE");
		return migratedCount;
	}

	private int scanRefreshTokenKeysAndBuildUserIndex() {
		ScanOptions scanOptions = ScanOptions.scanOptions()
			.match(REFRESH_TOKEN_PREFIX + "*")
			.count(1000)
			.build();

		int migratedCount = 0;
		try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
			while (cursor.hasNext()) {
				String redisKey = cursor.next();

				Object userId = redisTemplate.opsForValue().get(redisKey);
				if (!(userId instanceof String userIdValue) || userIdValue.isBlank()) {
					continue;
				}

				String refreshToken = redisKey.substring(REFRESH_TOKEN_PREFIX.length());
				String userRefreshTokensKey = USER_REFRESH_TOKENS_PREFIX + userIdValue;
				redisTemplate.opsForSet().add(userRefreshTokensKey, refreshToken);
				syncUserRefreshTokenIndexTtl(redisKey, userRefreshTokensKey);
				migratedCount++;
			}
		}
		return migratedCount;
	}

	/**
	 * 서로 다른 사용자에게 중복 발급된 refresh token을 찾아 폐기합니다.
	 * <p>
	 * jti 도입 이전에는 같은 초에 발급된 token 문자열이 동일해 {@code RefreshToken:{refreshToken} -> userId}
	 * 매핑이 덮어써졌습니다. {@code UserRefreshTokens:{userId}}는 Set이라 충돌한 token이 양쪽 인덱스에
	 * 남아 있으므로, 2명 이상에게 매핑된 token만 선별 폐기합니다.
	 *
	 * @return 폐기된 refresh token 개수
	 */
	public int purgeCollidedRefreshTokens() {
		if (redisTemplate.hasKey(COLLIDED_REFRESH_TOKEN_CLEANUP_KEY)) {
			return 0;
		}

		int purgedCount = scanUserIndexAndPurgeCollidedTokens();
		redisTemplate.opsForValue().set(COLLIDED_REFRESH_TOKEN_CLEANUP_KEY, "DONE");
		return purgedCount;
	}

	private int scanUserIndexAndPurgeCollidedTokens() {
		int purgedCount = 0;

		for (Map.Entry<String, Set<String>> entry : collectUserIdsByRefreshToken().entrySet()) {
			Set<String> userIds = entry.getValue();
			if (userIds.size() < 2) {
				continue;
			}

			String refreshToken = entry.getKey();
			redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
			userIds.forEach(
				userId -> redisTemplate.opsForSet().remove(USER_REFRESH_TOKENS_PREFIX + userId, refreshToken));
			purgedCount++;
		}

		return purgedCount;
	}

	private Map<String, Set<String>> collectUserIdsByRefreshToken() {
		ScanOptions scanOptions = ScanOptions.scanOptions()
			.match(USER_REFRESH_TOKENS_PREFIX + "*")
			.count(1000)
			.build();

		Map<String, Set<String>> userIdsByRefreshToken = new HashMap<>();
		try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
			while (cursor.hasNext()) {
				String userRefreshTokensKey = cursor.next();
				String userId = userRefreshTokensKey.substring(USER_REFRESH_TOKENS_PREFIX.length());

				Set<Object> refreshTokens = redisTemplate.opsForSet().members(userRefreshTokensKey);
				if (refreshTokens == null) {
					continue;
				}

				for (Object refreshToken : refreshTokens) {
					if (refreshToken instanceof String refreshTokenValue) {
						userIdsByRefreshToken.computeIfAbsent(refreshTokenValue, key -> new HashSet<>()).add(userId);
					}
				}
			}
		}
		return userIdsByRefreshToken;
	}

	private void syncUserRefreshTokenIndexTtl(String refreshTokenKey, String userRefreshTokensKey) {
		Long refreshTokenTtl = redisTemplate.getExpire(refreshTokenKey, TimeUnit.MILLISECONDS);
		if (refreshTokenTtl == null || refreshTokenTtl <= 0) {
			return;
		}

		Long indexTtl = redisTemplate.getExpire(userRefreshTokensKey, TimeUnit.MILLISECONDS);
		if (indexTtl == null || indexTtl < refreshTokenTtl) {
			redisTemplate.expire(userRefreshTokensKey, refreshTokenTtl, TimeUnit.MILLISECONDS);
		}
	}

	public void addToBlacklist(String token) {
		String redisKey = BLACKLIST_PREFIX + token;
		redisTemplate.opsForValue()
			.set(redisKey, "BLACKLISTED", StaticValue.JWT_ACCESS_TOKEN_VALID_TIME, TimeUnit.MILLISECONDS);
	}

	public boolean isTokenBlacklisted(String token) {
		String redisKey = BLACKLIST_PREFIX + token;
		return "BLACKLISTED".equals(redisTemplate.opsForValue().get(redisKey));
	}
}
