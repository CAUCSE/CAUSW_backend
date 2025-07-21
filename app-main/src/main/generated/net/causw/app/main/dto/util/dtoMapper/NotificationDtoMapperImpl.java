package net.causw.app.main.dto.util.dtoMapper;

import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.dto.notification.NotificationResponseDto;
import net.causw.app.main.dto.notification.NotificationResponseDto.NotificationResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class NotificationDtoMapperImpl implements NotificationDtoMapper {

    @Override
    public NotificationResponseDto toNotificationResponseDto(String notificationLogId, Notification notification, Boolean isRead) {
        if ( notificationLogId == null && notification == null && isRead == null ) {
            return null;
        }

        NotificationResponseDtoBuilder notificationResponseDto = NotificationResponseDto.builder();

        if ( notificationLogId != null ) {
            notificationResponseDto.notificationLogId( notificationLogId );
        }
        if ( notification != null ) {
            notificationResponseDto.title( notification.getTitle() );
            notificationResponseDto.body( notification.getBody() );
            notificationResponseDto.noticeType( notification.getNoticeType() );
            notificationResponseDto.targetId( notification.getTargetId() );
            notificationResponseDto.targetParentId( notification.getTargetParentId() );
        }
        if ( isRead != null ) {
            notificationResponseDto.isRead( isRead );
        }

        return notificationResponseDto.build();
    }
}
