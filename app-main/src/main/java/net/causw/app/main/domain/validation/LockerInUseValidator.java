package net.causw.app.main.domain.validation;

import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class LockerInUseValidator extends AbstractValidator {
	private final Boolean isInUse;

	private LockerInUseValidator(Boolean isInUse) {
		this.isInUse = isInUse;
	}

	public static LockerInUseValidator of(Boolean isInUse) {
		return new LockerInUseValidator(isInUse);
	}

	@Override
	public void validate() {
		if (isInUse) {
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.LOCKER_USED
			);
		}
	}
}
