package net.causw.app.main.domain.user.account.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.enums.user.DroppedIdentifierType;
import net.causw.app.main.domain.user.account.repository.user.DroppedUserIdentifierRepository;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DroppedUserIdentifierValidator {

	private final DroppedUserIdentifierRepository droppedUserIdentifierRepository;
	private final DroppedIdentifierHasher hasher;

	public void validateEmail(String email) {
		validate(DroppedIdentifierType.EMAIL, email);
	}

	public void validatePhone(String phoneNumber) {
		validate(DroppedIdentifierType.PHONE, phoneNumber);
	}

	public void validateStudentId(String studentId) {
		validate(DroppedIdentifierType.STUDENT_ID, studentId);
	}

	private void validate(DroppedIdentifierType type, String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return;
		}

		String hash = hasher.hash(rawValue);

		boolean blocked = droppedUserIdentifierRepository.existsByIdentifierTypeAndIdentifierHash(type, hash);

		if (blocked) {
			throw UserErrorCode.USER_DROPPED.toBaseException();
		}
	}
}
