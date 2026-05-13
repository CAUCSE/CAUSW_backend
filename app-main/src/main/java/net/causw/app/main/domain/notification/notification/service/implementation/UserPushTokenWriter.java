package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 FCM(Firebase Cloud Messaging) 토큰의 쓰기 작업을 담당하는 컴포넌트입니다.
 * <p>
 * 토큰은 기기 단위로 DB(User 엔티티)에만 저장되며, 무효화는 다음 시점에만 처리됩니다.
 * <ul>
 * <li>로그아웃 시 해당 기기 토큰 명시적 삭제</li>
 * <li>Firebase 푸시 발송 실패(UNREGISTERED 등) 시 자동 제거</li>
 * <li>회원 탈퇴 시 전체 토큰 삭제</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class UserPushTokenWriter {

	/**
	 * 새로운 FCM 토큰을 기기 단위로 등록합니다.
	 * <p>
	 * 이미 등록된 토큰이라면 중복 저장하지 않습니다.
	 *
	 * @param user     토큰을 소유할 사용자 엔티티
	 * @param fcmToken 등록할 디바이스의 FCM 토큰
	 */
	public void addFcmToken(User user, String fcmToken) {
		user.getFcmTokens().add(fcmToken);
	}

	/**
	 * 특정 기기의 FCM 토큰을 삭제합니다.
	 * <p>
	 * 로그아웃 또는 Firebase 발송 실패 시 호출됩니다.
	 *
	 * @param user     사용자 엔티티
	 * @param fcmToken 삭제할 FCM 토큰
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [NO_PERMISSION_FOR_RESOURCE] 토큰이 해당 사용자에게 등록되지 않은 경우
	 */
	public void removeFcmToken(User user, String fcmToken) {
		boolean isRemoved = user.removeFcmToken(fcmToken);
		if (!isRemoved) {
			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
		}
	}

	/**
	 * 사용자의 모든 FCM 토큰을 삭제합니다.
	 * <p>
	 * 회원 탈퇴 등 사용자의 모든 디바이스 세션을 종료할 때 호출합니다.
	 *
	 * @param user 사용자 엔티티
	 */
	public void clearFcmTokens(User user) {
		if (user.getFcmTokens() == null || user.getFcmTokens().isEmpty()) {
			return;
		}
		user.getFcmTokens().clear();
	}
}
