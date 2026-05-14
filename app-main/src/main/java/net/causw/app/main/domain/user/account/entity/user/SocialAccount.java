package net.causw.app.main.domain.user.account.entity.user;

import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_social_account", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"social_type", "social_id"})
})
public class SocialAccount extends BaseEntity {
	@Column(nullable = false)
	private String socialId;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type", nullable = false)
	private SocialType socialType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "oauth_refresh_token_cipher", columnDefinition = "TEXT")
	private String oauthRefreshTokenCipher;

	public static SocialAccount of(SocialType socialType, String socialId, String email, User user) {
		return SocialAccount.builder()
			.socialId(socialId)
			.socialType(socialType)
			.email(email)
			.user(user)
			.build();
	}

	// 소셜 계정을 다른 유저에게 연결
	// GUEST 상태인 유저의 소셜 계정을 본 계정에 연동할 때 사용
	public void relink(User newUser) {
		this.user = newUser;
	}

	public void replaceEncryptedOauthRefreshToken(String cipherText) {
		this.oauthRefreshTokenCipher = cipherText;
	}
}
