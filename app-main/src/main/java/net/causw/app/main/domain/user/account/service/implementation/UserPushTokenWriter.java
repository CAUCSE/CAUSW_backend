package net.causw.app.main.domain.user.account.service.implementation;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 FCM(Firebase Cloud Messaging) 토큰의 쓰기 작업을 담당하는 컴포넌트입니다.
 * <p>
 * DB(User 엔티티)와 Redis 간의 토큰 동기화를 관리하며,
 * 로그인 시 토큰 등록, 로그아웃 시 삭제, 만료된 토큰 정리(Cleanup) 기능을 수행합니다.
 */
@Component
@RequiredArgsConstructor
public class UserPushTokenWriter {

	private final RedisUtils redisUtils;

	/**
	 * 새로운 FCM 토큰을 등록하고 Redis에 메타데이터를 저장합니다.
	 * <p>
	 * 이미 Redis에 등록된 토큰이라면 중복 저장을 방지합니다.<br>
	 * 신규 토큰인 경우 User 엔티티에 추가하고, Redis에 <b>[FCM Token -> Refresh Token]</b> 매핑 정보를 저장하여
	 * 리프레시 토큰의 수명과 생명주기를 같이하도록 설정합니다.
	 *
	 * @param user         토큰을 소유할 사용자 엔티티
	 * @param refreshToken 현재 로그인 세션의 리프레시 토큰 (만료 기준점)
	 * @param fcmToken     등록할 디바이스의 FCM 토큰
	 */
	// TODO: 생명주기 수정 예정
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
	 * 특정 기기의 FCM 토큰을 삭제합니다.
	 * <p>
	 * 로그아웃 시 호출되며, DB와 Redis 양쪽에서 해당 토큰 데이터를 제거합니다.
	 *
	 * @param user     사용자 엔티티
	 * @param fcmToken 삭제할 FCM 토큰
	 */
	public void removeFcmToken(User user, String fcmToken) {
		boolean isRemoved = user.removeFcmToken(fcmToken);
		if (!isRemoved) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
		redisUtils.deleteFcmTokenData(fcmToken);
	}

	/**
	 * 사용자가 보유한 FCM 토큰 중 유효하지 않은(만료된) 토큰을 식별하여 정리합니다.
	 * <p>
	 * Redis의 데이터는 TTL에 의해 자동 만료되지만, DB의 데이터는 남아있을 수 있습니다.<br>
	 * 이를 동기화하기 위해 다음 두 가지 경우에 해당하는 토큰을 DB에서 제거합니다.
	 * <ul>
	 * <li>1. Redis에 FCM 토큰 데이터 자체가 없는 경우 (이미 만료됨)</li>
	 * <li>2. Redis에 FCM 토큰은 있으나, 매핑된 리프레시 토큰이 만료/삭제된 경우 (세션 종료)</li>
	 * </ul>
	 *
	 * @param user 검사할 사용자 엔티티
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
