package net.causw.app.main.domain.user.auth.service.implementation;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;
import net.causw.app.main.shared.infra.mail.event.EmailVerificationEvent;
import net.causw.app.main.shared.infra.mail.event.PasswordResetCodeEvent;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 코드 생성 및 발송을 담당하는 컴포넌트입니다.
 * <p>
 * 인증 코드 생성, DB 저장, 이메일 이벤트 발행 책임을 갖습니다.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationSender {

	private static final int CODE_LENGTH = 6;
	private static final int EXPIRATION_MINUTES = 10;
	private static final String CODE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final EmailVerificationWriter emailVerificationWriter;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 지정된 상태로 인증 코드를 생성하여 DB에 저장하고, 이메일로 발송합니다.
	 * <p>
	 * 동일 이메일과 상태의 기존 인증 정보가 있으면 삭제 후 새로 생성합니다.
	 *
	 * @param email  인증 코드를 받을 이메일 주소
	 * @param status 저장할 인증 상태 ({@link VerificationStatus})
	 */
	public void send(String email, VerificationStatus status) {
		emailVerificationWriter.deleteAllByEmailAndStatus(email, status);
		if (status == VerificationStatus.PENDING) {
			emailVerificationWriter.deleteAllByEmailAndStatus(email, VerificationStatus.VERIFIED);
		}

		String verificationCode = generateVerificationCode();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

		EmailVerification emailVerification = EmailVerification.of(email, verificationCode, expiresAt, status);
		emailVerificationWriter.save(emailVerification);

		if (status == VerificationStatus.PASSWORD_FIND) {
			eventPublisher.publishEvent(new PasswordResetCodeEvent(email, verificationCode));
		} else {
			eventPublisher.publishEvent(new EmailVerificationEvent(email, verificationCode));
		}
	}

	private String generateVerificationCode() {
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder(CODE_LENGTH);
		for (int i = 0; i < CODE_LENGTH; i++) {
			code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
		}
		return code.toString();
	}
}
