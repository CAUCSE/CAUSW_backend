package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.FcmToken;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.FcmTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 FCM(Firebase Cloud Messaging) 토큰의 쓰기 작업을 담당하는 컴포넌트입니다.
 * <p>
 * 토큰은 기기 단위로 {@code tb_fcm_token} 테이블에 저장됩니다.
 * 토큰 값에 UNIQUE 제약이 있으므로, 동일 기기 토큰이 다른 사용자에게 등록된 경우 먼저 제거합니다.
 * <p>
 * 토큰 무효화 처리 시점:
 * <ul>
 * <li>로그아웃 시 해당 기기 토큰 명시적 삭제</li>
 * <li>Firebase 푸시 발송 실패(UNREGISTERED 등) 시 자동 제거</li>
 * <li>회원 탈퇴 시 전체 토큰 삭제</li>
 * </ul>
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class UserPushTokenWriter {

	private final FcmTokenRepository fcmTokenRepository;

	/**
	 * 새로운 FCM 토큰을 DB에 저장합니다.
	 * <p>
	 * 동일 토큰이 다른 사용자에게 등록되어 있으면 먼저 제거한 뒤 현재 사용자에게 등록합니다.
	 * 이미 현재 사용자에게 등록된 토큰이라면 중복 저장하지 않습니다.
	 *
	 * @param user         토큰을 소유할 사용자 엔티티
	 * @param fcmToken     등록할 디바이스의 FCM 토큰
	 */
	public void addFcmToken(User user, String fcmToken) {
		Optional<FcmToken> existing = fcmTokenRepository.findByTokenValue(fcmToken);
		if (existing.isPresent()) {
			if (existing.get().getUser().getId().equals(user.getId())) {
				return;
			}
			fcmTokenRepository.delete(existing.get());
			fcmTokenRepository.flush();
		}
		fcmTokenRepository.save(FcmToken.of(user, fcmToken));
	}

	/**
	 * 특정 기기의 FCM 토큰을 삭제합니다.
	 * <p>
	 * 로그아웃 시 호출되며, DB에서 해당 토큰 데이터를 제거합니다.
	 *
	 * @param user     사용자 엔티티
	 * @param fcmToken 삭제할 FCM 토큰
	 */
	public void removeFcmToken(User user, String fcmToken) {
		if (!user.removeFcmToken(fcmToken)) {
			//			throw AuthErrorCode.NO_PERMISSION_FOR_RESOURCE.toBaseException();
			log.info("[FCM 토큰 삭제 실패] 이미 삭제된 FCM 토큰입니다.");
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
		user.clearFcmTokens();
	}
}
