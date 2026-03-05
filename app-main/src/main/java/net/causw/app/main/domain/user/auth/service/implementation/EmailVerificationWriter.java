package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.domain.user.auth.repository.EmailVerificationRepository;

/**
 * 이메일 인증 정보를 저장하는 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationWriter {

	private final EmailVerificationRepository emailVerificationRepository;

	/**
	 * 이메일 인증 정보를 저장합니다.
	 *
	 * @param emailVerification 저장할 EmailVerification 엔티티
	 * @return 저장된 EmailVerification 엔티티
	 */
	public EmailVerification save(EmailVerification emailVerification) {
		return emailVerificationRepository.save(emailVerification);
	}

	/**
	 * 이메일 인증 정보를 삭제합니다.
	 *
	 * @param emailVerification 삭제할 EmailVerification 엔티티
	 */
	public void delete(EmailVerification emailVerification) {
		emailVerificationRepository.delete(emailVerification);
	}

	/**
	 * 특정 이메일과 상태에 해당하는 모든 인증 정보를 삭제합니다.
	 *
	 * @param email  삭제할 이메일
	 * @param status 삭제할 인증 상태
	 */
	public void deleteAllByEmailAndStatus(String email, VerificationStatus status) {
		emailVerificationRepository.deleteAllByEmailAndStatus(email, status);
	}
}
