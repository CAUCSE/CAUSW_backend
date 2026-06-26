package net.causw.app.main.domain.community.report.service.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;

class ChildCommentReportValidatorTest {

	@Test
	@DisplayName("대댓글 신고 대상이 루트 댓글이면 예외가 발생한다")
	void validateCreate_shouldFail_whenCommentIsRoot() {
		// given
		User reporter = mock(User.class);
		Comment rootComment = mock(Comment.class);

		given(reporter.getState()).willReturn(UserState.ACTIVE);
		given(reporter.isInactive()).willReturn(false);
		given(reporter.getRoles()).willReturn(Set.of(Role.COMMON));
		given(rootComment.isReply()).willReturn(false);

		// when & then
		assertThatThrownBy(() -> ChildCommentReportValidator.validateCreate(reporter, rootComment, false))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND);
	}
}
