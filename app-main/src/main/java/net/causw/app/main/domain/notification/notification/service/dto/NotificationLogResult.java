package net.causw.app.main.domain.notification.notification.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.notification.notification.enums.NoticeType;

/**
 * 알림 로그 단건 조회 결과 (Service 계층).
 */
public record NotificationLogResult(
	String notificationLogId,
	String title,
	String body,
	NoticeType noticeType,
	String targetId,
	String targetParentId,
	Boolean isRead,
	LocalDateTime createdAt) {
}
