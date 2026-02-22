package net.causw.app.main.domain.community.report.service.v2.util;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v2.util.UserStateValidator;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostReportErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

//	게시글 신고 생성 유효성 검증
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostReportValidator {
	public static void validateCreate(User reporter, Post post, boolean alreadyReported) {
		UserStateValidator.validateUserIsActiveWithValidRole(reporter);

		if (post.getIsDeleted()) {
			throw PostErrorCode.POST_NOT_FOUND.toBaseException();
		}

		if (post.getWriter().getId().equals(reporter.getId())) {
			throw PostReportErrorCode.POST_REPORT_SELF_NOT_ALLOWED.toBaseException();
		}

		if (alreadyReported) {
			throw PostReportErrorCode.POST_REPORT_ALREADY_REPORTED.toBaseException();
		}
	}
}
