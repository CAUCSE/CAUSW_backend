package net.causw.app.main.domain.notification.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationLogService 단위 테스트")
class NotificationLogServiceTest {

	@InjectMocks
	private NotificationLogService notificationLogService;

	@Mock
	private NotificationLogRepository notificationLogRepository;
	@Mock
	private PageableFactory pageableFactory;

	@Nested
	@DisplayName("getNotificationTop1")
	class GetNotificationTop1Test {

		@Test
		@DisplayName("유저의 읽지 않은 최신 알림이 있으면 조회하여 반환한다")
		void givenUserWithUnreadLog_whenGetNotificationTop1_thenReturnsNotificationResponseDto() {
			// given
			User user = ObjectFixtures.getCertifiedUser();
			Notification notification = ObjectFixtures.getNotification(user);
			NotificationLog log = NotificationLog.of(user, notification);
			Pageable mockPageable = mock(Pageable.class);
			given(pageableFactory.create(any(), any())).willReturn(mockPageable);
			given(notificationLogRepository.findByUserAndIsReadFalseNotification(user, mockPageable))
				.willReturn(List.of(log));

			// when
			NotificationResponseDto result = notificationLogService.getNotificationTop1(user);

			// then
			assertThat(result).isNotNull();
			assertThat(result.title()).isEqualTo("최신 알림");
			then(notificationLogRepository)
				.should().findByUserAndIsReadFalseNotification(user, mockPageable);
		}

		@Test
		@DisplayName("유저의 읽지 않은 알림이 없으면 null을 반환한다")
		void givenUserWithNoUnreadLog_whenGetNotificationTop1_thenReturnsNull() {
			// given
			User user = ObjectFixtures.getCertifiedUser();
			Pageable mockPageable = mock(Pageable.class);
			given(pageableFactory.create(any(), any())).willReturn(mockPageable);
			given(notificationLogRepository.findByUserAndIsReadFalseNotification(user, mockPageable))
				.willReturn(Collections.emptyList());

			// when
			NotificationResponseDto result = notificationLogService.getNotificationTop1(user);

			// then
			assertThat(result).isNull();
			then(notificationLogRepository)
				.should().findByUserAndIsReadFalseNotification(user, mockPageable);
			then(pageableFactory).should().create(any(), any());
		}
	}

	@Nested
	@DisplayName("getNotificationLogCount")
	class GetNotificationLogCountTest {

		@Test
		@DisplayName("유저의 읽지 않은 알림 목록을 가져와 개수를 반환한다")
		void givenUser_whenGetNotificationLogCount_thenReturnsNotificationCountResponseDto() {
			// given
			User user = ObjectFixtures.getCertifiedUser();

			Pageable mockPageable = mock(Pageable.class);
			given(pageableFactory.create(any(), any())).willReturn(mockPageable);

			Notification notification = ObjectFixtures.getNotification(user);
			NotificationLog log1 = NotificationLog.of(user, notification);
			NotificationLog log2 = NotificationLog.of(user, notification);

			given(notificationLogRepository.findUnreadLogsUpToLimit(eq(user), eq(mockPageable)))
				.willReturn(List.of(log1, log2));

			// when
			NotificationCountResponseDto result = notificationLogService.getNotificationLogCount(user);

			// then
			assertThat(result.notificationLogCount()).isEqualTo(2L);
		}
	}
}