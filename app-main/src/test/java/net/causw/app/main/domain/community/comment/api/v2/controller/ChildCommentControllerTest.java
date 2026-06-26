package net.causw.app.main.domain.community.comment.api.v2.controller;

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

import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.service.CommentService;
import net.causw.app.main.domain.community.comment.service.dto.CommentAuthorInfo;
import net.causw.app.main.domain.community.comment.service.dto.CommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentUpdateCommand;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

@ExtendWith(MockitoExtension.class)
class ChildCommentControllerTest {

	@InjectMocks
	private ChildCommentController childCommentController;

	@Mock
	private CommentService commentService;

	@Mock
	private CustomUserDetails userDetails;

	@Nested
	@DisplayName("대댓글 생성")
	class CreateChildComment {

		@Test
		@DisplayName("통합 댓글 생성 command로 부모 댓글 ID를 전달한다")
		void givenChildCommentRequest_whenCreate_thenDelegateToCommentService() {
			// given
			ChildCommentCreateRequestDto request = new ChildCommentCreateRequestDto(
				"대댓글 내용", "parent-comment-id", false);
			given(userDetails.getUserId()).willReturn("creator-id");
			given(commentService.createComment(any(CommentCreateCommand.class))).willReturn(commentResult());
			ArgumentCaptor<CommentCreateCommand> captor = ArgumentCaptor.forClass(CommentCreateCommand.class);

			// when
			ApiResponse<ChildCommentResponseDto> response = childCommentController.createChildComment(
				request, userDetails);

			// then
			then(commentService).should().createComment(captor.capture());
			CommentCreateCommand command = captor.getValue();
			assertThat(command.content()).isEqualTo("대댓글 내용");
			assertThat(command.postId()).isNull();
			assertThat(command.parentCommentId()).isEqualTo("parent-comment-id");
			assertThat(command.isAnonymous()).isFalse();
			assertThat(command.creatorId()).isEqualTo("creator-id");
			assertThat(response.getData().id()).isEqualTo("comment-id");
			assertThat(response.getData().isChildCommentLike()).isTrue();
		}
	}

	@Nested
	@DisplayName("대댓글 수정")
	class UpdateChildComment {

		@Test
		@DisplayName("통합 댓글 수정 command로 대댓글 ID를 전달한다")
		void givenChildCommentRequest_whenUpdate_thenDelegateToCommentService() {
			// given
			ChildCommentUpdateRequestDto request = new ChildCommentUpdateRequestDto("수정 내용");
			given(userDetails.getUserId()).willReturn("updater-id");
			given(commentService.updateReplyComment(any(CommentUpdateCommand.class))).willReturn(commentResult());
			ArgumentCaptor<CommentUpdateCommand> captor = ArgumentCaptor.forClass(CommentUpdateCommand.class);

			// when
			ApiResponse<ChildCommentResponseDto> response = childCommentController.updateChildComment(
				"child-comment-id", request, userDetails);

			// then
			then(commentService).should().updateReplyComment(captor.capture());
			CommentUpdateCommand command = captor.getValue();
			assertThat(command.commentId()).isEqualTo("child-comment-id");
			assertThat(command.content()).isEqualTo("수정 내용");
			assertThat(command.updaterId()).isEqualTo("updater-id");
			assertThat(response.getData().id()).isEqualTo("comment-id");
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
			3L,
			0L,
			List.of(childCommentResult()));
	}

	private CommentResult childCommentResult() {
		return new CommentResult(
			"child-comment-id",
			"답글 내용",
			LocalDateTime.of(2026, 6, 26, 12, 2),
			LocalDateTime.of(2026, 6, 26, 12, 3),
			false,
			"post-id",
			new CommentAuthorInfo(null, null, null, null, null, false, false, false, false, false),
			false,
			1L,
			0L,
			List.of());
	}
}
