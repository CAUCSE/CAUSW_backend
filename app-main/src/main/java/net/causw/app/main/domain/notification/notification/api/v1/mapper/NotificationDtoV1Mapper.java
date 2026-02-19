package net.causw.app.main.domain.notification.notification.api.v1.mapper;

import java.time.format.DateTimeFormatter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.notification.notification.api.v1.dto.CeremonyListNotificationDto;
import net.causw.app.main.domain.notification.notification.api.v1.dto.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationDtoV1Mapper {

	NotificationDtoV1Mapper INSTANCE = Mappers.getMapper(NotificationDtoV1Mapper.class);

	@Mapping(target = "notificationLogId", source = "notificationLogId")
	@Mapping(target = "title", source = "notification.title")
	@Mapping(target = "body", source = "notification.body")
	@Mapping(target = "noticeType", source = "notification.noticeType")
	@Mapping(target = "targetId", source = "notification.targetId")
	@Mapping(target = "targetParentId", source = "notification.targetParentId")
	@Mapping(target = "isRead", source = "isRead")
	NotificationResponseDto toNotificationResponseDto(String notificationLogId, Notification notification,
		Boolean isRead);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "writer", expression = "java(formatWriter(ceremony))")
	@Mapping(target = "category", source = "ceremonyCategory.label")
	@Mapping(target = "date", expression = "java(formatDateRange(ceremony))")
	@Mapping(target = "description", source = "description")
	@Mapping(target = "createdAt", expression = "java(formatCreatedAt(ceremony))")
	CeremonyListNotificationDto toCeremonyListNotificationDto(Ceremony ceremony);

	default String formatWriter(Ceremony ceremony) {
		return String.format("%s(%s)",
			ceremony.getUser().getName(),
			ceremony.getUser().getAdmissionYear() % 100);
	}

	default String formatDateRange(Ceremony ceremony) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		return String.format("%s ~ %s",
			ceremony.getStartDate().format(formatter),
			ceremony.getEndDate().format(formatter));
	}

	default String formatCreatedAt(Ceremony ceremony) {
		return ceremony.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
	}
}
