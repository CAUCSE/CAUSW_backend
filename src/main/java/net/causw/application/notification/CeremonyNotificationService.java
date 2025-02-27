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
        notificationRepository.save(Notification.of(user, title, body, NoticeType.CEREMONY,false));
    }

    public void sendByAdmissionYear(Integer admissionYear, Ceremony ceremony) {
        List<CeremonyNotificationSetting> ceremonyNotificationSettings = ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(admissionYear);
        CeremonyNotificationDto ceremonyNotificationDto = CeremonyNotificationDto.of(ceremony);

        ceremonyNotificationSettings.stream()
                .map(CeremonyNotificationSetting::getUser)
                .forEach(user -> {
                    String targetToken = user.getFcmToken();
                    send(targetToken, ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody());
                    save(ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody(), user);
                });
    }

}
