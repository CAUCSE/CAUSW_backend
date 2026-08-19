package net.causw.app.main.domain.community.report.service.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.util.ObjectFixtures;

class ReportValidatorSoftDeleteTest {

	@Test
	@DisplayName("게시판이 삭제되면 소속 게시글을 신고할 수 없다")
	void postReportShouldFail_whenBoardIsDeleted() {
		User writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		User reporter = ObjectFixtures.getCertifiedUserWithId("reporter-id");
		Board board = ObjectFixtures.getBoardV2WithId("board-id");
		Post post = ObjectFixtures.getPost(writer, board);
		board.setIsDeleted(true);

		assertThatThrownBy(() -> PostReportValidator.validateCreate(reporter, post, false))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(PostErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글이 삭제되면 미삭제 댓글도 신고할 수 없다")
	void commentReportShouldFail_whenPostIsDeleted() {
		User writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		User reporter = ObjectFixtures.getCertifiedUserWithId("reporter-id");
		Post post = ObjectFixtures.getPost(writer, ObjectFixtures.getBoardV2WithId("board-id"));
		Comment comment = Comment.ofRoot("댓글", false, writer, post);
		post.setIsDeleted(true);

		assertThatThrownBy(() -> CommentReportValidator.validateCreate(reporter, comment, false))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);
	}
}
