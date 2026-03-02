package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class NotificationWriter {

	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;

	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
	}

	public void saveLog(User user, Notification notification) {
		notificationLogRepository.save(NotificationLog.of(user, notification));
	}
}
