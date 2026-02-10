package net.causw.app.main.domain.user.auth.service.v2.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.core.security.JwtTokenProvider;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.service.v2.dto.AuthTokenPair;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthTokenManager {

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisUtils redisUtils;

	public AuthTokenPair issueTokens(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState());
		String refreshToken = jwtTokenProvider.createRefreshToken();
		redisUtils.setRefreshTokenData(refreshToken, user.getId(), StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
		return new AuthTokenPair(accessToken, refreshToken);
	}
}
