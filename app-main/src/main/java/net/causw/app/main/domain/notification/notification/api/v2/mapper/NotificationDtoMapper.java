package net.causw.app.main.domain.notification.notification.api.v2.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationCountResult;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationLogResult;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	NotificationResponseDto toResponse(NotificationLogResult result);

	NotificationCountResponseDto toCountResponse(NotificationCountResult result);

	@Mapping(target = "notificationLogId", source = "notificationLogId")
	@Mapping(target = "title", source = "notification.title")
	@Mapping(target = "body", source = "notification.body")
	@Mapping(target = "noticeType", source = "notification.noticeType")
	@Mapping(target = "targetId", source = "notification.targetId")
	@Mapping(target = "targetParentId", source = "notification.targetParentId")
	@Mapping(target = "isRead", source = "isRead")
	@Mapping(target = "createdAt", source = "createdAt")
	NotificationResponseDto toNotificationResponseDto(String notificationLogId, Notification notification,
		Boolean isRead, LocalDateTime createdAt);
}