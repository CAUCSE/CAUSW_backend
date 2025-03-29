package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.notification.Notification;
import net.causw.application.dto.notification.NotificationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

    NotificationDtoMapper INSTANCE = Mappers.getMapper(NotificationDtoMapper.class);

    @Mapping(target = "notificationLogId", source = "notificationLogId")
    @Mapping(target = "title", source = "notification.title")
    @Mapping(target = "body", source = "notification.body")
    @Mapping(target = "noticeType", source = "notification.noticeType")
    @Mapping(target = "targetId", source = "notification.targetId")
    @Mapping(target = "isRead", source = "isRead")
    NotificationResponseDto toNotificationResponseDto(String notificationLogId, Notification notification, Boolean isRead);



}
