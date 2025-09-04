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
		// FIXME : 필요한 로직인지 정리 필요 (기존 값이 아니라 오늘 날짜보다 이후인지 체크하는게 맞지 않는지, 아니면 아예 필요 없는지)
		if (src.isAfter(dst)) {
			throw new BadRequestException(
				ErrorCode.INVALID_EXPIRE_DATE,
				MessageUtil.LOCKER_INVALID_EXPIRE_DATE
			);
		}
	}
}
