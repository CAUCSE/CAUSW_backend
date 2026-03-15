package net.causw.app.main.domain.notification.notification.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationLogReader {
	private final NotificationLogRepository notificationLogRepository;
	private final PageableFactory pageableFactory;

	public Optional<NotificationLog> findByIdAndUserId(String id, String userId) {
		return notificationLogRepository.findByIdAndUserId(id, userId);
	}

	public List<NotificationLog> getNotificationList(String userId, boolean isRead) {
		LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

		return notificationLogRepository.findRecentNotifications(userId, isRead, sevenDaysAgo);
	}

	public Optional<NotificationLog> getLatestUnread(String userId) {

		List<NotificationLog> notificationLog = notificationLogRepository.findByUserIdAndIsReadFalseNotification(
			userId, pageableFactory.create(0, StaticValue.HOME_NOTIFICATION_PAGE_SIZE));

		return notificationLog.stream().findFirst();
	}

	public List<NotificationLog> findUnreadUpToLimit(String userId) {
		return notificationLogRepository.findByUserIdUnreadLogsUpToLimit(
			userId, pageableFactory.create(0, StaticValue.MAX_NOTIFICATION_COUNT));
	}
}