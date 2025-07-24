package net.causw.app.main.domain.validation;

import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class UserStateIsDropOrIsInActiveValidator extends AbstractValidator {
	private final UserState userState;

	private UserStateIsDropOrIsInActiveValidator(UserState userState) {
		this.userState = userState;
	}

	public static UserStateIsDropOrIsInActiveValidator of(UserState userState) {
		return new UserStateIsDropOrIsInActiveValidator(userState);
	}

	@Override
	public void validate() {
		if (!(this.userState.equals(UserState.REJECT) || this.userState.equals(UserState.DROP) || this.userState.equals(
			UserState.INACTIVE) || this.userState.equals(UserState.DELETED))) {
			throw new UnauthorizedException(
				ErrorCode.BLOCKED_USER,
				"등록된 사용자가 아닙니다."
			);
		}
	}
}
