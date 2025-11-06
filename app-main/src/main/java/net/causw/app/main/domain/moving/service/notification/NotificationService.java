package net.causw.app.main.domain.moving.service.notification;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.moving.model.entity.notification.Notification;
import net.causw.app.main.domain.user.account.entity.user.User;

@Service
public interface NotificationService {
	void send(User user, String targetToken, String title, String body);

	void saveNotification(Notification notification);

	void saveNotificationLog(User user, Notification notification);
}
