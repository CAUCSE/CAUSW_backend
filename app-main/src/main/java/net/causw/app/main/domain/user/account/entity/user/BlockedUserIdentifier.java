package net.causw.app.main.domain.user.account.entity.user;

import net.causw.app.main.domain.user.account.enums.user.BlockedIdentifierType;
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

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "tb_blocked_user_identifier",
	indexes = {
		@Index(name = "idx_blocked_identifier_type_hash", columnList = "identifier_type, identifier_hash"),
		@Index(name = "idx_blocked_user_id", columnList = "user_id")
	}
)
public class BlockedUserIdentifier extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "identifier_type", nullable = false)
	private BlockedIdentifierType identifierType;

	@Column(name = "identifier_hash", nullable = false)
	private String identifierHash;

	@Column(name = "reason", nullable = true, length = 255)
	private String reason;

	public static BlockedUserIdentifier of(
		String userId,
		BlockedIdentifierType identifierType,
		String identifierHash,
		String reason
	) {
		return BlockedUserIdentifier.builder()
			.userId(userId)
			.identifierType(identifierType)
			.identifierHash(identifierHash)
			.reason(reason)
			.build();
	}
}
