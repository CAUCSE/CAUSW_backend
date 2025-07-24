package net.causw.app.main.domain.validation;

import java.time.LocalDateTime;

import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public class LockerExpiredAtValidator extends AbstractValidator {
	private final LocalDateTime src;
	private final LocalDateTime dst;

	private LockerExpiredAtValidator(
		LocalDateTime src,
		LocalDateTime dst
	) {
		this.src = src;
		this.dst = dst;
	}

	public static LockerExpiredAtValidator of(
		LocalDateTime src,
		LocalDateTime dst
	) {
		return new LockerExpiredAtValidator(src, dst);
	}

	@Override
	public void validate() {
		if (src.isAfter(dst)) {
			throw new BadRequestException(
				ErrorCode.INVALID_EXPIRE_DATE,
				MessageUtil.LOCKER_INVALID_EXPIRE_DATE
			);
		}
	}
}
