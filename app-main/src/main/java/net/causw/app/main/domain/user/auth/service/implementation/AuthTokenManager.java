package net.causw.app.main.domain.user.auth.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.core.security.JwtTokenProvider;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.service.dto.AuthTokenPair;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

/**
 * 인증 토큰(Access Token, Refresh Token)의 생명주기를 관리하는 컴포넌트입니다.
 * <p>
 * 토큰 발급(Issue), 조회(Retrieve), 무효화(Invalidate) 기능을 수행하며,
 * Redis를 통해 Refresh Token의 저장 및 Access Token의 블랙리스트 처리를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenManager {

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisUtils redisUtils;

	/**
	 * 사용자의 정보를 바탕으로 새로운 액세스 토큰과 리프레시 토큰 쌍을 발급합니다.
	 * <p>
	 * <b>RTR(Refresh Token Rotation) 정책 적용:</b><br>
	 * 재발급 요청(`oldRefreshToken` 존재)인 경우, 기존 리프레시 토큰을 Redis에서 삭제하여
	 * 한 번 사용된 토큰은 폐기하고 새로운 토큰으로 교체합니다.
	 *
	 * @param user            토큰 Payload에 포함될 사용자 정보 (ID, Role, State)
	 * @param oldRefreshToken 교체할 기존 리프레시 토큰 (로그인 시에는 null, 재발급 시에는 필수)
	 * @return 새로 생성된 Access Token과 Refresh Token이 담긴 객체
	 */
	public AuthTokenPair issueTokens(User user, String oldRefreshToken) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState());
		if (oldRefreshToken != null) {
			redisUtils.deleteRefreshTokenData(oldRefreshToken);
		}
		String refreshToken = jwtTokenProvider.createRefreshToken();
		redisUtils.setRefreshTokenData(refreshToken, user.getId(), StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
		return new AuthTokenPair(accessToken, refreshToken);
	}

	public String createRefreshToken(String userId) {
		String refreshToken = jwtTokenProvider.createRefreshToken();
		redisUtils.setRefreshTokenData(refreshToken, userId, StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
		return refreshToken;
	}

	/**
	 * 리프레시 토큰을 키(Key)로 사용하여 Redis에 저장된 사용자 ID를 조회합니다.
	 *
	 * @param refreshToken 클라이언트로부터 전달받은 리프레시 토큰
	 * @return 토큰에 매핑되어 있던 사용자 ID (String)
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 토큰이 만료되었거나, Redis에 존재하지 않는 경우 (INVALID_REFRESH_TOKEN)
	 */
	public String getUserIdFromRefreshToken(String refreshToken) {
		Object storedValue = redisUtils.getRefreshTokenData(refreshToken);
		return Optional.ofNullable(storedValue)
			.map(Object::toString)
			.orElseThrow(AuthErrorCode.INVALID_REFRESH_TOKEN::toBaseException);
	}

	/**
	 * 로그아웃 시 토큰들을 무효화 처리합니다.
	 * <p>
	 * 1. <b>Access Token:</b> 남은 유효 시간 동안 블랙리스트에 등록하여 재사용을 막습니다.<br>
	 * 2. <b>Refresh Token:</b> Redis에서 해당 데이터를 영구 삭제합니다.
	 *
	 * @param accessToken  무효화할(블랙리스트에 등록할) 액세스 토큰
	 * @param refreshToken 삭제할 리프레시 토큰 (null일 경우 생략 가능)
	 */
	public void invalidateTokens(String accessToken, String refreshToken) {
		if (accessToken != null && !accessToken.isBlank()) {
			redisUtils.addToBlacklist(accessToken);
		}
		if (refreshToken != null && !refreshToken.isBlank()) {
			redisUtils.deleteRefreshTokenData(refreshToken);
		}
	}
}
