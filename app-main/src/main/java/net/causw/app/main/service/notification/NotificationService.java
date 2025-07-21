package net.causw.app.main.service.notification;

import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void send(User user, String targetToken, String title, String body);
    void saveNotification(Notification notification);

    void saveNotificationLog(User user, Notification notification);
}
