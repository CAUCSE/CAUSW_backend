package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	public Optional<NotificationLog> findByIdAndUserId(String userId, String id) {
		return notificationLogRepository.findByIdAndUserId(id, userId);
	}

	public Page<NotificationLog> getNotificationList(String userId, Pageable pageable) {
		return notificationLogRepository.findByUserId(userId, pageable);
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