package net.causw.application.notification;


import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserBoardSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.BoardNotificationDto;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardNotificationService implements NotificationService {
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserBoardSubscribeRepository userBoardSubscribeRepository;

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

    public void sendByBoardIsSubscribed(Board board, Post post){
        //해당 게시판의 구독자 목록을 불러
        List<UserBoardSubscribe> userBoardSubscribeList = userBoardSubscribeRepository.findByBoardAndIsSubscribedTrue(board);
        BoardNotificationDto boardNotificationDto = BoardNotificationDto.of(board, post);

        Notification notification = Notification.of(post.getWriter(), boardNotificationDto.getTitle(), boardNotificationDto.getBody(), NoticeType.BOARD);

        saveNotification(notification);

        userBoardSubscribeList.stream()
                .map(UserBoardSubscribe::getUser)
                .forEach(user -> {
                    //여기서 게시글 푸시알람 보내면 됨
                    saveNotificationLog(user, notification);
                });

    }
}
