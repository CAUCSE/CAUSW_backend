package net.causw.app.main.domain.notification.notification.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationLogReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

	private final NotificationLogReader notificationLogReader;
	private final NotificationDtoMapper notificationDtoMapper;

	@Transactional(readOnly = true)
	public NotificationResponseDto getLatestUnread(String userId) {
		Optional<NotificationLog> notificationLog = notificationLogReader.getLatestUnread(userId);

		return notificationLog.map(log -> notificationDtoMapper.toNotificationResponseDto(
			log.getId(),
			log.getNotification(),
			log.getIsRead()))
			.orElse(null);
	}

	@Transactional(readOnly = true)
	public NotificationCountResponseDto getNotificationLogCount(String userId) {
		List<NotificationLog> unreadNotificationLogs = notificationLogReader.findUnreadUpToLimit(userId);

		return new NotificationCountResponseDto(unreadNotificationLogs.size());
	}

}