package net.causw.application.notification;


import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.repository.notification.CeremonyNotificationSettingRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.CeremonyNotificationDto;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CeremonyNotificationService implements NotificationService {
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
    private final NotificationRepository notificationRepository;
    @Override
    public void send(String targetToken, String title, String body) {
        firebasePushNotificationService.sendNotification(targetToken, title, body);
    }

    @Override
    public void save(String title, String body, User user) {
        notificationRepository.save(Notification.of(user, title, body, NoticeType.CEREMONY));
    }

    public void sendByAdmissionYear(Integer admissionYear, Ceremony ceremony) {
        List<CeremonyNotificationSetting> ceremonyNotificationSettings = ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(admissionYear);
        CeremonyNotificationDto ceremonyNotificationDto = CeremonyNotificationDto.of(ceremony);

        //save로 해당 알림의 제목, 내용, 알림의 신청자(경조사 신청자를 저장)
        save(ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody(), ceremony.getUser());

        //푸시알림은 별도로 구독 년도나 isSetAll 여부를 가지고 전송
        ceremonyNotificationSettings.stream()
                .map(CeremonyNotificationSetting::getUser)
                .forEach(user -> {
                    String targetToken = user.getFcmToken();
                    send(targetToken, ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody());
                });
    }

}
