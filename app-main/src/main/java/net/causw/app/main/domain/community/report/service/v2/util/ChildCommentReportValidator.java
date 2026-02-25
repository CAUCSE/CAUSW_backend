package net.causw.app.main.domain.community.report.service.v2.util;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;
import net.causw.app.main.shared.exception.errorcode.ChildCommentReportErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildCommentReportValidator {

	public static void validateCreate(User reporter, ChildComment childComment, boolean alreadyReported) {
		validateUserState(reporter);

		if (childComment.getIsDeleted()) {
			throw ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND.toBaseException();
		}

		if (childComment.getWriter().getId().equals(reporter.getId())) {
			throw ChildCommentReportErrorCode.CHILD_COMMENT_REPORT_SELF_NOT_ALLOWED.toBaseException();
		}

		if (alreadyReported) {
			throw ChildCommentReportErrorCode.CHILD_COMMENT_REPORT_ALREADY_REPORTED.toBaseException();
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
