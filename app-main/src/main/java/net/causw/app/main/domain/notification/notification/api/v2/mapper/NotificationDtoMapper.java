package net.causw.app.main.domain.notification.notification.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper INSTANCE = Mappers.getMapper(
		net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper.class);

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