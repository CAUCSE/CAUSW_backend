//package net.causw.application.notification;
//
//import lombok.RequiredArgsConstructor;
//import net.causw.adapter.persistence.notification.Notification;
//import net.causw.adapter.persistence.repository.notification.NotificationRepository;
//import net.causw.adapter.persistence.user.User;
//import net.causw.domain.model.enums.notification.NoticeType;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CommentNotificationService implements NotificationService{
//    private final FirebasePushNotificationService firebasePushNotificationService;
//    private final NotificationRepository notificationRepository;
//
//    @Override
//    public void send(String targetToken, String title, String body) {
//        firebasePushNotificationService.sendNotification(targetToken, title, body);
//    }
//
//    @Override
//    public void saveNotification(String title, String body, User user) {
//        notificationRepository.save(Notification.of(user, title, body, NoticeType.CEREMONY));
//    }
//}
