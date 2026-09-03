package net.causw.app.main.domain.community.comment.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
import net.causw.app.main.util.ObjectFixtures;

class CommentAuthorInfoTest {

	private User writer;
	private Post post;
	private BoardConfig boardConfig;

	@BeforeEach
	void setUp() {
		writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
		Board board = ObjectFixtures.getBoardV2WithId("board-id");
		post = ObjectFixtures.getPost(writer, board);
		boardConfig = BoardConfig.of(
			"board-id",
			false,
			BoardReadScope.BOTH,
			BoardWriteScope.ALL_USER,
			false,
			BoardVisibility.VISIBLE,
			10,
			null,
			null);
	}

	@Test
	@DisplayName("실제 익명 댓글은 이름·닉네임은 감추지만 입학연도는 노출한다")
	void givenAnonymousComment_whenOf_thenExposeAdmissionYearOnly() {
		// given
		Comment comment = Comment.ofRoot(
			"익명 댓글", true, "다정한 튜링 42", ProfileImageType.MALE_1, writer, post);

		// when
		CommentAuthorInfo authorInfo = CommentAuthorInfo.of(
			comment, null, true, writer, boardConfig, List.of(), false);

		// then
		assertThat(authorInfo.writerName()).isNull();
		assertThat(authorInfo.writerNickname()).isNull();
		assertThat(authorInfo.writerAdmissionYear()).isEqualTo(2000);
		assertThat(authorInfo.displayWriterNickname()).isEqualTo("다정한 튜링 42");
		assertThat(authorInfo.writerProfileImage().profileImageType()).isEqualTo(ProfileImageType.MALE_1);
	}

	@Test
	@DisplayName("삭제된 댓글은 tombstone 처리를 위해 익명으로 강제되지만 입학연도도 함께 감춘다")
	void givenDeletedComment_whenOf_thenHideAdmissionYearToo() {
		// given
		Comment comment = Comment.ofRoot("삭제될 댓글", false, null, null, writer, post);
		comment.delete();

		// when: CommentMapper가 삭제된 댓글에 대해 isAnonymous를 강제로 true로 넘기는 것과 동일하게 호출
		CommentAuthorInfo authorInfo = CommentAuthorInfo.of(
			comment, null, true, writer, boardConfig, List.of(), false);

		// then
		assertThat(authorInfo.writerName()).isNull();
		assertThat(authorInfo.writerNickname()).isNull();
		assertThat(authorInfo.writerAdmissionYear()).isNull();
	}
}
