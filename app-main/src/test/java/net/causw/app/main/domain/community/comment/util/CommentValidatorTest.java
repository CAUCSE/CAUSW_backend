package net.causw.app.main.domain.community.comment.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentValidator")
class CommentValidatorTest {

	@InjectMocks
	private CommentValidator commentValidator;

	@Mock
	private LikeCommentReader likeCommentReader;

	@Mock
	private User creator;

	@Mock
	private Post post;

	@Mock
	private BoardConfig boardConfig;

	@Test
	@DisplayName("시스템 공지에는 댓글을 작성할 수 없다")
	void validateForCreateRejectsCommentOnSystemNotice() {
		given(boardConfig.isSystemNotice()).willReturn(true);

		assertThatThrownBy(() -> commentValidator.validateForCreate(creator, post, boardConfig, List.of()))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(CommentErrorCode.COMMENT_NOT_ALLOWED_ON_SYSTEM_NOTICE);
	}

	@Test
	@DisplayName("게시판을 읽을 수 있으면 게시글 작성 권한 없이도 댓글을 작성할 수 있다")
	void validateForCreateAllowsCommentWhenUserCanReadButCannotWriteBoard() {
		String boardId = "board-id";
		User readableUser = ObjectFixtures.getCertifiedUserWithId("readable-user-id");
		var board = ObjectFixtures.getBoardV2WithId(boardId);
		Post readablePost = ObjectFixtures.getPost(readableUser, board);
		BoardConfig readOnlyBoardConfig = BoardConfig.of(
			boardId,
			false,
			BoardReadScope.BOTH,
			BoardWriteScope.ONLY_ADMIN,
			false,
			BoardVisibility.VISIBLE,
			10,
			null,
			null);

		assertThatCode(() -> commentValidator.validateForCreate(
			readableUser, readablePost, readOnlyBoardConfig, List.of()))
			.doesNotThrowAnyException();
	}
}
