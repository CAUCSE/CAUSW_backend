package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import net.causw.adapter.persistence.notification.UserPostSubscribe;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserPostSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.BoardNotificationDto;
import net.causw.application.dto.notification.PostNotificationDto;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.stereotype.Service;

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


    //이건 게시글에 댓글이 달렸을 때 알람을 보내는 거니까 내 게시에 댓글이 달렸을 때 보내야 됨
    //그럼 결국엔 얘는 댓글 달리는 api에서 그게 내 댓글인 지 확인하고 맞으면 로그도 저장하고 fcm 푸시알람을 보내야함
    //createComment일때 댓글을 씀. 그때마다 그냥 얘를 호출해서, 어떤 post인지 보내주면 알아서 여기서 그 post를 구독하는 애인지 판단함.
    //그럼 그 구독여부는 postservice에서 관리함
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
