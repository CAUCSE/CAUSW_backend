package net.causw.application.chat;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.chat.ChatMessage;
import net.causw.adapter.persistence.chat.ChatRoom;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserBoardSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.chat.ChatMessageDto;
import net.causw.application.dto.notification.BoardNotificationDto;
import net.causw.application.notification.FirebasePushNotificationService;
import net.causw.application.notification.NotificationService;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatNotificationService implements NotificationService {
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserBoardSubscribeRepository userBoardSubscribeRepository;

    public void send(User user, String targetToken, String title, String body) {
        try {
            firebasePushNotificationService.sendNotification(targetToken, title, body);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패: {}, 이유: {}", targetToken, e.getMessage());
            user.getFcmTokens().remove(targetToken);
            log.info("오류 발생으로 FCM 토큰 제거됨: {}", targetToken);
        } catch (Exception e) {
            log.error("FCM 전송 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
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
    public void sendByChatIsSubscribed(ChatRoom room, String content, User sender, User receiver) {
        Notification notification = Notification.of(
                sender,
                room.getRoomName(),
                content,
                NoticeType.CHAT,
                receiver.getId(),
                room.getId()
        );

        saveNotification(notification);

        Set<String> tokens = new HashSet<>(receiver.getFcmTokens());
        tokens.forEach(token -> send(receiver, token, room.getRoomName(), content));
        saveNotificationLog(receiver, notification);
    }
}
