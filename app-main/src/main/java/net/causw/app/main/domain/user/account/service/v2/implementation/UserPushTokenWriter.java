package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPushTokenWriter {

	private final RedisUtils redisUtils;

	/**
	 * 새로운 FCM 토큰을 등록하고 Redis에 메타데이터 저장
	 */
	public void addFcmToken(User user, String refreshToken, String fcmToken) {
		if (!redisUtils.existsFcmToken(fcmToken)) {
			user.getFcmTokens().add(fcmToken);
			redisUtils.setFcmTokenData(
				fcmToken,
				refreshToken,
				StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
		}
	}

	/**
	 * 특정 FCM 토큰 제거
	 */
	public void removeFcmToken(User user, String fcmToken) {
		user.removeFcmToken(fcmToken);
		redisUtils.deleteFcmTokenData(fcmToken);
	}

	/**
	 * 유저의 FCM 토큰 중 유효하지 않은 토큰들을 식별하고 제거를 결정함
	 */
	public void cleanInvalidFcmTokens(User user) {
		Set<String> currentTokens = new HashSet<>(user.getFcmTokens());

		for (String fcmToken : currentTokens) {
			// 1. Redis에 토큰 정보가 없는 경우
			if (!redisUtils.existsFcmToken(fcmToken)) {
				user.removeFcmToken(fcmToken);
				continue;
			}

			// 2. Redis에 토큰은 있으나 연결된 RefreshToken이 만료/삭제된 경우
			String refreshToken = redisUtils.getFcmTokenData(fcmToken);
			if (refreshToken == null || !redisUtils.existsRefreshToken(refreshToken)) {
				user.removeFcmToken(fcmToken);
				redisUtils.deleteFcmTokenData(fcmToken);
			}
		}
	}
}
