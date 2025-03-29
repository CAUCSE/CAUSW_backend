package net.causw.application.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebasePushNotificationService {

    public void sendNotification(String targetToken, String title, String body) throws Exception {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(notification)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        log.info("Successfully sent message: " + response);
    }

}
