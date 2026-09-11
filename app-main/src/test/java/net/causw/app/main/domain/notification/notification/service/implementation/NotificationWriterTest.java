package net.causw.app.main.domain.notification.notification.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

@ExtendWith(MockitoExtension.class)
class NotificationWriterTest {

	@InjectMocks
	private NotificationWriter notificationWriter;

	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private NotificationLogRepository notificationLogRepository;

	@Test
	@DisplayName("saveLog는 저장된 로그의 id를 반환한다")
	void givenUserAndNotification_whenSaveLog_thenReturnSavedLogId() {
		// given
		User user = mock(User.class);
		Notification notification = mock(Notification.class);
		NotificationLog savedLog = mock(NotificationLog.class);
		given(savedLog.getId()).willReturn("logId");
		given(notificationLogRepository.save(any())).willReturn(savedLog);

		// when
		String result = notificationWriter.saveLog(user, notification);

		// then
		assertThat(result).isEqualTo("logId");
	}

	@Test
	@DisplayName("saveLogs는 saveAll이 반환한 순서가 입력 순서와 달라도 각 유저 자신의 로그 id로 매핑한다")
	void givenSaveAllReturnsReorderedLogs_whenSaveLogs_thenMapByOwnUserIdRegardlessOfOrder() {
		// given
		User user1 = userWithId("user1Id");
		User user2 = userWithId("user2Id");
		User user3 = userWithId("user3Id");
		Notification notification = mock(Notification.class);

		NotificationLog log1 = notificationLogOf("log1Id", user1);
		NotificationLog log2 = notificationLogOf("log2Id", user2);
		NotificationLog log3 = notificationLogOf("log3Id", user3);
		// saveAll이 입력 순서(user1, user2, user3)와 다르게 반환하는 상황을 시뮬레이션
		given(notificationLogRepository.saveAll(any())).willReturn(List.of(log3, log1, log2));

		// when
		Map<String, String> result = notificationWriter.saveLogs(List.of(user1, user2, user3), notification);

		// then: 반환 순서와 무관하게 각 유저 본인의 로그 id와 매핑됨
		assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
			"user1Id", "log1Id",
			"user2Id", "log2Id",
			"user3Id", "log3Id"));
	}

	private User userWithId(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		return user;
	}

	private NotificationLog notificationLogOf(String logId, User user) {
		NotificationLog log = mock(NotificationLog.class);
		given(log.getId()).willReturn(logId);
		given(log.getUser()).willReturn(user);
		return log;
	}
}
