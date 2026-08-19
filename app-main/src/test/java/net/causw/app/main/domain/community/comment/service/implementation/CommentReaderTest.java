package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentQueryRepository;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class CommentReaderTest {

	@InjectMocks
	private CommentReader commentReader;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private CommentQueryRepository commentQueryRepository;

	@Nested
	@DisplayName("댓글 단건 조회")
	class GetComment {

		@Test
		@DisplayName("레거시 null 삭제 상태는 미삭제 댓글로 조회한다")
		void givenNullDeletedState_whenGetComment_thenReturnComment() {
			Comment comment = Comment.of("댓글", null, false, ObjectFixtures.getUser(),
				ObjectFixtures.getPost(ObjectFixtures.getUser(), ObjectFixtures.getBoard()));
			given(commentRepository.findById("comment-id")).willReturn(Optional.of(comment));

			assertThat(commentReader.getComment("comment-id")).isSameAs(comment);
		}

		@Test
		@DisplayName("삭제된 댓글은 찾을 수 없다")
		void givenDeletedComment_whenGetComment_thenThrowNotFound() {
			Comment comment = Comment.of("댓글", true, false, ObjectFixtures.getUser(),
				ObjectFixtures.getPost(ObjectFixtures.getUser(), ObjectFixtures.getBoard()));
			given(commentRepository.findById("comment-id")).willReturn(Optional.of(comment));

			assertThatThrownBy(() -> commentReader.getComment("comment-id"))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CommentErrorCode.COMMENT_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("답글 부모 조회")
	class GetReplyParent {

		@Test
		@DisplayName("삭제된 댓글도 답글 부모로 조회한다")
		void givenDeletedComment_whenGetReplyParent_thenReturnComment() {
			Comment comment = Comment.of("댓글", true, false, ObjectFixtures.getUser(),
				ObjectFixtures.getPost(ObjectFixtures.getUser(), ObjectFixtures.getBoard()));
			given(commentRepository.findById("comment-id")).willReturn(Optional.of(comment));

			assertThat(commentReader.getReplyParent("comment-id")).isSameAs(comment);
		}
	}

	@Nested
	@DisplayName("댓글 목록 조회")
	class GetComments {

		@Test
		@DisplayName("루트 댓글만 페이지 조회하고 답글을 부모 댓글에 붙인다")
		void givenPostId_whenGetComments_thenAttachChildCommentsToRootComments() {
			// given
			User writer = ObjectFixtures.getUser();
			Board board = ObjectFixtures.getBoard();
			Post post = ObjectFixtures.getPost(writer, board);
			Comment root = Comment.ofRoot("루트 댓글", false, null, writer, post);
			ReflectionTestUtils.setField(root, "id", "root-comment-id");
			Comment childComment = Comment.ofChildComment("답글", false, null, writer, root);
			ReflectionTestUtils.setField(childComment, "id", "child-comment-id");
			Pageable pageable = PageRequest.of(0, 10);

			given(commentQueryRepository.findRootCommentsByPostId("post-id", pageable))
				.willReturn(new PageImpl<>(List.of(root), pageable, 1));
			given(commentQueryRepository.findChildCommentsByParentCommentIds(List.of("root-comment-id")))
				.willReturn(List.of(childComment));

			// when
			Page<Comment> result = commentReader.getComments("post-id", pageable);

			// then
			assertThat(result.getContent()).containsExactly(root);
			assertThat(result.getContent().get(0).getChildCommentList()).containsExactly(childComment);
			then(commentQueryRepository).should().findRootCommentsByPostId("post-id", pageable);
			then(commentQueryRepository).should().findChildCommentsByParentCommentIds(List.of("root-comment-id"));
		}
	}
}
