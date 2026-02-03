package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationLogReader {
	private final NotificationLogRepository notificationLogRepository;
	private final PageableFactory pageableFactory;

	public List<NotificationLog> findTop1Unread(User user) {
		return notificationLogRepository.findByUserAndIsReadFalseNotification(
			user, pageableFactory.create(0, StaticValue.HOME_NOTIFICATION_PAGE_SIZE));
	}

	public List<NotificationLog> findUnreadUpToLimit(User user) {
		return notificationLogRepository.findUnreadLogsUpToLimit(
			user, pageableFactory.create(0, StaticValue.MAX_NOTIFICATION_COUNT));
	}
}