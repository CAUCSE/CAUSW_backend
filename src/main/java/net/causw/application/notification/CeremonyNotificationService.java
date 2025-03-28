package net.causw.application.notification;


import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.repository.notification.CeremonyNotificationSettingRepository;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.CeremonyNotificationDto;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CeremonyNotificationService implements NotificationService {
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
    @Override
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
    public void sendByAdmissionYear(Integer admissionYear, Ceremony ceremony) {
        List<CeremonyNotificationSetting> ceremonyNotificationSettings = ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(admissionYear);
        CeremonyNotificationDto ceremonyNotificationDto = CeremonyNotificationDto.of(ceremony);

        Notification notification = Notification.of(ceremony.getUser(), ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody(), NoticeType.CEREMONY, ceremony.getId());

        saveNotification(notification);

        ceremonyNotificationSettings.stream()
                .map(CeremonyNotificationSetting::getUser)
                .forEach(user -> {
                    Set<String> copy = new HashSet<>(user.getFcmTokens());
                    copy.forEach(token -> send(user, token, ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody()));
                    saveNotificationLog(user, notification);
                });
    }



}
