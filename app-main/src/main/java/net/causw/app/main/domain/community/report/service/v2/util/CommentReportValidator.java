package net.causw.app.main.domain.community.report.service.v2.util;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v2.util.UserStateValidator;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;
import net.causw.app.main.shared.exception.errorcode.CommentReportErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentReportValidator {

	public static void validateCreate(User reporter, Comment comment, boolean alreadyReported) {
		validateUserState(reporter);

		if (comment.getIsDeleted()) {
			throw CommentErrorCode.COMMENT_NOT_FOUND.toBaseException();
		}

		if (comment.getWriter().getId().equals(reporter.getId())) {
			throw CommentReportErrorCode.COMMENT_REPORT_SELF_NOT_ALLOWED.toBaseException();
		}

		if (alreadyReported) {
			throw CommentReportErrorCode.COMMENT_REPORT_ALREADY_REPORTED.toBaseException();
		}
	}

	private static void validateUserState(User user) {
		UserStateValidator.validateUserIsActiveWithValidRole(user);
	}
}
