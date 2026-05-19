package net.causw.app.main.domain.notification.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

	private final UserReader userReader;
	private final NotificationPushSender notificationPushSender;
	private final NotificationWriter notificationWriter;

	@Transactional
	public void sendPushToUser(User admin, String targetUserId, String title, String body, boolean saveNotification) {
		User targetUser = userReader.findUserById(targetUserId);

		notificationPushSender.sendToUser(targetUser, title, body);

		if (saveNotification) {
			Notification notification = notificationWriter.save(
				Notification.of(admin, title, body, NoticeType.SYSTEM, null, null));
			notificationWriter.saveLog(targetUser, notification);
		}
	}
}
