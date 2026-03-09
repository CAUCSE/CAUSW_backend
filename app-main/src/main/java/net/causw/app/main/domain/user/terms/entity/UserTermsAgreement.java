package net.causw.app.main.domain.user.terms.entity;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_terms_agreement",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "terms_id"}))
public class UserTermsAgreement extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "terms_id", nullable = false)
	private Terms terms;

	@Column(name = "agreed_at", nullable = false)
	private LocalDateTime agreedAt;

	public static UserTermsAgreement of(User user, Terms terms) {
		return UserTermsAgreement.builder()
			.user(user)
			.terms(terms)
			.agreedAt(LocalDateTime.now())
			.build();
	}
}
