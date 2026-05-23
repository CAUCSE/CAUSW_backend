package net.causw.app.main.domain.user.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.api.v1.dto.UserFcmTokenResponseDto;
import net.causw.app.main.domain.user.account.api.v1.mapper.UserDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationService {

	private final UserReader userReader;
	private final UserPushTokenWriter userPushTokenWriter;

	@Transactional
	public UserFcmTokenResponseDto findFcmTokenByUser(String userId) {
		User validatedUser = userReader.findUserById(userId);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(validatedUser);
	}

	@Transactional
	public UserFcmTokenResponseDto createFcmToken(String userId, String fcmToken) {
		User validatedUser = userReader.findUserById(userId);
		userPushTokenWriter.addFcmToken(validatedUser, fcmToken);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(validatedUser);
	}
}
