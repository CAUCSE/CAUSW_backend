package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.user.User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostNotificationService implements NotificationService{
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    @Override
    public void send(String targetToken, String title, String body) {
        firebasePushNotificationService.sendNotification(targetToken, title, body);
    }

    @Override
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    @Override
    public void saveNotificationLog(User user, Notification notification) {
        notificationLogRepository.save(NotificationLog.of(user, notification));
    }


    public void sendByIsSubscribed(){

    }

}
