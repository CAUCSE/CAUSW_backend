package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.repository.notification.NotificationLogRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.dto.util.dtoMapper.NotificationDtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.notification.NoticeType;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationLogService {
    private final NotificationLogRepository notificationLogRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getCeremonyNotification(User user) {
        List<NoticeType> types = Arrays.asList(NoticeType.CEREMONY);
        List<NotificationLog> notificationLogs = notificationLogRepository.findByUserAndNotificationTypes(user, types);

        return notificationLogs.stream()
                .map(log -> NotificationDtoMapper.INSTANCE.toNotificationResponseDto(log.getId(), log.getNotification(), log.getIsRead()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getGeneralNotification(User user) {
        List<NoticeType> types = Arrays.asList(NoticeType.BOARD, NoticeType.POST, NoticeType.COMMENT);
        List<NotificationLog> notificationLogs = notificationLogRepository.findByUserAndNotificationTypes(user, types);

        return notificationLogs.stream()
                .map(log -> NotificationDtoMapper.INSTANCE.toNotificationResponseDto(log.getId(), log.getNotification(), log.getIsRead()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void readNotification(User user, String id){
        NotificationLog notificationLog = notificationLogRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.NOTIFICATION_LOG_NOT_FOUND
                )
        );
        notificationLog.setIsRead(true);
    }


}
