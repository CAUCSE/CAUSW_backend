package net.causw.app.main.domain.notification.notification.service.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.UserBoardSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;

@ExtendWith(MockitoExtension.class)
class OfficialPostNotificationHandlerTest {

	@InjectMocks
	private OfficialPostNotificationHandler handler;

	@Mock
	private BoardReader boardReader;
	@Mock
	private PostReader postReader;
	@Mock
	private UserBoardSubscribeReader userBoardSubscribeReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private BoardConfigReader boardConfigReader;

	@Nested
	@DisplayName("공식 게시글 알림 (handle)")
	class HandleTest {

		@Test
		@DisplayName("성공: 공지글 + VISIBLE 게시판이면 구독자에게 알림 발송")
		void givenNoticeAndVisibleBoard_whenHandle_thenSendToSubscribers() {
			// given
			Board board = mockBoard("boardId");
			Post post = mockPost(board);
			BoardConfig boardConfig = mockBoardConfig(true, BoardVisibility.VISIBLE, BoardReadScope.BOTH);
			List<User> targets = List.of(mock(User.class), mock(User.class));

			given(boardReader.getById("boardId")).willReturn(board);
			given(postReader.findById("postId")).willReturn(post);
			given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);
			given(userBoardSubscribeReader.findNotificationTargets("boardId", BoardReadScope.BOTH))
				.willReturn(targets);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new OfficialPostEvent("boardId", "postId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUsers(eq(targets), any(), any());
			verify(notificationWriter).saveLogs(eq(targets), any());
		}

		@Test
		@DisplayName("스킵: 공지글이 아닌 경우 알림 미발송")
		void givenNotNotice_whenHandle_thenSkip() {
			// given
			Board board = mockBoard("boardId");
			Post post = mockPost(board);
			BoardConfig boardConfig = mockBoardConfig(false, BoardVisibility.VISIBLE, BoardReadScope.BOTH);

			given(boardReader.getById("boardId")).willReturn(board);
			given(postReader.findById("postId")).willReturn(post);
			given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);

			// when
			handler.handle(new OfficialPostEvent("boardId", "postId"));

			// then
			verify(notificationWriter, never()).save(any());
			verify(notificationPushSender, never()).sendToUsers(any(), any(), any());
		}

		@Test
		@DisplayName("스킵: 게시판이 비공개(HIDDEN)인 경우 알림 미발송")
		void givenHiddenBoard_whenHandle_thenSkip() {
			// given
			Board board = mockBoard("boardId");
			Post post = mockPost(board);
			BoardConfig boardConfig = mockBoardConfig(true, BoardVisibility.HIDDEN, BoardReadScope.BOTH);

			given(boardReader.getById("boardId")).willReturn(board);
			given(postReader.findById("postId")).willReturn(post);
			given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);

			// when
			handler.handle(new OfficialPostEvent("boardId", "postId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("성공: 발송 대상 목록이 비어있어도 알림 저장은 수행")
		void givenEmptyTargets_whenHandle_thenSaveNotificationOnly() {
			// given
			Board board = mockBoard("boardId");
			Post post = mockPost(board);
			BoardConfig boardConfig = mockBoardConfig(true, BoardVisibility.VISIBLE, BoardReadScope.ENROLLED);

			given(boardReader.getById("boardId")).willReturn(board);
			given(postReader.findById("postId")).willReturn(post);
			given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);
			given(userBoardSubscribeReader.findNotificationTargets("boardId", BoardReadScope.ENROLLED))
				.willReturn(List.of());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new OfficialPostEvent("boardId", "postId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUsers(eq(List.of()), any(), any());
		}

		@Test
		@DisplayName("성공: readScope=ENROLLED이면 ENROLLED 범위로 대상 조회")
		void givenEnrolledReadScope_whenHandle_thenQueryWithEnrolledScope() {
			// given
			Board board = mockBoard("boardId");
			Post post = mockPost(board);
			BoardConfig boardConfig = mockBoardConfig(true, BoardVisibility.VISIBLE, BoardReadScope.ENROLLED);
			List<User> targets = List.of(mock(User.class));

			given(boardReader.getById("boardId")).willReturn(board);
			given(postReader.findById("postId")).willReturn(post);
			given(boardConfigReader.getByBoardId("boardId")).willReturn(boardConfig);
			given(userBoardSubscribeReader.findNotificationTargets("boardId", BoardReadScope.ENROLLED))
				.willReturn(targets);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new OfficialPostEvent("boardId", "postId"));

			// then
			verify(userBoardSubscribeReader).findNotificationTargets("boardId", BoardReadScope.ENROLLED);
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	private Board mockBoard(String boardId) {
		Board board = mock(Board.class);
		given(board.getId()).willReturn(boardId);
		given(board.getName()).willReturn("공지 게시판");
		return board;
	}

	private Post mockPost(Board board) {
		Post post = mock(Post.class);
		given(post.getWriter()).willReturn(mock(User.class));
		given(post.getId()).willReturn("postId");
		given(post.getBoard()).willReturn(board);
		given(post.getContent()).willReturn("공지 내용입니다.");
		return post;
	}

	private BoardConfig mockBoardConfig(boolean isNotice, BoardVisibility visibility, BoardReadScope readScope) {
		BoardConfig config = mock(BoardConfig.class);
		given(config.isNotice()).willReturn(isNotice);
		given(config.getVisibility()).willReturn(visibility);
		if (isNotice && visibility == BoardVisibility.VISIBLE) {
			given(config.getReadScope()).willReturn(readScope);
		}
		return config;
	}
}
