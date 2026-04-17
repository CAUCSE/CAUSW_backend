package net.causw.app.main.domain.user.auth.service.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
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

		validateResendInterval(email, VerificationStatus.PENDING);
	}

	/**
	 * 회원가입 시, 해당 이메일에 대해 VERIFIED 상태이며 만료되지 않은 인증 정보가 존재하는지 검증합니다.
	 *
	 * @param email 검사할 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_VERIFICATION_NOT_FOUND] VERIFIED 상태의 인증 정보가 없는 경우
	 * [EMAIL_VERIFICATION_EXPIRED] 인증 정보가 만료된 경우
	 */
	public void validateVerified(String email, String verificationCode) {
		EmailVerification verification = emailVerificationRepository
			.findVerifiedByEmailAndCode(email, verificationCode)
			.orElseThrow(AuthErrorCode.EMAIL_VERIFICATION_NOT_FOUND::toBaseException);

		if (verification.isExpired()) {
			throw AuthErrorCode.EMAIL_VERIFICATION_EXPIRED.toBaseException();
		}
	}

	/**
	 * 비밀번호 초기화 인증 메일 발송 전, 재발송 간격(30초)을 검증하고
	 * 이름+이메일에 해당하는 사용자가 존재하는지 검증합니다.
	 *
	 * @param name  사용자 이름
	 * @param email 이메일 주소
	 */
	public void validatePasswordResetSend(String name, String email) {
		validateResendInterval(email, VerificationStatus.PASSWORD_FIND);
		if (!userReader.existsByEmailAndName(email, name)) {
			throw UserErrorCode.USER_NOT_FOUND.toBaseException();
		}
	}

	/**
	 * V1 유저 온보딩 인증 메일 발송 전, 대상 유저가 V1 유저이면서 ACTIVE 상태인지 검증하고
	 * 재발송 간격(30초)을 검증합니다.
	 *
	 * @param email 검사할 이메일
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [USER_NOT_FOUND] 해당 이메일의 유저가 존재하지 않는 경우
	 * [INVALID_REGISTRATION_STATUS] V1 유저가 아니거나 ACTIVE 상태가 아닌 경우
	 * [EMAIL_VERIFICATION_SEND_TOO_SOON] 재발송 가능 시간이 지나지 않은 경우
	 */
	public void validateOnboardingSend(String email) {
		User user = userReader.findByEmail(email)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);

		if (user.checkEmailVerification()) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}

		if (user.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}

		validateResendInterval(email, VerificationStatus.V1_ONBOARDING_PENDING);
	}

	/**
	 * 특정 상태의 가장 최근 인증 정보를 기준으로 재발송 간격(30초)을 검증합니다.
	 *
	 * @param email  검사할 이메일
	 * @param status 검사할 인증 상태
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_VERIFICATION_SEND_TOO_SOON] 재발송 가능 시간이 지나지 않은 경우
	 */
	private void validateResendInterval(String email, VerificationStatus status) {
		emailVerificationRepository.findLatestByEmailAndStatus(email, status)
			.ifPresent(latest -> {
				LocalDateTime allowedAt = latest.getCreatedAt().plusSeconds(RESEND_INTERVAL_SECONDS);
				if (LocalDateTime.now().isBefore(allowedAt)) {
					throw AuthErrorCode.EMAIL_VERIFICATION_SEND_TOO_SOON.toBaseException();
				}
			});
	}

}
