package net.causw.app.main.domain.community.report.service.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.ChildCommentErrorCode;
import net.causw.app.main.util.ObjectFixtures;

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
		given(rootComment.isChildComment()).willReturn(false);

		// when & then
		assertThatThrownBy(() -> ChildCommentReportValidator.validateCreate(reporter, rootComment, false))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND);
	}

	@Test
	@DisplayName("삭제된 루트 댓글 아래의 미삭제 답글은 신고할 수 있다")
	void validateCreate_shouldSucceed_whenParentCommentIsDeleted() {
		User writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		User reporter = ObjectFixtures.getCertifiedUserWithId("reporter-id");
		Post post = ObjectFixtures.getPost(writer, ObjectFixtures.getBoardV2WithId("board-id"));
		Comment rootComment = Comment.ofRoot("루트 댓글", false, writer, post);
		Comment childComment = Comment.ofChildComment("답글", false, writer, rootComment);
		rootComment.delete();

		assertThatCode(() -> ChildCommentReportValidator.validateCreate(reporter, childComment, false))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("상위 게시글이 삭제되면 미삭제 답글도 신고할 수 없다")
	void validateCreate_shouldFail_whenPostIsDeleted() {
		User writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		User reporter = ObjectFixtures.getCertifiedUserWithId("reporter-id");
		Post post = ObjectFixtures.getPost(writer, ObjectFixtures.getBoardV2WithId("board-id"));
		Comment rootComment = Comment.ofRoot("루트 댓글", false, writer, post);
		Comment childComment = Comment.ofChildComment("답글", false, writer, rootComment);
		post.setIsDeleted(true);

		assertThatThrownBy(() -> ChildCommentReportValidator.validateCreate(reporter, childComment, false))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(ChildCommentErrorCode.CHILD_COMMENT_NOT_FOUND);
	}
}
