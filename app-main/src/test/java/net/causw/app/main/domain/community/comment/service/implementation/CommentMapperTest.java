package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentMeta;
import net.causw.app.main.domain.community.comment.service.dto.CommentMeta;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

class CommentMapperTest {

	private User viewer;
	private Post post;
	private BoardConfig boardConfig;

	@BeforeEach
	void setUp() {
		viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
		Board board = ObjectFixtures.getBoardV2WithId("board-id");
		post = ObjectFixtures.getPost(viewer, board);
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
	@DisplayName("레거시 null 삭제 상태 댓글을 미삭제 응답으로 정규화한다")
	void nullDeletedRootCommentIsMappedAsAlive() {
		Comment comment = Comment.of("댓글", null, false, viewer, post);
		CommentMapper mapper = new CommentMapper(mock(ChildCommentMapper.class));

		CommentResult result = mapper.toResult(
			comment, viewer, boardConfig, List.of(), CommentMeta.forNew(), Map.of());

		assertThat(result.isDeleted()).isFalse();
		assertThat(result.content()).isEqualTo("댓글");
	}

	@Test
	@DisplayName("레거시 null 삭제 상태 대댓글을 미삭제 응답으로 정규화한다")
	void nullDeletedChildCommentIsMappedAsAlive() {
		Comment comment = Comment.of("대댓글", null, false, viewer, post);
		ChildCommentMapper mapper = new ChildCommentMapper();

		CommentResult result = mapper.toResult(
			comment,
			viewer,
			boardConfig,
			new ChildCommentMeta(List.of(), 0L, false, false),
			Map.of());

		assertThat(result.isDeleted()).isFalse();
		assertThat(result.content()).isEqualTo("대댓글");
	}
}
