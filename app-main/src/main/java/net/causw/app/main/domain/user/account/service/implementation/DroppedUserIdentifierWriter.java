package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.DroppedUserIdentifier;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.DroppedIdentifierType;
import net.causw.app.main.domain.user.account.repository.user.DroppedUserIdentifierRepository;
import net.causw.app.main.domain.user.account.util.DroppedIdentifierHasher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DroppedUserIdentifierWriter {

	private final DroppedUserIdentifierRepository droppedUserIdentifierRepository;
	private final DroppedIdentifierHasher hasher;

	@Transactional
	public void saveDroppedIdentifiers(User user) {

		save(user.getId(), DroppedIdentifierType.EMAIL, user.getEmail(), user.getRejectionOrDropReason());
		save(user.getId(), DroppedIdentifierType.PHONE, user.getPhoneNumber(), user.getRejectionOrDropReason());
		save(user.getId(), DroppedIdentifierType.STUDENT_ID, user.getStudentId(), user.getRejectionOrDropReason());
	}

	private void save(String userId, DroppedIdentifierType type, String raw, String reason) {
		if (raw == null || raw.isBlank()) {
			return;
		}

		String hash = hasher.hash(raw);

		if (droppedUserIdentifierRepository.existsByIdentifierTypeAndIdentifierHash(type, hash)) {
			return;
		}

		droppedUserIdentifierRepository.save(
			DroppedUserIdentifier.of(userId, type, hash, reason));
	}
}
