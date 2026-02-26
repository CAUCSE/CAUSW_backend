package net.causw.app.main.domain.community.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeComment;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentSubscribeWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentWriter;
import net.causw.app.main.domain.community.comment.util.CommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.service.implementation.UserCommentSubscribeReader;
import net.causw.app.main.domain.notification.notification.service.v1.PostNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

	@InjectMocks
	CommentService commentService;

	@Mock
	BoardConfigReader boardConfigReader;
	@Mock
	PostReader postReader;
	@Mock
	UserReader userReader;
	@Mock
	CommentReader commentReader;
	@Mock
	LikeCommentReader likeCommentReader;
	@Mock
	LikeChildCommentReader likeChildCommentReader;
	@Mock
	CommentWriter commentWriter;
	@Mock
	LikeCommentWriter likeCommentWriter;
	@Mock
	CommentSubscribeWriter commentSubscribeWriter;
	@Mock
	CommentValidator commentValidator;
	@Mock
	PostNotificationService postNotificationService;
	@Mock
	UserBlockEntityService userBlockEntityService;
	@Mock
	UserCommentSubscribeReader userCommentSubscribeReader;

	@Nested
	@DisplayName("댓글 생성 테스트")
	class CreateCommentTest {
		User creator;
		Post post;
		Board board;

		@BeforeEach
		void setUp() {
			creator = mock(User.class);
			post = mock(Post.class);
			board = mock(Board.class);

			given(post.getBoard()).willReturn(board);
		}

		@DisplayName("댓글 생성 성공")
		@Test
		void createComment_shouldSucceed() {
			// given
			CommentCreateRequestDto requestDto = new CommentCreateRequestDto("댓글 내용", "post-id", false);
			CommentResponseDto expectedResponse = mock(CommentResponseDto.class);

			given(postReader.findById("post-id")).willReturn(post);
			given(userReader.findUserById("creator-id")).willReturn(creator);
			given(commentReader.getCommentDetail(any(Comment.class), eq(creator), eq(board)))
				.willReturn(expectedResponse);

			// when
			CommentResponseDto result = commentService.createComment("creator-id", requestDto);

			// then
			assertThat(result).isNotNull();
			verify(commentValidator, times(1)).validateForCreate(eq(creator), eq(post), any(Comment.class));
			verify(commentWriter, times(1)).save(any(Comment.class));
			verify(commentSubscribeWriter, times(1)).createCommentSubscribe(eq(creator), any());
			verify(postNotificationService, times(1)).sendByPostIsSubscribed(eq(post), any(Comment.class));
		}
	}

	@Nested
	@DisplayName("댓글 목록 조회 테스트")
	class FindAllCommentsTest {
		User user;
		Post post;
		Board board;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			post = mock(Post.class);
			board = mock(Board.class);

			given(post.getBoard()).willReturn(board);
			given(board.getId()).willReturn("board-id");
		}

		@DisplayName("댓글 목록 조회 성공 (대댓글 포함)")
		@Test
		void findAllComments_shouldSucceed() {
			// given
			given(user.getId()).willReturn("user-id");
			PageRequest pageable = PageRequest.of(0, 10);
			Comment comment = mock(Comment.class);
			ChildComment childComment = mock(ChildComment.class);
			CommentResponseDto responseDto = mock(CommentResponseDto.class);

			given(comment.getId()).willReturn("comment-id");
			given(childComment.getId()).willReturn("child-comment-id");
			given(comment.getChildCommentList()).willReturn(List.of(childComment));

			Page<Comment> commentsPage = new PageImpl<>(List.of(comment), pageable, 1);

			given(postReader.findById("post-id")).willReturn(post);
			given(userReader.findUserById("user-id")).willReturn(user);
			given(boardConfigReader.getAdminIdsByBoardId("board-id")).willReturn(List.of("admin-id"));
			given(userBlockEntityService.findBlockeeUserIdsByBlocker(user)).willReturn(Collections.emptySet());
			given(commentReader.getComments("post-id", pageable)).willReturn(commentsPage);

			// Bulk 조회 목업 세팅
			given(likeCommentReader.getCommentLikeCounts(anyList())).willReturn(Map.of("comment-id", 5L));
			given(likeCommentReader.getLikedCommentIds("user-id", List.of("comment-id")))
				.willReturn(Set.of("comment-id"));
			given(userCommentSubscribeReader.getSubscribedCommentIds("user-id", List.of("comment-id")))
				.willReturn(Collections.emptySet());
			given(likeChildCommentReader.getChildCommentLikeCounts(anyList()))
				.willReturn(Map.of("child-comment-id", 2L));
			given(likeChildCommentReader.getLikedChildCommentIds("user-id", List.of("child-comment-id")))
				.willReturn(Collections.emptySet());

			// DTO 조립 목업 세팅
			given(commentReader.getCommentListDetails(
				eq(comment), eq(user), anySet(), anyList(), anyMap(), anySet(), anySet(), anyMap(), anySet()))
				.willReturn(responseDto);

			// when
			Page<CommentResponseDto> result = commentService.findAllComments("user-id", "post-id", pageable);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.getContent()).hasSize(1));

			verify(commentValidator, times(1)).validateForFind(user, post);
			verify(likeCommentReader, times(1)).getCommentLikeCounts(anyList());
			verify(likeChildCommentReader, times(1)).getChildCommentLikeCounts(anyList());
		}

		@DisplayName("댓글이 없을 경우 조기 종료(Early Exit) 성공")
		@Test
		void findAllComments_shouldReturnEmptyPage_whenNoComments() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);
			Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

			given(postReader.findById("post-id")).willReturn(post);
			given(userReader.findUserById("user-id")).willReturn(user);
			given(commentReader.getComments("post-id", pageable)).willReturn(emptyPage);

			// when
			Page<CommentResponseDto> result = commentService.findAllComments("user-id", "post-id", pageable);

			// then
			assertThat(result.isEmpty()).isTrue();

			// 댓글이 없으므로 N+1 방지용 Bulk 조회가 아예 호출되지 않아야 함
			verify(likeCommentReader, never()).getCommentLikeCounts(anyList());
			verify(likeChildCommentReader, never()).getChildCommentLikeCounts(anyList());
		}
	}

	@Nested
	@DisplayName("댓글 수정 테스트")
	class UpdateCommentTest {
		@DisplayName("댓글 내용 수정 성공")
		@Test
		void updateComment_shouldSucceed() {
			// given
			User updater = mock(User.class);
			Post post = mock(Post.class);
			Board board = mock(Board.class);
			Comment comment = mock(Comment.class);
			CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정된 댓글 내용");
			CommentResponseDto expectedResponse = mock(CommentResponseDto.class);

			given(userReader.findUserById("updater-id")).willReturn(updater);
			given(commentReader.getComment("comment-id")).willReturn(comment);
			given(comment.getPost()).willReturn(post);
			given(post.getId()).willReturn("post-id");
			given(postReader.findById("post-id")).willReturn(post);
			given(post.getBoard()).willReturn(board);
			given(commentReader.getCommentDetail(comment, updater, board)).willReturn(expectedResponse);

			// when
			CommentResponseDto result = commentService.updateComment("updater-id", "comment-id", requestDto);

			// then
			assertThat(result).isNotNull();
			verify(commentValidator, times(1)).validateForUpdate(updater, post, comment);
			verify(comment, times(1)).update("수정된 댓글 내용");
			verify(commentWriter, times(1)).save(comment);
		}
	}

	@Nested
	@DisplayName("댓글 삭제 테스트")
	class DeleteCommentTest {
		@DisplayName("댓글 삭제 성공")
		@Test
		void deleteComment_shouldSucceed() {
			// given
			User deleter = mock(User.class);
			Post post = mock(Post.class);
			Board board = mock(Board.class);
			Comment comment = mock(Comment.class);
			CommentResponseDto expectedResponse = mock(CommentResponseDto.class);

			given(userReader.findUserById("deleter-id")).willReturn(deleter);
			given(commentReader.getComment("comment-id")).willReturn(comment);
			given(comment.getPost()).willReturn(post);
			given(post.getId()).willReturn("post-id");
			given(postReader.findById("post-id")).willReturn(post);
			given(post.getBoard()).willReturn(board);
			given(commentReader.getCommentDetail(comment, deleter, board)).willReturn(expectedResponse);

			// when
			CommentResponseDto result = commentService.deleteComment("deleter-id", "comment-id");

			// then
			assertThat(result).isNotNull();
			verify(commentValidator, times(1)).validateForDelete(deleter, post, comment);
			verify(comment, times(1)).delete();
			verify(commentWriter, times(1)).save(comment);
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 테스트")
	class LikeCommentTest {
		User user;
		Comment comment;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			comment = mock(Comment.class);
			given(userReader.findUserById("user-id")).willReturn(user);
			given(commentReader.getComment("comment-id")).willReturn(comment);
		}

		@DisplayName("댓글 좋아요 성공")
		@Test
		void likeComment_shouldSucceed() {
			// when
			commentService.likeComment("user-id", "comment-id");

			// then
			verify(commentValidator, times(1)).validateForLike(user, comment);
			verify(likeCommentWriter, times(1)).save(any(LikeComment.class));
		}

		@DisplayName("이미 좋아요를 누른 경우 예외 발생")
		@Test
		void likeComment_shouldFail_whenAlreadyLiked() {
			// given
			doThrow(new RuntimeException("좋아요를 이미 누른 댓글 입니다"))
				.when(commentValidator).validateForLike(user, comment);

			// when & then
			assertThatThrownBy(() -> commentService.likeComment("user-id", "comment-id"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("좋아요를 이미 누른 댓글 입니다");

			verify(likeCommentWriter, never()).save(any(LikeComment.class));
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 취소 테스트")
	class CancelLikeCommentTest {
		User user;
		Comment comment;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			comment = mock(Comment.class);
			given(userReader.findUserById("user-id")).willReturn(user);
			given(commentReader.getComment("comment-id")).willReturn(comment);
		}

		@DisplayName("댓글 좋아요 취소 성공")
		@Test
		void cancelLikeComment_shouldSucceed() {
			// given
			given(user.getId()).willReturn("user-id");

			// when
			commentService.cancelLikeComment("user-id", "comment-id");

			// then
			verify(commentValidator, times(1)).validateForCancelLike(user, comment);
			verify(likeCommentWriter, times(1)).delete("comment-id", "user-id");
		}

		@DisplayName("좋아요를 누르지 않은 상태에서 취소 시 예외 발생")
		@Test
		void cancelLikeComment_shouldFail_whenNotLiked() {
			// given
			doThrow(new RuntimeException("좋아요를 누르지 않은 댓글입니다"))
				.when(commentValidator).validateForCancelLike(user, comment);

			// when & then
			assertThatThrownBy(() -> commentService.cancelLikeComment("user-id", "comment-id"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("좋아요를 누르지 않은 댓글입니다");

			verify(likeCommentWriter, never()).delete(anyString(), anyString());
		}
	}
}