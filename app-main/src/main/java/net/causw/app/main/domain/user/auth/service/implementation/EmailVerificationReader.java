package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.domain.user.auth.repository.EmailVerificationRepository;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 정보를 조회하는 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationReader {

	private final EmailVerificationRepository emailVerificationRepository;

	/**
	 * 주어진 이메일과 상태에 해당하는 가장 최근 이메일 인증 정보를 조회합니다.
	 *
	 * @param email  조회할 이메일
	 * @param status 조회할 인증 상태
	 * @return 조회된 EmailVerification 엔티티
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception
	 * [EMAIL_VERIFICATION_NOT_FOUND] 해당 이메일과 상태에 대한 인증 정보가 없는 경우
	 */
	public EmailVerification findLatestByEmailAndStatus(String email, VerificationStatus status) {
		return emailVerificationRepository.findLatestByEmailAndStatus(email, status)
			.orElseThrow(AuthErrorCode.EMAIL_VERIFICATION_NOT_FOUND::toBaseException);
	}
}
