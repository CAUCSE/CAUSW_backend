package net.causw.app.main.domain.user.auth.service;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationReader;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationSender;
import net.causw.app.main.domain.user.auth.service.implementation.EmailVerificationValidator;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 프로세스를 담당하는 Facade 서비스입니다.
 * <p>
 * 인증 코드 발송 및 인증 코드 검증 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private final EmailVerificationReader emailVerificationReader;
	private final EmailVerificationValidator emailVerificationValidator;
	private final EmailVerificationSender emailVerificationSender;

	/**
	 * 이메일로 인증 코드를 발송하고 DB에 인증 정보를 저장합니다.
	 * <p>
	 * 6자리 코드를 생성하여 메일로 전송하며, 만료 시간은 10분입니다.
	 *
	 * @param email 인증 코드를 받을 이메일 주소
	 */
	@Transactional
	public void sendVerificationEmail(String email) {
		emailVerificationValidator.validateSend(email);
		emailVerificationSender.send(email, VerificationStatus.PENDING);
	}

	/**
	 * 이메일과 인증 코드를 검증하고 인증 상태를 VERIFIED로 변경합니다.
	 *
	 * @param email            인증할 이메일 주소
	 * @param verificationCode 사용자가 입력한 인증 코드
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_VERIFICATION_NOT_FOUND] 해당 이메일의 PENDING 상태 인증 정보가 없는 경우,
	 * [EMAIL_VERIFICATION_EXPIRED] 인증 유효 시간이 만료된 경우,
	 * [EMAIL_VERIFICATION_CODE_MISMATCH] 인증 코드가 일치하지 않는 경우
	 */
	@Transactional
	public void verifyEmail(String email, String verificationCode) {
		EmailVerification emailVerification = emailVerificationReader.findLatestByEmailAndStatus(email,
			VerificationStatus.PENDING);

		if (emailVerification.isExpired()) {
			throw AuthErrorCode.EMAIL_VERIFICATION_EXPIRED.toBaseException();
		}

		if (!emailVerification.getVerificationCode().equals(verificationCode)) {
			throw AuthErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH.toBaseException();
		}

		emailVerification.verify();
	}
}
