package net.causw.app.main.domain.validation;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class LockerIsActiveValidator extends AbstractValidator {
	private final Boolean isActive;

	private LockerIsActiveValidator(Boolean isActive) {
		this.isActive = isActive;
	}

	public static LockerIsActiveValidator of(Boolean isActive) {
		return new LockerIsActiveValidator(isActive);
	}

	@Override
	public void validate() {
		if (this.isActive) {
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				"이미 사용 가능한 사물함입니다."
			);
		}
	}
}
