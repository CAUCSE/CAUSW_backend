package net.causw.app.main.domain.notification.notification.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationLogReader;
import net.causw.app.main.shared.exception.errorcode.NotificationLogErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

	private final NotificationLogReader notificationLogReader;
	private final NotificationDtoMapper notificationDtoMapper;

	@Transactional(readOnly = true)
	public Page<NotificationResponseDto> getNotificationList(String userId, Pageable pageable) {
		Page<NotificationLog> notificationLog = notificationLogReader.getNotificationList(userId, pageable);

		return notificationLog.map(log -> notificationDtoMapper.toNotificationResponseDto(
			log.getId(),
			log.getNotification(),
			log.getIsRead(),
			log.getCreatedAt()));
	}

	@Transactional(readOnly = true)
	public NotificationResponseDto getLatestUnread(String userId) {
		Optional<NotificationLog> notificationLog = notificationLogReader.getLatestUnread(userId);

		return notificationLog.map(log -> notificationDtoMapper.toNotificationResponseDto(
			log.getId(),
			log.getNotification(),
			log.getIsRead(),
			log.getCreatedAt()))
			.orElse(null);
	}

	@Transactional(readOnly = true)
	public NotificationCountResponseDto getNotificationLogCount(String userId) {
		List<NotificationLog> unreadNotificationLogs = notificationLogReader.findUnreadUpToLimit(userId);

		return new NotificationCountResponseDto(unreadNotificationLogs.size());
	}

	@Transactional
	public void readNotification(String userId, String id) {
		NotificationLog notificationLog = notificationLogReader.findByIdAndUserId(id, userId)
			.orElseThrow(NotificationLogErrorCode.NOTIFICATION_LOG_NOT_FOUND::toBaseException);
		notificationLog.setIsRead(true);
	}

}