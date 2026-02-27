package net.causw.app.main.domain.user.account.util;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.AbstractValidator;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

public class UserStateIsDeletedValidator extends AbstractValidator {

	private final User user;

	private UserStateIsDeletedValidator(User user) {
		this.user = user;
	}

	public static UserStateIsDeletedValidator of(User user) {
		return new UserStateIsDeletedValidator(user);
	}

	@Override
	public void validate() {
		if (this.user.getDeletedAt() != null) {
			throw new UnauthorizedException(
				ErrorCode.DELETED_USER,
				"삭제된 사용자 입니다.");
		}
	}
}
