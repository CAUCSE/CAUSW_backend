//package net.causw.application.notification;
//
//import lombok.RequiredArgsConstructor;
//import net.causw.adapter.persistence.user.User;
//import org.springframework.stereotype.Service;
//
//@RequiredArgsConstructor
//@Service
//public class PostNotificationService implements NotificationService{
//    private final FirebasePushNotificationService firebasePushNotificationService;
//    @Override
//    public void send(String targetToken, String title, String body) {
//        firebasePushNotificationService.sendNotification(targetToken, "[게시물] " + title, body);
//    }
//
//    @Override
//    public void saveNotification(String title, String body, User user) {
//    }
//}
