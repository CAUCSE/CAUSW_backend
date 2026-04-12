package net.causw.app.main.domain.user.account.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.enums.user.BlockedIdentifierType;
import net.causw.app.main.domain.user.account.repository.user.BlockedUserIdentifierRepository;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlockedUserIdentifierValidator {

	private final BlockedUserIdentifierRepository blockedUserIdentifierRepository;
	private final BlockedIdentifierHasher hasher;

	public void validateEmail(String email) {
		validate(BlockedIdentifierType.EMAIL, email);
	}

	public void validatePhone(String phoneNumber) {
		validate(BlockedIdentifierType.PHONE, phoneNumber);
	}

	public void validateStudentId(String studentId) {
		validate(BlockedIdentifierType.STUDENT_ID, studentId);
	}

	private void validate(BlockedIdentifierType type, String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return;
		}

		String hash = hasher.hash(rawValue);

		boolean blocked = blockedUserIdentifierRepository.existsByIdentifierTypeAndIdentifierHash(type, hash);

		if (blocked) {
			throw UserErrorCode.USER_DROPPED.toBaseException();
		}
	}
}
