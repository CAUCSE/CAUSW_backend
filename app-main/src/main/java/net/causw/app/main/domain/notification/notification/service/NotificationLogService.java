package net.causw.app.main.domain.notification.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.response.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogService {
	private final NotificationLogRepository notificationLogRepository;
	private final PageableFactory pageableFactory;

	@Transactional(readOnly = true)
	public NotificationResponseDto getNotificationTop1(User user) {
		List<NotificationLog> notificationLogs = notificationLogRepository.findByUserAndIsReadFalseNotification(
			user, pageableFactory.create(0, StaticValue.HOME_NOTIFICATION_PAGE_SIZE));

		return notificationLogs.stream()
			.findFirst()
			.map(log -> NotificationDtoMapper.INSTANCE.toNotificationResponseDto(log.getId(), log.getNotification(),
				log.getIsRead()))
			.orElse(null);
	}

	@Transactional(readOnly = true)
	public NotificationCountResponseDto getNotificationLogCount(User user) {
		List<NotificationLog> unreadNotificationLogs = notificationLogRepository.findUnreadLogsUpToLimit(user,
			pageableFactory.create(0, StaticValue.MAX_NOTIFICATION_COUNT));

		return NotificationCountResponseDto.builder()
			.notificationLogCount(unreadNotificationLogs.size())
			.build();
	}

}