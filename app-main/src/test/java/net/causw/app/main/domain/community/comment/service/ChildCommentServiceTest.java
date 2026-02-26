package net.causw.app.main.domain.community.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeChildComment;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentWriter;
import net.causw.app.main.domain.community.comment.util.ChildCommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.service.v1.CommentNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
public class ChildCommentServiceTest {

	@InjectMocks
	ChildCommentService childCommentService;

	@Mock
	UserReader userReader;
	@Mock
	CommentReader commentReader;
	@Mock
	ChildCommentReader childCommentReader;
	@Mock
	LikeChildCommentReader likeChildCommentReader;
	@Mock
	PostReader postReader;
	@Mock
	ChildCommentWriter childCommentWriter;
	@Mock
	LikeChildCommentWriter likeChildCommentWriter;
	@Mock
	ChildCommentValidator childCommentValidator;
	@Mock
	CommentNotificationService commentNotificationService;

	@Nested
	@DisplayName("대댓글 생성 테스트")
	class CreateChildCommentTest {
		User creator;
		Comment parentComment;
		Post post;
		Board board;

		@BeforeEach
		void setUp() {
			creator = mock(User.class);
			parentComment = mock(Comment.class);
			post = mock(Post.class);
			board = mock(Board.class);

			given(parentComment.getPost()).willReturn(post);
			given(post.getId()).willReturn("post-id");
			given(post.getBoard()).willReturn(board);
		}

		@DisplayName("대댓글 생성 성공")
		@Test
		void createChildComment_shouldSucceed() {
			// given
			ChildCommentCreateRequestDto requestDto = new ChildCommentCreateRequestDto(
				"대댓글 내용", "parent-comment-id", false);
			ChildCommentResponseDto expectedResponse = mock(ChildCommentResponseDto.class);

			given(userReader.findUserById("creator-id")).willReturn(creator);
			given(commentReader.getComment("parent-comment-id")).willReturn(parentComment);
			given(postReader.findById("post-id")).willReturn(post);
			given(childCommentReader.getChildCommentDetail(any(ChildComment.class), eq(creator), eq(board)))
				.willReturn(expectedResponse);

			// when
			ChildCommentResponseDto result = childCommentService.createChildComment("creator-id", requestDto);

			// then
			assertThat(result).isNotNull();
			verify(childCommentValidator, times(1)).validateForCreate(eq(creator), eq(post), eq(parentComment),
				any(ChildComment.class));
			verify(childCommentWriter, times(1)).save(any(ChildComment.class));
			verify(commentNotificationService, times(1)).sendByCommentIsSubscribed(eq(parentComment),
				any(ChildComment.class));
		}
	}

	@Nested
	@DisplayName("대댓글 수정 테스트")
	class UpdateChildCommentTest {
		@DisplayName("대댓글 내용 수정 성공")
		@Test
		void updateChildComment_shouldSucceed() {
			// given
			User updater = mock(User.class);
			ChildComment childComment = mock(ChildComment.class);
			Comment parentComment = mock(Comment.class);
			Post post = mock(Post.class);
			Board board = mock(Board.class);
			ChildCommentUpdateRequestDto requestDto = new ChildCommentUpdateRequestDto("수정된 대댓글 내용");
			ChildCommentResponseDto expectedResponse = mock(ChildCommentResponseDto.class);

			given(userReader.findUserById("updater-id")).willReturn(updater);
			given(childCommentReader.findById("child-comment-id")).willReturn(childComment);

			// 객체 체이닝을 위한 Mock 설정
			given(childComment.getParentComment()).willReturn(parentComment);
			given(parentComment.getPost()).willReturn(post);
			given(post.getId()).willReturn("post-id");
			given(postReader.findById("post-id")).willReturn(post);
			given(post.getBoard()).willReturn(board);

			given(childCommentReader.getChildCommentDetail(childComment, updater, board)).willReturn(expectedResponse);

			// when
			ChildCommentResponseDto result = childCommentService.updateChildComment("updater-id", "child-comment-id",
				requestDto);

			// then
			assertThat(result).isNotNull();
			verify(childComment, times(1)).update("수정된 대댓글 내용");
			verify(childCommentValidator, times(1)).validateForUpdate(updater, post, childComment);
			verify(childCommentWriter, times(1)).save(childComment);
		}
	}

