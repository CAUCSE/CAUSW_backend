package net.causw.app.main.domain.user.relation.service.v2.util;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserStateValidator {

	/**
	 * 사용자가 서비스 내 활동(신고, 차단 등)을 수행할 수 있는 상태인지 검증.
	 * DROP / INACTIVE / DELETED 상태이거나 NONE 역할인 경우 예외를 발생시킨다.
	 */
	public static void validateUserIsActiveWithValidRole(User user) {
		UserState state = user.getState();
		if (state == UserState.DROP) {
			throw UserErrorCode.USER_DROPPED.toBaseException();
		}
		if (state == UserState.INACTIVE) {
			throw UserErrorCode.USER_INACTIVE_CAN_REJOIN.toBaseException();
		}
		if (state == UserState.DELETED) {
			throw UserErrorCode.USER_DELETED.toBaseException();
		}
		if (user.getRoles().contains(Role.NONE)) {
			throw AuthErrorCode.USER_ROLE_NONE.toBaseException();
		}
	}
}
