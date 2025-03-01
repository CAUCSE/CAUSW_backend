package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.user.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentNotificationService implements NotificationService{
    private final FirebasePushNotificationService firebasePushNotificationService;
    @Override
    public void send(String targetToken, String title, String body) {
        firebasePushNotificationService.sendNotification(targetToken, "[댓글] " + title, body);
    }

    @Override
    public void save(String title, String body, User user) {
        return;
    }
}
