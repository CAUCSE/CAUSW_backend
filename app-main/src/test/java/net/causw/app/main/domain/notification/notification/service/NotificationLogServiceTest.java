package net.causw.app.main.domain.notification.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationLogReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationLogService 단위 테스트")
class NotificationLogServiceTest {

	@InjectMocks
	private NotificationLogService notificationLogService;

	@Mock
	private NotificationLogReader notificationLogReader;

	@Mock
	private NotificationDtoMapper notificationDtoMapper;

	@Nested
	@DisplayName("최신 미확인 알림 조회 테스트")
	class GetLatestUnreadTest {

		@Test
		@DisplayName("유저의 읽지 않은 최신 알림이 있으면 조회하여 DTO를 반환한다")
		void givenUserWithUnreadLog_whenGetLatestUnread_thenReturnsNotificationResponseDto() {
			// given
			String userId = "user-uuid-123";
			User user = ObjectFixtures.getCertifiedUser();
			Notification notification = ObjectFixtures.getNotification(user);
			NotificationLog log = NotificationLog.of(user, notification);

			given(notificationLogReader.getLatestUnread(userId))
				.willReturn(Optional.of(log));

			NotificationResponseDto expectedDto = NotificationResponseDto.builder()
				.notificationLogId(log.getId())
				.title(notification.getTitle())
				.isRead(false)
				.build();

			given(notificationDtoMapper.toNotificationResponseDto(any(), any(), anyBoolean()))
				.willReturn(expectedDto);

			// when
			NotificationResponseDto result = notificationLogService.getLatestUnread(userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.notificationLogId()).isEqualTo(log.getId());
			assertThat(result.title()).isEqualTo(notification.getTitle());
			then(notificationLogReader).should().getLatestUnread(userId);
		}

		@Test
		@DisplayName("유저의 읽지 않은 알림이 없으면 null을 반환한다")
		void givenNoUnreadLog_whenGetLatestUnread_thenReturnsNull() {
			// given
			String userId = "user-uuid-123";
			given(notificationLogReader.getLatestUnread(userId))
				.willReturn(Optional.empty());

			// when
			NotificationResponseDto result = notificationLogService.getLatestUnread(userId);

			// then
			assertThat(result).isNull();
			then(notificationLogReader).should().getLatestUnread(userId);
		}
	}

	@Nested
	@DisplayName("미확인 알림 개수 조회 테스트")
	class GetNotificationLogCountTest {

		@Test
		@DisplayName("유저의 읽지 않은 알림 목록을 가져와 그 개수를 반환한다")
		void givenUnreadLogs_whenGetNotificationLogCount_thenReturnsNotificationCountResponseDto() {
			// given
			String userId = "user-uuid-123";
			User user = ObjectFixtures.getCertifiedUser();
			Notification notification = ObjectFixtures.getNotification(user);
			List<NotificationLog> logs = List.of(
				NotificationLog.of(user, notification),
				NotificationLog.of(user, notification));

			given(notificationLogReader.findUnreadUpToLimit(userId))
				.willReturn(logs);

			// when
			NotificationCountResponseDto result = notificationLogService.getNotificationLogCount(userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.notificationLogCount()).isEqualTo(2);
			then(notificationLogReader).should().findUnreadUpToLimit(userId);
		}
	}
}