package net.causw.application.notification;

import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.user.User;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void send(String targetToken, String title, String body);
    void saveNotification(Notification notification);

    void saveNotificationLog(User user, Notification notification);
}
