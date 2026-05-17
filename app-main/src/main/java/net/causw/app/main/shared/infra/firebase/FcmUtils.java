package net.causw.app.main.shared.infra.firebase;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.FcmToken;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.user.account.repository.user.FcmTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * @deprecated V2 이후 신규 코드는 {@link net.causw.app.main.domain.notification.notification.service.implementation.UserPushTokenWriter}를 사용하세요.
 * V1 및 배치 호환을 위해 유지됩니다.
 */
@Deprecated
@RequiredArgsConstructor
@Component
public class FcmUtils {
	private final FcmTokenRepository fcmTokenRepository;

	@Deprecated
	public void addFcmToken(User user, String fcmToken) {
		fcmTokenRepository.findByTokenValue(fcmToken).ifPresent(existing -> {
			if (!existing.getUser().getId().equals(user.getId())) {
				fcmTokenRepository.delete(existing);
				fcmTokenRepository.flush();
			}
		});
		if (fcmTokenRepository.findByTokenValue(fcmToken).isEmpty()) {
			fcmTokenRepository.save(FcmToken.of(user, fcmToken));
		}
	}

	public void removeFcmToken(User user, String fcmToken) {
		fcmTokenRepository.findByTokenValue(fcmToken)
			.filter(t -> t.getUser().getId().equals(user.getId()))
			.ifPresent(fcmTokenRepository::delete);
	}

	public void clearFcmTokens(User user) {
		fcmTokenRepository.deleteAllByUser(user);
	}
}
