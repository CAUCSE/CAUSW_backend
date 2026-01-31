package net.causw.app.main.domain.user.account.entity.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class TermAgreements {

	@Column(name = "service_agreed_at")
	private LocalDateTime serviceAgreedAt;

	@Column(name = "privacy_agreed_at")
	private LocalDateTime privacyAgreedAt;

	@Column(name = "third_party_agreed_at")
	private LocalDateTime thirdPartyAgreedAt;

	public TermAgreements(LocalDateTime now) {
		this.serviceAgreedAt = now;
		this.privacyAgreedAt = now;
		this.thirdPartyAgreedAt = now;
	}

	public static TermAgreements createRequiredAgreements() {
		return new TermAgreements(LocalDateTime.now());
	}
}
