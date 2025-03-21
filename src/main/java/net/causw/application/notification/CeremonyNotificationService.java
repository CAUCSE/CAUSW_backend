package net.causw.application.notification;


import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CeremonyNotificationService implements NotificationService {
    private final FirebasePushNotificationService firebasePushNotificationService;
    private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;
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

    public void sendByAdmissionYear(Integer admissionYear, Ceremony ceremony) {
        List<CeremonyNotificationSetting> ceremonyNotificationSettings = ceremonyNotificationSettingRepository.findByAdmissionYearOrSetAll(admissionYear);
        CeremonyNotificationDto ceremonyNotificationDto = CeremonyNotificationDto.of(ceremony);

        //save로 해당 알림의 제목, 내용, 알림의 신청자(경조사 신청자를 저장)
        Notification notification = Notification.of(ceremony.getUser(), ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody(), NoticeType.CEREMONY);

        //공지 저장
        saveNotification(notification);

        //푸시알림은 별도로 구독 년도나 isSetAll 여부를 가지고 전송
        //근데 여기서 이제 푸시알림을 보낼때 notificationLog테이블에도 내가 보낼 애들에 대해서 저장을 해야함
        ceremonyNotificationSettings.stream()
                .map(CeremonyNotificationSetting::getUser)
                .forEach(user -> {
//                    String targetToken = user.getFcmToken();
//                    send(targetToken, ceremonyNotificationDto.getTitle(), ceremonyNotificationDto.getBody());
                    //공지를 받는 사람의 매핑테이블 저장
                    saveNotificationLog(user, notification);
                });
    }



}
