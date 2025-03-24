package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
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
@Service
public class PostNotificationService implements NotificationService{
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserPostSubscribeRepository userPostSubscribeRepository;
    @Override
    public void send(String targetToken, String title, String body) {
        firebasePushNotificationService.sendNotification(targetToken, "[게시물] " + title, body);
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
                    //특정 게시글에 댓글이 달리는 경우의 푸시알림 전송 여기서 진행
                    saveNotificationLog(user, notification);
                });
    }


}
