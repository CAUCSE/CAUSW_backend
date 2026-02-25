package net.causw.app.main.domain.user.auth.service.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.domain.user.auth.repository.EmailVerificationRepository;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 관련 비즈니스 규칙을 검증하는 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationValidator {

	private static final int RESEND_INTERVAL_SECONDS = 30;

	private final EmailVerificationRepository emailVerificationRepository;
	private final UserReader userReader;

	/**
	 * 인증 메일 발송 전, 이메일 중복 여부와 재발송 간격(30초)을 함께 검증합니다.
	 *
	 * @param email 검사할 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_ALREADY_EXIST] 이미 가입된 이메일인 경우
	 * [EMAIL_VERIFICATION_SEND_TOO_SOON] 재발송 가능 시간이 지나지 않은 경우
	 */
	public void validateSend(String email) {
		if (userReader.existsByEmail(email)) {
			throw UserErrorCode.EMAIL_ALREADY_EXIST.toBaseException();
		}

		emailVerificationRepository.findLatestByEmail(email)
			.ifPresent(latest -> {
				LocalDateTime allowedAt = latest.getCreatedAt().plusSeconds(RESEND_INTERVAL_SECONDS);
				if (LocalDateTime.now().isBefore(allowedAt)) {
					throw AuthErrorCode.EMAIL_VERIFICATION_SEND_TOO_SOON.toBaseException();
				}
			});
	}

	/**
	 * 회원가입 시, 해당 이메일에 대해 VERIFIED 상태이며 만료되지 않은 인증 정보가 존재하는지 검증합니다.
	 *
	 * @param email 검사할 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_VERIFICATION_NOT_FOUND] VERIFIED 상태의 인증 정보가 없는 경우
	 * [EMAIL_VERIFICATION_EXPIRED] 인증 정보가 만료된 경우
	 */
	public void validateVerified(String email) {
		EmailVerification verification = emailVerificationRepository
			.findLatestByEmailAndStatus(email, VerificationStatus.VERIFIED)
			.orElseThrow(AuthErrorCode.EMAIL_VERIFICATION_NOT_FOUND::toBaseException);

		if (verification.isExpired()) {
			throw AuthErrorCode.EMAIL_VERIFICATION_EXPIRED.toBaseException();
		}
	}

}
