package net.causw.app.main.domain.user.account.entity.user;

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
import net.causw.app.main.shared.entity.BaseEntity;

@Entity
@Table(
	name = "tb_fcm_token",
	uniqueConstraints = @UniqueConstraint(name = "uk_fcm_token_value", columnNames = "token_value")
)
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token_value", nullable = false)
	private String tokenValue;

	public static FcmToken of(User user, String tokenValue) {
		return FcmToken.builder()
			.user(user)
			.tokenValue(tokenValue)
			.build();
	}
}
