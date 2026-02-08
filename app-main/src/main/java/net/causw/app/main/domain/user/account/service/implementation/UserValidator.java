package net.causw.app.main.domain.user.account.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.infra.redis.RedisUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

	private final RedisUtils redisUtils;

	public void validateRefreshToken(String userId, String refreshToken) {
		String userIdFromRedis = Optional.ofNullable(redisUtils.getRefreshTokenData(refreshToken))
			.orElseThrow(AuthErrorCode.INVALID_REFRESH_TOKEN::toBaseException);

		if (!userId.equals(userIdFromRedis)) {
			throw AuthErrorCode.INVALID_REFRESH_TOKEN.toBaseException();
		}
	}
}
