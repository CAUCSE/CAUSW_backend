package net.causw.app.main.domain.community.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.util.LikePostValidator;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostWriter;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
public class LikePostServiceTest {

	@InjectMocks
	LikePostService likePostService;

	@Mock
	PostReader postReader;

	@Mock
	LikePostReader likePostReader;

	@Mock
	LikePostWriter likePostWriter;

	@Mock
	LikePostValidator likePostValidator;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	UserReader userReader;

	@Mock
	BoardConfigReader boardConfigReader;

	@Mock
	BlockReader blockReader;

	@Nested
	@DisplayName("게시글 좋아요 테스트")
	class LikePostTest {
		User user;
		User writer;
		Board board;
		Post post;
		BoardConfig boardConfig;

		@BeforeEach
		void setUp() {
			user = ObjectFixtures.getCertifiedUserWithId("user-id");
			writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
			board = ObjectFixtures.getBoardV2WithId("board-id");
			post = Post.of(null, "게시글 내용", writer, false, board, List.of());
			boardConfig = boardConfig(BoardReadScope.ENROLLED, BoardVisibility.VISIBLE);

			given(userReader.findUserByIdNotDeleted("user-id")).willReturn(user);
			given(postReader.findByIdAndNotDeleted("post-id")).willReturn(post);
			given(boardConfigReader.getByBoardId("board-id")).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId("board-id")).willReturn(List.of("admin-id"));
		}

		@DisplayName("좋아요 수가 1~5이면 매번 알림 전용 이벤트를 발행함")
		@ParameterizedTest(name = "좋아요 {0}개")
		@ValueSource(longs = {1, 2, 3, 4, 5})
		void likePost_shouldPublishEvent_whenLikeCountIsBetweenOneAndFive(long likeCount) {
			// given
			given(likePostReader.countByPostId("post-id")).willReturn(likeCount);

			// when
			likePostService.likePost("user-id", "post-id");

			// then
			verify(likePostValidator, times(1)).validateForLike("user-id", "post-id");
			verify(likePostWriter, times(1)).saveLikePost("user-id", post);
			verify(eventPublisher)
				.publishEvent(new PostLikeMilestoneReachedEvent("post-id", "user-id", likeCount));
		}

		@DisplayName("마일스톤이 아닌 좋아요 수에서는 알림 전용 이벤트를 발행하지 않음")
		@Test
		void likePost_shouldNotPublishEvent_whenLikeCountIsNotMilestone() {
			// given
			given(likePostReader.countByPostId("post-id")).willReturn(6L);

			// when
			likePostService.likePost("user-id", "post-id");

			// then
			verify(likePostWriter).saveLikePost("user-id", post);
			verify(eventPublisher, never()).publishEvent(any());
		}

		@DisplayName("게시판 읽기 권한이 없으면 게시글 좋아요 실패")
		@Test
		void likePost_shouldFail_whenPostIsNotReadable() {
			// given
			user.setAcademicStatus(AcademicStatus.GRADUATED);

			// when & then
			assertThatThrownBy(() -> likePostService.likePost("user-id", "post-id"))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(likePostValidator, never()).validateForLike(anyString(), anyString());
			verify(likePostWriter, never()).saveLikePost(anyString(), any(Post.class));
		}

		@DisplayName("삭제된 게시판의 게시글에는 좋아요 실패")
		@Test
		void likePost_shouldFail_whenBoardIsDeleted() {
			// given
			board.setIsDeleted(true);

			// when & then
			assertThatThrownBy(() -> likePostService.likePost("user-id", "post-id"))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(likePostValidator, never()).validateForLike(anyString(), anyString());
			verify(likePostWriter, never()).saveLikePost(anyString(), any(Post.class));
		}

		@DisplayName("이미 좋아요를 누른 경우 예외 발생")
		@Test
		void likePost_shouldFail_whenAlreadyLiked() {
			// given
			doThrow(new RuntimeException())
				.when(likePostValidator).validateForLike("user-id", "post-id");

			// when & then
			assertThatThrownBy(() -> likePostService.likePost("user-id", "post-id"))
				.isInstanceOf(RuntimeException.class);

			verify(likePostWriter, never()).saveLikePost(anyString(), any(Post.class));
		}
	}

	@Nested
	@DisplayName("게시글 좋아요 취소 테스트")
	class CancelLikePostTest {
		User user;
		Board board;
		Post post;

		@BeforeEach
		void setUp() {
			user = ObjectFixtures.getCertifiedUserWithId("user-id");
			board = ObjectFixtures.getBoardV2WithId("board-id");
			post = Post.of(null, "게시글 내용", ObjectFixtures.getCertifiedUserWithId("writer-id"), false,
				board, List.of());

			given(userReader.findUserByIdNotDeleted("user-id")).willReturn(user);
			given(postReader.findByIdAndNotDeleted("post-id")).willReturn(post);
		}

		@DisplayName("읽기 권한을 재검사하지 않고 게시글 좋아요 취소 성공")
		@Test
		void cancelLikePost_shouldSucceed() {
			// when
			likePostService.cancelLikePost("user-id", "post-id");

			// then
			verify(likePostValidator, times(1)).validateForCancelLike("user-id", "post-id");
			verify(likePostWriter, times(1)).deleteLikePost("user-id", "post-id");
			verifyNoInteractions(boardConfigReader, blockReader);
		}

		@DisplayName("삭제된 게시판의 게시글 좋아요는 취소할 수 없음")
		@Test
		void cancelLikePost_shouldFail_whenBoardIsDeleted() {
			// given
			board.setIsDeleted(true);

			// when & then
			assertThatThrownBy(() -> likePostService.cancelLikePost("user-id", "post-id"))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(likePostValidator, never()).validateForCancelLike(anyString(), anyString());
			verify(likePostWriter, never()).deleteLikePost(anyString(), anyString());
			verifyNoInteractions(boardConfigReader, blockReader);
		}

		@DisplayName("좋아요를 누르지 않은 상태에서 취소 시 예외 발생")
		@Test
		void cancelLikePost_shouldFail_whenNotLiked() {
			// given
			doThrow(new RuntimeException())
				.when(likePostValidator).validateForCancelLike("user-id", "post-id");

			// when & then
			assertThatThrownBy(() -> likePostService.cancelLikePost("user-id", "post-id"))
				.isInstanceOf(RuntimeException.class);

			verify(likePostWriter, never()).deleteLikePost(anyString(), anyString());
		}
	}

	private static BoardConfig boardConfig(BoardReadScope readScope, BoardVisibility visibility) {
		return BoardConfig.of(
			"board-id",
			false,
			readScope,
			BoardWriteScope.ALL_USER,
			false,
			visibility,
			10,
			null,
			null);
	}
}
