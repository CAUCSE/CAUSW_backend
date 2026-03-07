package net.causw.app.main.domain.user.account.util;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.AbstractValidator;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class UserStateValidator extends AbstractValidator {

	private final User user;
	private final UserState userState;

	private UserStateValidator(User user) {
		this.user = user;
		this.userState = user.getState();
	}

	public static UserStateValidator of(User user) {
		return new UserStateValidator(user);
	}

	@Override
	public void validate() {
		if (this.user.isDeleted()) {
			throw new UnauthorizedException(
				ErrorCode.INACTIVE_USER,
				MessageUtil.USER_INACTIVE_CAN_REJOIN);
		}

		if (this.userState == UserState.DROP) {
			throw new UnauthorizedException(
				ErrorCode.BLOCKED_USER,
				MessageUtil.USER_DROPPED_CONTACT_EMAIL);
		}

	}
}
