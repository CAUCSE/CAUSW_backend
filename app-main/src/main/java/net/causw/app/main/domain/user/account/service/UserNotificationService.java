package net.causw.app.main.domain.user.account.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v1.mapper.UserDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.infra.firebase.FcmUtils;
import net.causw.app.main.shared.infra.redis.RedisUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationService {

	private final UserReader userReader;
	private final RedisUtils redisUtils;
	private final FcmUtils fcmUtils;

	@Transactional
	public UserFcmTokenResponseDto findFcmTokenByUser(String userId) {
		User validatedUser = userReader.findUserById(userId);
		fcmUtils.cleanInvalidFcmTokens(validatedUser);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(validatedUser);
	}

	@Transactional
	public UserFcmTokenResponseDto createFcmToken(String userId, String fcmToken, String refreshToken) {
		String userIdFromRedis = Optional.ofNullable(redisUtils.getRefreshTokenData(refreshToken))
			.orElseThrow(AuthErrorCode.INVALID_REFRESH_TOKEN::toBaseException);

		// 1. 유효한 refreshToken인지 검증
		if(!userId.equals(userIdFromRedis)) {
			throw AuthErrorCode.INVALID_REFRESH_TOKEN.toBaseException();
		}
		// 2. fcmToken 최신화
		User validatedUser = userReader.findUserById(userId);
		fcmUtils.cleanInvalidFcmTokens(validatedUser);
		// 3. fcmToken 추가
		fcmUtils.addFcmToken(validatedUser, refreshToken, fcmToken);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(validatedUser);
	}
}
