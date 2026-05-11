package net.causw.app.main.shared.infra.firebase;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

/**
 * V2 이후 FCM 토큰 CRUD 단일 진입점.
 * FcmUtils를 대체하며, 동시수정 방어와 auto-save를 포함한다.
 */
@RequiredArgsConstructor
@Component
public class FcmTokenManager {

	private final RedisUtils redisUtils;
	private final UserRepository userRepository;

	/**
	 * 특정 user에게 fcm 토큰 추가
	 * @param user 유저
	 * @param refreshToken 리프레시 토큰
	 * @param fcmToken fcm 토큰
	 */
	public void addFcmToken(User user, String refreshToken, String fcmToken) {
		if (!redisUtils.existsFcmToken(fcmToken)) {
			user.getFcmTokens().add(fcmToken);
			redisUtils.setFcmTokenData(fcmToken, refreshToken, StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
			userRepository.save(user);
		}
	}

	/**
	 * 특정 user의 fcm 토큰 삭제
	 * @param user user
	 * @param fcmToken fcmtoken
	 */
	public void removeFcmToken(User user, String fcmToken) {
		user.removeFcmToken(fcmToken);
		redisUtils.deleteFcmTokenData(fcmToken);
		userRepository.save(user);
	}

	/**
	 * 특정 user의 사용되지 않는 fcm 토큰 삭제
	 * @param user
	 */
	public void cleanInvalidFcmTokens(User user) {
		Set<String> copy = new HashSet<>(user.getFcmTokens());
		for (String fcmToken : copy) {
			if (!redisUtils.existsFcmToken(fcmToken)) {
				user.removeFcmToken(fcmToken);
			} else {
				String refreshToken = redisUtils.getFcmTokenData(fcmToken);
				if (refreshToken == null || !redisUtils.existsRefreshToken(refreshToken)) {
					user.removeFcmToken(fcmToken);
					redisUtils.deleteFcmTokenData(fcmToken);
				}
			}
		}
		userRepository.save(user);
	}

	/**
	 * 특정 user의 fcm 토큰 전부 삭제
	 * @param user 유저
	 */
	public void clearFcmTokens(User user) {
		if (user.getFcmTokens() == null || user.getFcmTokens().isEmpty()) {
			return;
		}
		for (String token : user.getFcmTokens()) {
			redisUtils.deleteFcmTokenData(token);
		}
		user.getFcmTokens().clear();
		userRepository.save(user);
	}
}