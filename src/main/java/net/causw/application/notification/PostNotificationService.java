package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.notification.UserPostSubscribe;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserPostSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.PostNotificationDto;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostNotificationService implements NotificationService{
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserPostSubscribeRepository userPostSubscribeRepository;
    @Override
    public void send(User user, String targetToken, String title, String body) {
        try {
            firebasePushNotificationService.sendNotification(targetToken, title, body);
        } catch (Exception e) {
            log.warn("FCM 전송 실패: {}, 이유: {}", targetToken, e.getMessage());

            String msg = e.getMessage();
            if (msg != null &&
                    (msg.contains("registration-token-not-registered") || msg.contains("invalid-registration-token"))) {
                user.getFcmTokens().remove(targetToken);
                log.info("만료된 FCM 토큰 제거됨: {}", targetToken);
            }
        }
    }

    @Override
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    @Override
    public void saveNotificationLog(User user, Notification notification) {
        notificationLogRepository.save(NotificationLog.of(user, notification));
    }

    @Async("asyncExecutor")
    @Transactional
    public void sendByPostIsSubscribed(Post post, Comment comment){
        List<UserPostSubscribe> userPostSubscribeList = userPostSubscribeRepository.findByPostAndIsSubscribedTrue(post);
        PostNotificationDto postNotificationDto = PostNotificationDto.of(post, comment);

        Notification notification = Notification.of(comment.getWriter(), postNotificationDto.getTitle(), postNotificationDto.getBody(), NoticeType.POST);

        saveNotification(notification);

        userPostSubscribeList.stream()
                .map(UserPostSubscribe::getUser)
                .forEach(user -> {
                    user.getFcmTokens().forEach(token -> {
                        send(user, token, postNotificationDto.getTitle(), postNotificationDto.getBody());
                    });

                    saveNotificationLog(user, notification);
                });
    }


}
