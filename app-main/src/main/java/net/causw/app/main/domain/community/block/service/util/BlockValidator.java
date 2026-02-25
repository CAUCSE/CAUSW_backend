package net.causw.app.main.domain.community.block.service.util;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.BlockErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockValidator {

	public static void validateCreate(User blocker, User blocked, boolean alreadyBlocked, boolean isAnonymous) {
		validateUserState(blocker);

		// 본인 차단 방지
		if (blocker.getId().equals(blocked.getId())) {
			throw BlockErrorCode.BLOCK_SELF_NOT_ALLOWED.toBaseException();
		}

		// 익명 컨텐츠면 중복 차단 예외 없음 (익명성 보호)
		if (!isAnonymous && alreadyBlocked) {
			throw BlockErrorCode.BLOCK_ALREADY_BLOCKED.toBaseException();
		}
	}

	private static void validateUserState(User user) {
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
