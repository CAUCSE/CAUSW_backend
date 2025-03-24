package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CommentNotificationService implements NotificationService{
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserCommentSubscribeRepository userCommentSubscribeRepository;
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
                    //특정 게시글에 댓글이 달리는 경우의 푸시알림 전송 여기서 진행
                    saveNotificationLog(user, notification);
                });
    }
}
