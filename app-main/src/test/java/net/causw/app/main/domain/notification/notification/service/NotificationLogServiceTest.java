package net.causw.app.main.domain.notification.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationLogReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
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
	@DisplayName("알림 리스트 페이징 조회 테스트")
	class GetNotificationListTest {

		@Test
		@DisplayName("유저의 알림 리스트를 성공적으로 페이징하여 DTO로 변환한다")
		void givenUserWithLogs_whenGetNotificationList_thenReturnsPagedDto() {
			// given
			String userId = "user-uuid-123";
			Pageable pageable = PageRequest.of(0, 20);

			User user = ObjectFixtures.getCertifiedUser();
			Notification notification = ObjectFixtures.getNotification(user);
			NotificationLog log = NotificationLog.of(user, notification);
			ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.now());

			Page<NotificationLog> mockPage = new PageImpl<>(List.of(log), pageable, 1);

			given(notificationLogReader.getNotificationList(userId, pageable))
				.willReturn(mockPage);

			NotificationResponseDto expectedDto = NotificationResponseDto.builder()
				.notificationLogId(log.getId())
				.title(notification.getTitle())
				.isRead(false)
				.build();

			given(notificationDtoMapper.toNotificationResponseDto(
				any(), any(), anyBoolean(), any(LocalDateTime.class)))
				.willReturn(expectedDto);

			// when
			Page<NotificationResponseDto> result = notificationLogService.getNotificationList(userId, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0)).isEqualTo(expectedDto);
			assertThat(result.getTotalElements()).isEqualTo(1);

			then(notificationLogReader).should().getNotificationList(userId, pageable);
			then(notificationDtoMapper).should().toNotificationResponseDto(
				any(), any(), anyBoolean(), any(LocalDateTime.class));
		}
	}

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
			ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.now());

			given(notificationLogReader.getLatestUnread(userId))
				.willReturn(Optional.of(log));

			NotificationResponseDto expectedDto = NotificationResponseDto.builder()
				.notificationLogId(log.getId())
				.title(notification.getTitle())
				.isRead(false)
				.createdAt(LocalDateTime.now())
				.build();

			given(notificationDtoMapper.toNotificationResponseDto(any(), any(), anyBoolean(), any(LocalDateTime.class)))
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

	@Nested
	@DisplayName("알림 읽음 처리 테스트")
	class ReadNotificationTest {

		@Test
		@DisplayName("존재하는 알림의 경우 읽음 상태(isRead)를 true로 성공적으로 변경한다")
		void givenValidIds_whenReadNotification_thenSetsIsReadTrue() {
			// given
			String userId = "user-uuid-123";
			String logId = "log-uuid-456";

			User user = ObjectFixtures.getCertifiedUser();
			Notification notification = ObjectFixtures.getNotification(user);
			NotificationLog log = NotificationLog.of(user, notification);
			ReflectionTestUtils.setField(log, "isRead", false); // 초기 상태 명시적 세팅

			given(notificationLogReader.findByIdAndUserId(logId, userId))
				.willReturn(Optional.of(log));

			// when
			notificationLogService.updateNotificationLogAsRead(userId, logId);

			// then
			// 상태가 정상적으로 true로 업데이트되었는지 검증
			assertThat(log.getIsRead()).isTrue();

			// mock 객체가 의도대로 호출되었는지 행위 검증
			then(notificationLogReader).should().findByIdAndUserId(logId, userId);
		}

		@Test
		@DisplayName("알림을 찾을 수 없거나 권한이 없는 경우 예외가 발생한다")
		void givenInvalidLogId_whenReadNotification_thenThrowsException() {
			// given
			String userId = "user-uuid-123";
			String logId = "log-uuid-456";

			given(notificationLogReader.findByIdAndUserId(logId, userId))
				.willReturn(Optional.empty());

			// when & then
			// 프로젝트의 커스텀 BaseException 계열로 예외가 던져지는지 검증
			assertThatThrownBy(() -> notificationLogService.updateNotificationLogAsRead(userId, logId))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			then(notificationLogReader).should().findByIdAndUserId(logId, userId);
		}
	}

}