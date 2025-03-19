package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.dto.util.dtoMapper.NotificationDtoMapper;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationLogService {
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;

    //여기서 알람 로그를 가져와야됨
    //당장은 notificationRepository인데 이걸 매핑테이블을 만들어서 notificationLog
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getCeremonyNotification(User user) {
        //
        List<NoticeType> noticeTypes = Arrays.asList(NoticeType.CEREMONY);
        List<Notification> notifications = notificationRepository.findByNoticeTypeIn(noticeTypes);

        //여기서 현재 사용자한테 보내졌던 알람들을 찾음
        List<NotificationLog> notificationLogs = notificationLogRepository.findByUserAndNotificationIn(user, notifications);

        //그럼 여기선 그 각각의 알람들을 반환함
        return notificationLogs.stream()
                .map(log -> NotificationDtoMapper.INSTANCE.toNotificationResponseDto(log.getNotification()))
                .collect(Collectors.toList());
    }

    //알람을 조회하는 각각의 조건이 필요할거 같은데 아니 걍 ㄹㅇ 깡으로 다 저장해?? 그게 맞아?
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getGeneralNotification(User user) {
        List<NoticeType> noticeTypes = Arrays.asList(NoticeType.BOARD, NoticeType.POST, NoticeType.COMMENT);
        List<Notification> notifications = notificationRepository.findByNoticeTypeIn(noticeTypes);

        //여기서 현재 사용자한테 보내졌던 알람들을 찾음
        List<NotificationLog> notificationLogs = notificationLogRepository.findByUserAndNotificationIn(user, notifications);

        //그럼 여기선 그 각각의 알람들을 반환함
        return notificationLogs.stream()
                .map(log -> NotificationDtoMapper.INSTANCE.toNotificationResponseDto(log.getNotification()))
                .collect(Collectors.toList());
    }
}
