package net.causw.app.main.shared.infra.firebase;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * @deprecated V2 이후 신규 코드는 {@link UserPushTokenWriter}를 사용하세요. V1 및 배치 호환을 위해 유지됩니다.
 */
@Deprecated
@RequiredArgsConstructor
@Component
public class FcmUtils {
	private final UserRepository userRepository;

	/** @deprecated refreshToken 파라미터는 무시됩니다. 기기 단위 관리로 전환되어 세션 종속성이 제거되었습니다. */
	@Deprecated
	public void addFcmToken(User user, String refreshToken, String fcmToken) {
		user.getFcmTokens().add(fcmToken);
		userRepository.save(user);
	}

	public void removeFcmToken(User user, String fcmToken) {
		user.removeFcmToken(fcmToken);
		userRepository.save(user);
	}

	/** @deprecated 기기 단위 관리로 전환되어 no-op입니다. Firebase 발송 실패 시 자동 정리됩니다. */
	@Deprecated
	public void cleanInvalidFcmTokens(User user) {
		// no-op: 토큰 무효화는 Firebase 발송 실패 응답 기반으로 처리됩니다.
	}

	public void clearFcmTokens(User user) {
		if (user.getFcmTokens() == null || user.getFcmTokens().isEmpty()) {
			return;
		}
		user.getFcmTokens().clear();
		userRepository.save(user);
	}
}
