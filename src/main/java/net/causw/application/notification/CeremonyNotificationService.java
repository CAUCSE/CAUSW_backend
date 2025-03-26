package net.causw.application.notification;


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

import java.util.List;

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
    public void sendByAdmissionYear(Integer admissionYear, Ceremony ceremony) {
        List<CeremonyNotificationSetting> ceremonyNotificationSettings = ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(admissionYear);
        CeremonyNotificationDto ceremonyNotificationDto = CeremonyNotificationDto.of(ceremony);

        //save로 해당 알림의 제목, 내용, 알림의 신청자(경조사 신청자를 저장)
        Notification notification = Notification.of(ceremony.getUser(), ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody(), NoticeType.CEREMONY);

        //공지 저장
        saveNotification(notification);

        //푸시알림은 별도로 구독 년도나 isSetAll 여부를 가지고 전송
        ceremonyNotificationSettings.stream()
                .map(CeremonyNotificationSetting::getUser)
                .forEach(user -> {
                    user.getFcmTokens().forEach(token -> {
                        send(user, token, ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody());
                    });
                    saveNotificationLog(user, notification);
                });
    }



}
