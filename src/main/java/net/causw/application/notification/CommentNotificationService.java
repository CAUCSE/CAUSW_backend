package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.notification.UserCommentSubscribe;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserCommentSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.CommentNotificationDto;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentNotificationService implements NotificationService{
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserCommentSubscribeRepository userCommentSubscribeRepository;
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
    public void sendByCommentIsSubscribed(Comment comment, ChildComment childComment){
        List<UserCommentSubscribe> userCommentSubscribeList = userCommentSubscribeRepository.findByCommentAndIsSubscribedTrue(comment);
        CommentNotificationDto commentNotificationDto = CommentNotificationDto.of(comment, childComment);

        Notification notification = Notification.of(childComment.getWriter(), commentNotificationDto.getTitle(), commentNotificationDto.getBody(), NoticeType.COMMENT);

        saveNotification(notification);

        userCommentSubscribeList.stream()
                .map(UserCommentSubscribe::getUser)
                .forEach(user -> {
                    user.getFcmTokens().forEach(token -> {
                        send(user, token, commentNotificationDto.getTitle(), commentNotificationDto.getBody());
                    });
                    saveNotificationLog(user, notification);
                });
    }
}
