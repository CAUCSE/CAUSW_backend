package net.causw.app.main.domain.notification.notification.service.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.event.CommunityPostEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

@ExtendWith(MockitoExtension.class)
class CommunityPostNotificationListenerTest {

	@InjectMocks
	private CommunityPostNotificationListener handler;

	@Mock
	private BoardReader boardReader;
	@Mock
	private PostReader postReader;
	@Mock
	private BoardConfigReader boardConfigReader;
	@Mock
	private UserBoardSubscribeReader userBoardSubscribeReader;
	@Mock
	private BlockReader blockReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;

	@Nested
	@DisplayName("소통 게시판 새 글 알림 (handle)")
	class HandleTest {

		@Test
		@DisplayName("성공: 구독자에게 게시판 이름과 글 제목으로 알림 발송")
		void givenSubscribers_whenHandle_thenSendWithBoardNameAndPostTitle() {
			// given
			Board board = mockBoard();
			User writer = mockWriter();
			Post post = mockPost(writer);
			BoardConfig boardConfig = mockConfig(false, false, BoardVisibility.VISIBLE);
			List<User> targets = List.of(mock(User.class), mock(User.class));
			stubCommon(board, post, boardConfig);
			given(userBoardSubscribeReader.findCommunityNotificationTargets(
				eq("boardId"), eq(boardConfig), eq(Set.of()), anySet())).willReturn(targets);
			given(board.getName()).willReturn("자유게시판");
			given(post.getTitle()).willReturn("새 글 제목");
			given(post.getId()).willReturn("postId");
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));
			Map<String, String> logIdsByUserId = Map.of("t1", "log1", "t2", "log2");
			given(notificationWriter.saveLogs(eq(targets), any())).willReturn(logIdsByUserId);

			// when
			handler.handle(new CommunityPostEvent("boardId", "postId"));

			// then
			PushNotificationData expectedData = new PushNotificationData(null, NoticeType.COMMUNITY, "postId",
				"boardId");
			verify(notificationPushSender).sendToUsers(eq(targets), eq("자유게시판"), eq("새 글 제목"),
				eq(expectedData), eq(logIdsByUserId));
		}

		@Test
		@DisplayName("성공: 작성자 본인과 차단 관계인 유저는 발송 대상에서 제외")
		void givenBlockRelations_whenHandle_thenExcludeWriterAndBlockedUsers() {
			// given
			Board board = mockBoard();
			User writer = mockWriter();
			Post post = mockPost(writer);
			BoardConfig boardConfig = mockConfig(false, false, BoardVisibility.VISIBLE);
			stubCommon(board, post, boardConfig);
			given(blockReader.findBlockerUserIdsByBlockee(writer)).willReturn(Set.of("blocker"));
			given(blockReader.findBlockeeUserIdsByBlocker(writer)).willReturn(Set.of("blockee"));
			given(userBoardSubscribeReader.findCommunityNotificationTargets(any(), any(), any(), any()))
				.willReturn(List.of());

			// when
			handler.handle(new CommunityPostEvent("boardId", "postId"));

			// then
			@SuppressWarnings("unchecked") ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
			verify(userBoardSubscribeReader).findCommunityNotificationTargets(
				eq("boardId"), eq(boardConfig), eq(Set.of()), captor.capture());
			assertThat(captor.getValue()).containsExactlyInAnyOrder("writerId", "blocker", "blockee");
		}

		@Test
		@DisplayName("스킵: 발송 대상이 없으면 알림을 저장하지 않는다")
		void givenNoTargets_whenHandle_thenSkip() {
			// given
			Board board = mockBoard();
			User writer = mockWriter();
			Post post = mockPost(writer);
			BoardConfig boardConfig = mockConfig(false, false, BoardVisibility.VISIBLE);
			stubCommon(board, post, boardConfig);
			given(userBoardSubscribeReader.findCommunityNotificationTargets(any(), any(), any(), any()))
				.willReturn(List.of());

			// when
			handler.handle(new CommunityPostEvent("boardId", "postId"));

			// then
			verify(notificationWriter, never()).save(any());
			verify(notificationPushSender, never()).sendToUsers(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("스킵: 소식 게시판이면 알림 미발송")
		void givenNoticeBoard_whenHandle_thenSkip() {
			// given
			stubSkipPath(mockConfig(true, false, BoardVisibility.VISIBLE));

			// when
			handler.handle(new CommunityPostEvent("boardId", "postId"));

			// then
			verifyNotSent();
		}

		@Test
		@DisplayName("스킵: 시스템 공지 게시판이면 알림 미발송")
		void givenSystemNoticeBoard_whenHandle_thenSkip() {
			// given
			stubSkipPath(mockConfig(false, true, BoardVisibility.VISIBLE));

			// when
			handler.handle(new CommunityPostEvent("boardId", "postId"));

			// then
			verifyNotSent();
		}

		@Test
		@DisplayName("스킵: 비공개(HIDDEN) 게시판이면 알림 미발송")
		void givenHiddenBoard_whenHandle_thenSkip() {
			// given
			stubSkipPath(mockConfig(false, false, BoardVisibility.HIDDEN));

			// when
			handler.handle(new CommunityPostEvent("boardId", "postId"));

			// then
			verifyNotSent();
		}

		private void stubSkipPath(BoardConfig boardConfig) {
			Board board = mockBoard();
			Post post = mockPost(mock(User.class));
			given(boardReader.getById("boardId")).willReturn(board);
			given(postReader.findById("postId")).willReturn(post);
			given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);
		}

		private void verifyNotSent() {
			verify(userBoardSubscribeReader, never()).findCommunityNotificationTargets(any(), any(), any(), any());
			verify(notificationWriter, never()).save(any());
			verify(notificationPushSender, never()).sendToUsers(any(), any(), any(), any(), any());
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	private void stubCommon(Board board, Post post, BoardConfig boardConfig) {
		given(boardReader.getById("boardId")).willReturn(board);
		given(postReader.findById("postId")).willReturn(post);
		given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);
		given(boardConfigReader.getAdminIdSetMapByBoardIds(any())).willReturn(Map.of());
	}

	private Board mockBoard() {
		Board board = mock(Board.class);
		given(board.getId()).willReturn("boardId");
		return board;
	}

	private User mockWriter() {
		User writer = mock(User.class);
		given(writer.getId()).willReturn("writerId");
		return writer;
	}

	private Post mockPost(User writer) {
		Post post = mock(Post.class);
		given(post.getWriter()).willReturn(writer);
		return post;
	}

	private BoardConfig mockConfig(boolean isNotice, boolean isSystemNotice, BoardVisibility visibility) {
		BoardConfig config = mock(BoardConfig.class, withSettings().strictness(Strictness.LENIENT));
		given(config.isNotice()).willReturn(isNotice);
		given(config.isSystemNotice()).willReturn(isSystemNotice);
		given(config.getVisibility()).willReturn(visibility);
		return config;
	}
}
