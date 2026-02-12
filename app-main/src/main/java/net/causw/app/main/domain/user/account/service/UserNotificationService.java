package net.causw.app.main.domain.user.account.service;

import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v1.mapper.UserDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserPushTokenWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationService {

	private final UserReader userReader;
	private final UserPushTokenWriter userPushTokenWriter;
	private final UserValidator userValidator;

	@Transactional
	public UserFcmTokenResponseDto findFcmTokenByUser(String userId) {
		User validatedUser = userReader.findUserById(userId);
		userPushTokenWriter.cleanInvalidFcmTokens(validatedUser);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(validatedUser);
	}

	@Transactional
	public UserFcmTokenResponseDto createFcmToken(String userId, String fcmToken, String refreshToken) {
		// 1. 유효한 refreshToken인지 검증
		userValidator.validateRefreshToken(userId, refreshToken);
		// 2. fcmToken 최신화
		User validatedUser = userReader.findUserById(userId);
		userPushTokenWriter.cleanInvalidFcmTokens(validatedUser);
		// 3. fcmToken 추가
		userPushTokenWriter.addFcmToken(validatedUser, refreshToken, fcmToken);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(validatedUser);
	}
}
