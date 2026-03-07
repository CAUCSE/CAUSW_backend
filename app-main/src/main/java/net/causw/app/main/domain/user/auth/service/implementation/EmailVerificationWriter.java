package net.causw.app.main.domain.user.auth.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.repository.EmailVerificationRepository;

import lombok.RequiredArgsConstructor;

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
}
