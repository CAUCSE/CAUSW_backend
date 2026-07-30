package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
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

	@Test
	@DisplayName("삭제된 루트 댓글은 tombstone으로 반환하고 미삭제 답글은 그대로 유지한다")
	void deletedRootCommentIsMappedAsTombstoneWithAliveChildComment() {
		Comment rootComment = Comment.ofRoot("삭제될 댓글", false, viewer, post);
		ReflectionTestUtils.setField(rootComment, "id", "root-comment-id");
		Comment childComment = Comment.ofChildComment("남아 있는 답글", false, viewer, rootComment);
		ReflectionTestUtils.setField(childComment, "id", "child-comment-id");
		rootComment.setChildCommentList(List.of(childComment));
		rootComment.delete();
		CommentMapper mapper = new CommentMapper(new ChildCommentMapper());
		CommentMeta meta = new CommentMeta(
			3L,
			true,
			false,
			Map.of("child-comment-id", 2L),
			Set.of("child-comment-id"),
			Set.of());

		CommentResult result = mapper.toResult(
			rootComment, viewer, boardConfig, List.of(), meta, Map.of());

		assertThat(result.isDeleted()).isTrue();
		assertThat(result.content()).isNull();
		assertThat(result.isCommentLike()).isTrue();
		assertThat(result.numLike()).isEqualTo(3L);
		assertThat(result.numChildComment()).isEqualTo(1L);
		assertThat(result.authorInfo().writerName()).isNull();
		assertThat(result.authorInfo().writerNickname()).isNull();
		assertThat(result.authorInfo().writerAdmissionYear()).isNull();
		assertThat(result.authorInfo().writerProfileImage().profileImageType()).isEqualTo(ProfileImageType.GHOST);
		assertThat(result.authorInfo().updatable()).isFalse();
		assertThat(result.authorInfo().deletable()).isFalse();
		assertThat(result.childCommentList()).singleElement().satisfies(childResult -> {
			assertThat(childResult.isDeleted()).isFalse();
			assertThat(childResult.content()).isEqualTo("남아 있는 답글");
			assertThat(childResult.isCommentLike()).isTrue();
			assertThat(childResult.numLike()).isEqualTo(2L);
		});
	}

	@Test
	@DisplayName("삭제된 답글은 tombstone으로 반환한다")
	void deletedChildCommentIsMappedAsTombstone() {
		Comment rootComment = Comment.ofRoot("루트 댓글", false, viewer, post);
		Comment childComment = Comment.ofChildComment("삭제될 답글", false, viewer, rootComment);
		childComment.delete();
		ChildCommentMapper mapper = new ChildCommentMapper();

		CommentResult result = mapper.toResult(
			childComment,
			viewer,
			boardConfig,
			new ChildCommentMeta(List.of(), 4L, true, false),
			Map.of());

		assertThat(result.isDeleted()).isTrue();
		assertThat(result.content()).isNull();
		assertThat(result.isCommentLike()).isTrue();
		assertThat(result.numLike()).isEqualTo(4L);
		assertThat(result.authorInfo().writerProfileImage().profileImageType()).isEqualTo(ProfileImageType.GHOST);
		assertThat(result.authorInfo().updatable()).isFalse();
		assertThat(result.authorInfo().deletable()).isFalse();
	}
}
