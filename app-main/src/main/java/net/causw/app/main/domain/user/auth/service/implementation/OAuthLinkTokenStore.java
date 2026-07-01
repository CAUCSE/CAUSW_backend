package net.causw.app.main.domain.user.auth.service.implementation;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 계정 연동 OAuth 플로우에서 사용하는 1회용 링크 토큰을 Redis로 관리합니다.
 * <p>
 * 토큰은 {@code OAuthLinkToken:{uuid}} 키로 저장되며 5분 TTL을 가집니다.
 * consumeToken() 호출 시 userId를 반환하고 즉시 삭제합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuthLinkTokenStore {

	/** 프론트엔드가 /oauth2/authorization/{provider}에 전달하는 쿼리 파라미터 이름 */
	public static final String LINK_TOKEN_QUERY_PARAM = "linkToken";

	/** OAuth2AuthorizationRequest.attributes 및 HttpServletRequest.attribute에 링크 토큰을 저장할 때 사용하는 키 */
	public static final String LINK_TOKEN_ATTR = "oauth_link_token";

	/** 연동 플로우임을 하위 핸들러에 전달하기 위해 HttpServletRequest.attribute에 저장할 userId 키 */
	public static final String LINK_USER_ID_ATTR = "oauth_link_user_id";

	private static final String REDIS_KEY_PREFIX = "OAuthLinkToken:";
	private static final Duration TTL = Duration.ofMinutes(5);

	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * userId를 매핑한 1회용 링크 토큰을 생성하여 Redis에 저장하고 반환합니다.
	 */
	public String issueToken(String userId) {
		String token = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + token, userId, TTL);
		return token;
	}

	/**
	 * 토큰에 매핑된 userId를 반환하고 Redis에서 즉시 삭제합니다 (1회용).
	 *
	 * @return userId, 토큰이 존재하지 않거나 만료된 경우 null
	 */
	public String consumeToken(String token) {
		if (!StringUtils.hasText(token)) {
			return null;
		}
		String key = REDIS_KEY_PREFIX + token;
		Object value = redisTemplate.opsForValue().get(key);
		if (value == null) {
			return null;
		}
		redisTemplate.delete(key);
		return (String)value;
	}

	/**
	 * 토큰을 Redis에서 삭제합니다. 이미 없는 경우 no-op입니다.
	 */
	public void deleteToken(String token) {
		if (StringUtils.hasText(token)) {
			redisTemplate.delete(REDIS_KEY_PREFIX + token);
		}
	}
}
