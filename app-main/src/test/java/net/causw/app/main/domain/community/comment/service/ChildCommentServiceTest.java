package net.causw.app.main.domain.community.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.comment.service.dto.ChildCommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentResult;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentUpdateCommand;
import net.causw.app.main.domain.community.comment.service.dto.CommentAuthorInfo;
import net.causw.app.main.domain.community.comment.service.dto.CommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentUpdateCommand;

@ExtendWith(MockitoExtension.class)
class ChildCommentServiceTest {

	@InjectMocks
	private ChildCommentService childCommentService;

	@Mock
	private CommentService commentService;

	@Nested
	@DisplayName("대댓글 생성")
	class CreateChildComment {

		@Test
		@DisplayName("통합 댓글 생성 서비스에 부모 댓글 ID를 전달한다")
		void givenChildCommand_whenCreate_thenDelegateToCommentService() {
			// given
			ChildCommentCreateCommand command = new ChildCommentCreateCommand(
				"대댓글 내용", "parent-comment-id", false, "creator-id");
			CommentResult commentResult = commentResult();
			given(commentService.createComment(any(CommentCreateCommand.class))).willReturn(commentResult);
			ArgumentCaptor<CommentCreateCommand> captor = ArgumentCaptor.forClass(CommentCreateCommand.class);

			// when
			ChildCommentResult result = childCommentService.createChildComment(command);

			// then
			then(commentService).should().createComment(captor.capture());
			CommentCreateCommand delegated = captor.getValue();
			assertThat(delegated.content()).isEqualTo("대댓글 내용");
			assertThat(delegated.postId()).isNull();
			assertThat(delegated.parentCommentId()).isEqualTo("parent-comment-id");
			assertThat(delegated.isAnonymous()).isFalse();
			assertThat(delegated.creatorId()).isEqualTo("creator-id");
			assertThat(result.id()).isEqualTo(commentResult.id());
			assertThat(result.isChildCommentLike()).isEqualTo(commentResult.isCommentLike());
		}
	}

	@Nested
	@DisplayName("대댓글 수정")
	class UpdateChildComment {

		@Test
		@DisplayName("통합 댓글 수정 서비스에 대댓글 ID를 전달한다")
		void givenChildCommand_whenUpdate_thenDelegateToCommentService() {
			// given
			ChildCommentUpdateCommand command = new ChildCommentUpdateCommand(
				"child-comment-id", "수정 내용", "updater-id");
			given(commentService.updateComment(any(CommentUpdateCommand.class))).willReturn(commentResult());
			ArgumentCaptor<CommentUpdateCommand> captor = ArgumentCaptor.forClass(CommentUpdateCommand.class);

			// when
			ChildCommentResult result = childCommentService.updateChildComment(command);

			// then
			then(commentService).should().updateComment(captor.capture());
			CommentUpdateCommand delegated = captor.getValue();
			assertThat(delegated.commentId()).isEqualTo("child-comment-id");
			assertThat(delegated.content()).isEqualTo("수정 내용");
			assertThat(delegated.updaterId()).isEqualTo("updater-id");
			assertThat(result.id()).isEqualTo("comment-id");
		}
	}

	@Nested
	@DisplayName("대댓글 삭제")
	class DeleteChildComment {

		@Test
		@DisplayName("통합 댓글 삭제 서비스를 호출한다")
		void givenChildCommentId_whenDelete_thenDelegateToCommentService() {
			// given
			given(commentService.deleteComment("deleter-id", "child-comment-id")).willReturn(commentResult());

			// when
			ChildCommentResult result = childCommentService.deleteChildComment("deleter-id", "child-comment-id");

			// then
			then(commentService).should().deleteComment("deleter-id", "child-comment-id");
			assertThat(result.id()).isEqualTo("comment-id");
		}
	}

	@Nested
	@DisplayName("대댓글 좋아요")
	class LikeChildComment {

		@Test
		@DisplayName("통합 댓글 좋아요 서비스를 호출한다")
		void givenChildCommentId_whenLike_thenDelegateToCommentService() {
			// when
			childCommentService.likeChildComment("user-id", "child-comment-id");

			// then
			then(commentService).should().likeComment("user-id", "child-comment-id");
		}

		@Test
		@DisplayName("통합 댓글 좋아요 취소 서비스를 호출한다")
		void givenChildCommentId_whenCancelLike_thenDelegateToCommentService() {
			// when
			childCommentService.cancelLikeChildComment("user-id", "child-comment-id");

			// then
			then(commentService).should().cancelLikeComment("user-id", "child-comment-id");
		}
	}

	private CommentResult commentResult() {
		return new CommentResult(
			"comment-id",
			"내용",
			LocalDateTime.of(2026, 6, 26, 12, 0),
			LocalDateTime.of(2026, 6, 26, 12, 1),
			false,
			"post-id",
			new CommentAuthorInfo(null, null, null, null, null, false, false, false, false, false),
			true,
			false,
			3L,
			0L,
			List.of());
	}
}
