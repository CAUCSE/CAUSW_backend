package net.causw.app.main.domain.user.auth.entity;

import java.time.LocalDateTime;

import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_email_verification", indexes = {
	@Index(name = "idx_email_verification_email", columnList = "email"),
})
public class EmailVerification extends BaseEntity {

	public enum VerificationStatus {
		PENDING,
		VERIFIED,
		PASSWORD_FIND
	}

	@Column(name = "email", nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private VerificationStatus status;

	@Column(name = "verification_code", nullable = false, length = 10)
	private String verificationCode;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	public static EmailVerification of(String email, String verificationCode, LocalDateTime expiresAt) {
		return of(email, verificationCode, expiresAt, VerificationStatus.PENDING);
	}

	public static EmailVerification of(
		String email,
		String verificationCode,
		LocalDateTime expiresAt,
		VerificationStatus status) {
		return EmailVerification.builder()
			.email(email)
			.verificationCode(verificationCode)
			.status(status)
			.expiresAt(expiresAt)
			.build();
	}

	public void verify() {
		this.status = VerificationStatus.VERIFIED;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}
}
