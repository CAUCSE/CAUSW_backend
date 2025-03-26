package net.causw.application.notification;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardNotificationService implements NotificationService {
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserBoardSubscribeRepository userBoardSubscribeRepository;

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
    public void sendByBoardIsSubscribed(Board board, Post post){
        List<UserBoardSubscribe> userBoardSubscribeList = userBoardSubscribeRepository.findByBoardAndIsSubscribedTrue(board);
        BoardNotificationDto boardNotificationDto = BoardNotificationDto.of(board, post);

        Notification notification = Notification.of(post.getWriter(), boardNotificationDto.getTitle(), boardNotificationDto.getBody(), NoticeType.BOARD);

        saveNotification(notification);

        userBoardSubscribeList.stream()
                .map(UserBoardSubscribe::getUser)
                .forEach(user -> {
                    user.getFcmTokens().forEach(token -> {
                        send(user, token, boardNotificationDto.getTitle(), boardNotificationDto.getBody());
                    });
                    saveNotificationLog(user, notification);
                });

    }
}
