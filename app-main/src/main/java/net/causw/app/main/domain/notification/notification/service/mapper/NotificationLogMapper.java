package net.causw.app.main.domain.notification.notification.service.mapper;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationLogResult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationLogMapper {

	public static NotificationLogResult toResult(NotificationLog log) {
		Notification notification = log.getNotification();
		return new NotificationLogResult(
			log.getId(),
			notification.getTitle(),
			notification.getBody(),
			notification.getNoticeType(),
			notification.getTargetId(),
			notification.getTargetParentId(),
			log.getIsRead(),
			log.getCreatedAt());
	}
}
