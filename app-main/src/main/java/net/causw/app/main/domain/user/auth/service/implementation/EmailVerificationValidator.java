package net.causw.app.main.domain.user.auth.service.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.auth.repository.EmailVerificationRepository;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 관련 비즈니스 규칙을 검증하는 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationValidator {

	private static final int RESEND_INTERVAL_SECONDS = 30;

	private final EmailVerificationRepository emailVerificationRepository;

	/**
	 * 가장 최근 인증 요청으로부터 30초가 지나지 않은 경우 예외를 발생시킵니다.
	 *
	 * @param email 검사할 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_VERIFICATION_SEND_TOO_SOON] 재발송 가능 시간이 지나지 않은 경우
	 */
	public void validateResendInterval(String email) {
		emailVerificationRepository.findLatestByEmail(email)
			.ifPresent(latest -> {
				LocalDateTime allowedAt = latest.getCreatedAt().plusSeconds(RESEND_INTERVAL_SECONDS);
				if (LocalDateTime.now().isBefore(allowedAt)) {
					throw AuthErrorCode.EMAIL_VERIFICATION_SEND_TOO_SOON.toBaseException();
				}
			});
	}
}
