package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

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
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class CommentReaderTest {

	@InjectMocks
	private CommentReader commentReader;

	@Mock
	private CommentRepository commentRepository;

	@Nested
	@DisplayName("댓글 목록 조회")
	class GetComments {

		@Test
		@DisplayName("루트 댓글만 페이지 조회하고 답글을 부모 댓글에 붙인다")
		void givenPostId_whenGetComments_thenAttachRepliesToRootComments() {
			// given
			User writer = ObjectFixtures.getUser();
			Board board = ObjectFixtures.getBoard();
			Post post = ObjectFixtures.getPost(writer, board);
			Comment root = Comment.ofRoot("루트 댓글", false, writer, post);
			ReflectionTestUtils.setField(root, "id", "root-comment-id");
			Comment reply = Comment.ofReply("답글", false, writer, root);
			ReflectionTestUtils.setField(reply, "id", "reply-comment-id");
			Pageable pageable = PageRequest.of(0, 10);

			given(commentRepository.findRootCommentsByPostId("post-id", pageable))
				.willReturn(new PageImpl<>(List.of(root), pageable, 1));
			given(commentRepository.findRepliesByParentCommentIds(List.of("root-comment-id")))
				.willReturn(List.of(reply));

			// when
			Page<Comment> result = commentReader.getComments("post-id", pageable);

			// then
			assertThat(result.getContent()).containsExactly(root);
			assertThat(result.getContent().get(0).getChildCommentList()).containsExactly(reply);
			then(commentRepository).should().findRootCommentsByPostId("post-id", pageable);
			then(commentRepository).should().findRepliesByParentCommentIds(List.of("root-comment-id"));
		}
	}
}
