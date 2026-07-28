package net.causw.app.main.domain.notification.notification.service.dto;

import net.causw.app.main.domain.notification.notification.enums.NoticeType;

public record PushNotificationData(
	NoticeType noticeType,
	String targetId,
	String targetParentId) {
}
