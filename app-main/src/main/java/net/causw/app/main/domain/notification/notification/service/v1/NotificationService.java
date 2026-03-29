package net.causw.app.main.domain.notification.notification.service.v1;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.user.account.entity.user.User;

@Service
public interface NotificationService {
	void send(User user, String targetToken, String title, String body);

	void saveNotification(Notification notification);

	void saveNotificationLog(User user, Notification notification);
}