	@Nested
	@DisplayName("대댓글 삭제 테스트")
	class DeleteChildCommentTest {
		@DisplayName("대댓글 삭제 성공")
		@Test
		void deleteChildComment_shouldSucceed() {
			// given
			User deleter = mock(User.class);
			ChildComment childComment = mock(ChildComment.class);
			Comment parentComment = mock(Comment.class);
			Post post = mock(Post.class);
			Board board = mock(Board.class);
			ChildCommentResponseDto expectedResponse = mock(ChildCommentResponseDto.class);

			given(userReader.findUserById("deleter-id")).willReturn(deleter);
			given(childCommentReader.findById("child-comment-id")).willReturn(childComment);

			// 객체 체이닝을 위한 Mock 설정
			given(childComment.getParentComment()).willReturn(parentComment);
			given(parentComment.getPost()).willReturn(post);
			given(post.getId()).willReturn("post-id");
			given(postReader.findById("post-id")).willReturn(post);
			given(post.getBoard()).willReturn(board);

			given(childCommentReader.getChildCommentDetail(childComment, deleter, board)).willReturn(expectedResponse);

			// when
			ChildCommentResponseDto result = childCommentService.deleteChildComment("deleter-id", "child-comment-id");

			// then
			assertThat(result).isNotNull();
			verify(childCommentValidator, times(1)).validateForDelete(deleter, post, childComment);
			verify(childComment, times(1)).delete();
			verify(childCommentWriter, times(1)).save(childComment);
		}
	}

	@Nested
	@DisplayName("대댓글 좋아요 테스트")
	class LikeChildCommentTest {
		User user;
		ChildComment childComment;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			childComment = mock(ChildComment.class);
			given(userReader.findUserById("user-id")).willReturn(user);
			given(childCommentReader.findById("child-comment-id")).willReturn(childComment);
		}

		@DisplayName("대댓글 좋아요 생성 성공")
		@Test
		void likeChildComment_shouldSucceed() {
			// given
			given(likeChildCommentReader.isChildCommentLiked(user, "child-comment-id")).willReturn(false);

			// when
			childCommentService.likeChildComment("user-id", "child-comment-id");

			// then
			verify(childCommentValidator, times(1)).validateWriterNotDeleted(childComment);
			verify(likeChildCommentWriter, times(1)).save(any(LikeChildComment.class));
		}

		@DisplayName("이미 좋아요를 누른 경우 예외 발생")
		@Test
		void likeChildComment_shouldFail_whenAlreadyLiked() {
			// given
			given(likeChildCommentReader.isChildCommentLiked(user, "child-comment-id")).willReturn(true);

			// when & then
			assertThatThrownBy(() -> childCommentService.likeChildComment("user-id", "child-comment-id"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("좋아요를 이미 누른 대댓글 입니다.");

			verify(likeChildCommentWriter, never()).save(any(LikeChildComment.class));
		}
	}

	@Nested
	@DisplayName("대댓글 좋아요 취소 테스트")
	class CancelLikeChildCommentTest {
		User user;
		ChildComment childComment;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			childComment = mock(ChildComment.class);
			given(userReader.findUserById("user-id")).willReturn(user);
			given(user.getId()).willReturn("user-id");
			given(childCommentReader.findById("child-comment-id")).willReturn(childComment);
		}

		@DisplayName("대댓글 좋아요 취소 성공")
		@Test
		void cancelLikeChildComment_shouldSucceed() {
			// given
			given(likeChildCommentReader.isChildCommentLiked(user, "child-comment-id")).willReturn(true);

			// when
			childCommentService.cancelLikeChildComment("user-id", "child-comment-id");

			// then
			verify(childCommentValidator, times(1)).validateWriterNotDeleted(childComment);
			verify(likeChildCommentWriter, times(1)).delete("child-comment-id", "user-id");
		}

		@DisplayName("좋아요를 누르지 않은 상태에서 취소 시 예외 발생")
		@Test
		void cancelLikeChildComment_shouldFail_whenNotLiked() {
			// given
			given(likeChildCommentReader.isChildCommentLiked(user, "child-comment-id")).willReturn(false);

			// when & then
			assertThatThrownBy(() -> childCommentService.cancelLikeChildComment("user-id", "child-comment-id"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("좋아요를 누르지 않은 대댓글입니다.");

			verify(likeChildCommentWriter, never()).delete(anyString(), anyString());
		}
	}
}