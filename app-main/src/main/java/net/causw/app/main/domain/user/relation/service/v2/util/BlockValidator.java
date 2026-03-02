package net.causw.app.main.domain.user.relation.service.v2.util;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.BlockErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockValidator {

	public static void validateCreate(User blocker, User blocked, boolean alreadyBlocked, boolean isAnonymous) {
		UserStateValidator.validateUserIsActiveWithValidRole(blocker);

		// 본인 차단 방지
		if (blocker.getId().equals(blocked.getId())) {
			throw BlockErrorCode.BLOCK_SELF_NOT_ALLOWED.toBaseException();
		}

		// 익명 게시글이면 중복 차단 예외 없음 (익명성 보호)
		if (!isAnonymous && alreadyBlocked) {
			throw BlockErrorCode.BLOCK_ALREADY_BLOCKED.toBaseException();
		}
	}
}
