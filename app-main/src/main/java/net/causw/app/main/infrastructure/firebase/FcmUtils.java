package net.causw.app.main.infrastructure.firebase;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.infrastructure.redis.RedisUtils;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class FcmUtils {
	private final RedisUtils redisUtils;
	private final UserRepository userRepository;

	/**
	 * 유효하지 않은 fcmToken 정리 및 최신화
	 * redis 구조
	 * Key : FcmToken:{FcmToken 값}
	 * Value: {RefreshToken 값}
	 *
	 * 1.   redis에 없는 토큰 -> DB에서 삭제
	 * 2.   redis에 있는 토큰인 경우
	 * 2.1. refreshToken이 redis에 저장되어있는지 확인
	 * 2.2. refreshToken이 redis에 없음 -> fcmToken을 DB, redis에서 모두 삭제
	 * */
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

	public void addFcmToken(User user, String refreshToken, String fcmToken) {
		if (!redisUtils.existsFcmToken(fcmToken)) {
			user.getFcmTokens().add(fcmToken);
			redisUtils.setFcmTokenData(fcmToken, refreshToken, StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
			userRepository.save(user);
		}
	}

	public void removeFcmToken(User user, String fcmToken) {
		user.removeFcmToken(fcmToken);
		redisUtils.deleteFcmTokenData(fcmToken);
		userRepository.save(user);
	}

}
























