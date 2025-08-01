package net.causw.app.main.dto.util.dtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.dto.notification.NotificationResponseDto;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	NotificationDtoMapper INSTANCE = Mappers.getMapper(NotificationDtoMapper.class);

	@Mapping(target = "notificationLogId", source = "notificationLogId")
	@Mapping(target = "title", source = "notification.title")
	@Mapping(target = "body", source = "notification.body")
	@Mapping(target = "noticeType", source = "notification.noticeType")
	@Mapping(target = "targetId", source = "notification.targetId")
	@Mapping(target = "targetParentId", source = "notification.targetParentId")
	@Mapping(target = "isRead", source = "isRead")
	NotificationResponseDto toNotificationResponseDto(String notificationLogId, Notification notification,
		Boolean isRead);

}
