package net.causw.app.main.domain.notification.notification.service.listener;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.SystemNoticeNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;

@ExtendWith(MockitoExtension.class)
class SystemNoticeNotificationListenerTest {

	@InjectMocks
	private SystemNoticeNotificationListener handler;

	@Mock
	private SystemNoticeReader systemNoticeReader;
	@Mock
	private NotificationSettingReader notificationSettingReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;

	@Nested
	@DisplayName("시스템 공지 알림 (handle)")
	class HandleTest {

		@Test
		@DisplayName("성공: 게시글을 재조회하고 서비스 공지 알림 ON인 전체 활성 유저에게 발송")
		void givenTargets_whenHandle_thenRefetchPostAndSendToTargets() {
			// given
			Post post = mockSystemNoticePost();
			List<User> targets = List.of(mock(User.class), mock(User.class));

			given(systemNoticeReader.getSystemNoticePost("postId")).willReturn(post);
			given(notificationSettingReader.findAllActiveUsersBySettingKey(
				UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)).willReturn(targets);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new SystemNoticeNotificationEvent("postId"));

			// then: ID로 게시글 재조회
			verify(systemNoticeReader).getSystemNoticePost("postId");

			// then: 알림 저장 + 발송 + 로그
			PushNotificationData expectedData = new PushNotificationData(NoticeType.SYSTEM_NOTICE, "postId", "boardId");
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUsers(eq(targets), eq("시스템 공지"), any(), eq(expectedData));
			verify(notificationWriter).saveLogs(eq(targets), any());
		}

		@Test
		@DisplayName("성공: 대상 조회는 SERVICE_NOTICE_ENABLED 설정 키로만 수행 (다른 키 아님)")
		void whenHandle_thenQueryTargetsBySericeNoticeEnabledKeyOnly() {
			// given
			Post post = mockSystemNoticePost();
			given(systemNoticeReader.getSystemNoticePost("postId")).willReturn(post);
			given(notificationSettingReader.findAllActiveUsersBySettingKey(
				UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)).willReturn(List.of());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new SystemNoticeNotificationEvent("postId"));

			// then
			verify(notificationSettingReader).findAllActiveUsersBySettingKey(
				UserNotificationSettingKey.SERVICE_NOTICE_ENABLED);
			verify(notificationSettingReader, never())
				.findAllActiveUsersBySettingKey(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED);
			verify(notificationSettingReader, never()).findSettingMap(any());
		}

		@Test
		@DisplayName("성공: 알림 제목/본문에 post.getTitle()이 사용됨")
		void givenPostTitle_whenHandle_thenUseTitleAsNotificationContent() {
			// given
			Post post = mockSystemNoticePost();
			given(post.getTitle()).willReturn("긴급 시스템 점검 안내");
			List<User> targets = List.of(mock(User.class));

			given(systemNoticeReader.getSystemNoticePost("postId")).willReturn(post);
			given(notificationSettingReader.findAllActiveUsersBySettingKey(
				UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)).willReturn(targets);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new SystemNoticeNotificationEvent("postId"));

			// then: 서비스 알림(Notification) 제목/본문
			ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
			verify(notificationWriter).save(notificationCaptor.capture());
			Notification saved = notificationCaptor.getValue();
			assertThat(saved.getTitle()).isEqualTo("긴급 시스템 점검 안내");
			assertThat(saved.getBody()).isEqualTo("긴급 시스템 점검 안내");

			// then: 푸시 알림 본문
			verify(notificationPushSender).sendToUsers(eq(targets), eq("시스템 공지"), eq("긴급 시스템 점검 안내"), any());
		}

		@Test
		@DisplayName("경계: 발송 대상이 0명이어도 예외 없이 처리되고 알림은 저장됨")
		void givenEmptyTargets_whenHandle_thenSaveNotificationWithoutError() {
			// given
			Post post = mockSystemNoticePost();
			given(systemNoticeReader.getSystemNoticePost("postId")).willReturn(post);
			given(notificationSettingReader.findAllActiveUsersBySettingKey(
				UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)).willReturn(List.of());
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when & then: 예외 없이 처리됨
			handler.handle(new SystemNoticeNotificationEvent("postId"));

			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUsers(eq(List.of()), any(), any(), any());
			verify(notificationWriter).saveLogs(eq(List.of()), any());
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	/** id/board/writer/title을 모두 stub한 기본 시스템 공지 게시글 */
	private Post mockSystemNoticePost() {
		Post post = mock(Post.class);
		Board board = mock(Board.class);
		given(board.getId()).willReturn("boardId");
		given(post.getId()).willReturn("postId");
		given(post.getBoard()).willReturn(board);
		given(post.getWriter()).willReturn(mock(User.class));
		given(post.getTitle()).willReturn("시스템 공지 제목");
		return post;
	}
}
