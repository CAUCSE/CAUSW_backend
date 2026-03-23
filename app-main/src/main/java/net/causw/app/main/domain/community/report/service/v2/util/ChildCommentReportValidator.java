package net.causw.app.main.domain.community.report.service.v2.util;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v2.util.UserStateValidator;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;
import net.causw.app.main.shared.exception.errorcode.ChildCommentReportErrorCode;

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
		UserStateValidator.validateUserIsActiveWithValidRole(user);
	}
}
