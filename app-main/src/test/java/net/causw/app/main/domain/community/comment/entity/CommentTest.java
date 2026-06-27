package net.causw.app.main.domain.community.comment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

class CommentTest {

	@Nested
	@DisplayName("루트 댓글 생성")
	class CreateRoot {

		@Test
		@DisplayName("부모 댓글 없이 게시글에 연결된 루트 댓글을 생성한다")
		void givenPost_whenCreateRoot_thenReturnRootComment() {
			// given
			User writer = ObjectFixtures.getUser();
			Board board = ObjectFixtures.getBoard();
			Post post = ObjectFixtures.getPost(writer, board);

			// when
			Comment comment = Comment.ofRoot("댓글 내용", false, writer, post);

			// then
			assertThat(comment.getContent()).isEqualTo("댓글 내용");
			assertThat(comment.getIsDeleted()).isFalse();
			assertThat(comment.getIsAnonymous()).isFalse();
			assertThat(comment.getWriter()).isEqualTo(writer);
			assertThat(comment.getPost()).isEqualTo(post);
			assertThat(comment.getParentComment()).isNull();
			assertThat(comment.isChildComment()).isFalse();
		}
	}

	@Nested
	@DisplayName("답글 생성")
	class CreateChildComment {

		@Test
		@DisplayName("부모 댓글의 게시글을 공유하는 답글을 생성한다")
		void givenParentComment_whenCreateChildComment_thenInheritPost() {
			// given
			User parentWriter = ObjectFixtures.getUser();
			User childCommentWriter = ObjectFixtures.getCertifiedUser();
			Board board = ObjectFixtures.getBoard();
			Post post = ObjectFixtures.getPost(parentWriter, board);
			Comment parent = Comment.ofRoot("부모 댓글", false, parentWriter, post);

			// when
			Comment childComment = Comment.ofChildComment("답글 내용", true, childCommentWriter, parent);

			// then
			assertThat(childComment.getContent()).isEqualTo("답글 내용");
			assertThat(childComment.getIsDeleted()).isFalse();
			assertThat(childComment.getIsAnonymous()).isTrue();
			assertThat(childComment.getWriter()).isEqualTo(childCommentWriter);
			assertThat(childComment.getPost()).isEqualTo(post);
			assertThat(childComment.getParentComment()).isEqualTo(parent);
			assertThat(childComment.isChildComment()).isTrue();
		}
	}
}
