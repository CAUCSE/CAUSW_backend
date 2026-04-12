package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.BlockedUserIdentifier;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.BlockedIdentifierType;
import net.causw.app.main.domain.user.account.repository.user.BlockedUserIdentifierRepository;
import net.causw.app.main.domain.user.account.util.BlockedIdentifierHasher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlockedUserIdentifierWriter {

	private final BlockedUserIdentifierRepository blockedUserIdentifierRepository;
	private final BlockedIdentifierHasher hasher;

	@Transactional
	public void saveBlockedIdentifiers(User user) {

		save(user.getId(), BlockedIdentifierType.EMAIL, user.getEmail(), user.getRejectionOrDropReason());
		save(user.getId(), BlockedIdentifierType.PHONE, user.getPhoneNumber(), user.getRejectionOrDropReason());
		save(user.getId(), BlockedIdentifierType.STUDENT_ID, user.getStudentId(), user.getRejectionOrDropReason());
	}

	private void save(String userId, BlockedIdentifierType type, String raw, String reason) {
		if (raw == null || raw.isBlank()) {
			return;
		}

		String hash = hasher.hash(raw);

		if (blockedUserIdentifierRepository.existsByIdentifierTypeAndIdentifierHash(type, hash)) {
			return;
		}

		blockedUserIdentifierRepository.save(
			BlockedUserIdentifier.of(userId, type, hash, reason)
		);
	}
}
