package net.causw.app.main.domain.user.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserFcmTokenResponse;
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
	public UserFcmTokenResponse findFcmTokenByUser(String userId) {
		User validatedUser = userReader.findUserById(userId);
		return UserFcmTokenResponse.builder()
			.fcmToken(validatedUser.getFcmTokens())
			.build();
	}

	@Transactional
	public UserFcmTokenResponse createFcmToken(String userId, String fcmToken) {
		User validatedUser = userReader.findUserById(userId);
		userPushTokenWriter.addFcmToken(validatedUser, fcmToken);
		return UserFcmTokenResponse.builder()
			.fcmToken(validatedUser.getFcmTokens())
			.build();
	}
}
