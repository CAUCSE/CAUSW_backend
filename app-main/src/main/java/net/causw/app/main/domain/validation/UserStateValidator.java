package net.causw.app.main.domain.validation;

import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class UserStateValidator extends AbstractValidator {

	private final UserState userState;

	private UserStateValidator(UserState userState) {
		this.userState = userState;
	}

	public static UserStateValidator of(UserState userState) {
		return new UserStateValidator(userState);
	}

	@Override
	public void validate() {
		if (this.userState == UserState.DROP) {
			throw new UnauthorizedException(
				ErrorCode.BLOCKED_USER,
				MessageUtil.USER_DROPPED_CONTACT_EMAIL
			);
		}

		if (this.userState == UserState.INACTIVE) {
			throw new UnauthorizedException(
				ErrorCode.INACTIVE_USER,
				MessageUtil.USER_INACTIVE_CAN_REJOIN
			);
		}

		if (this.userState == UserState.DELETED) {
			throw new UnauthorizedException(
				ErrorCode.DELETED_USER,
				MessageUtil.USER_DELETED
			);
		}
	}
}
