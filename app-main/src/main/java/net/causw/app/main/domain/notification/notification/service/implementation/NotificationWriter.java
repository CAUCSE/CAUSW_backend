package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class NotificationWriter {

	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;

	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
	}

	/**
	 * 알림 로그를 저장하고, 저장된 로그의 id를 반환합니다.
	 * 반환된 id는 푸시 알림 data 필드의 notificationLogId 값으로 사용됩니다.
	 */
	public String saveLog(User user, Notification notification) {
		NotificationLog log = notificationLogRepository.save(NotificationLog.of(user, notification));
		return log.getId();
	}

	/**
	 * 여러 유저에 대한 알림 로그를 일괄 저장하고, 유저 id -> 로그 id 맵을 반환합니다.
	 * 반환된 맵은 유저별 푸시 알림 data 필드의 notificationLogId 값으로 사용됩니다.
	 */
	public Map<String, String> saveLogs(List<User> users, Notification notification) {
		List<NotificationLog> logs = users.stream()
			.map(user -> NotificationLog.of(user, notification))
			.toList();
		List<NotificationLog> savedLogs = notificationLogRepository.saveAll(logs);

		return savedLogs.stream()
			.collect(Collectors.toMap(log -> log.getUser().getId(), NotificationLog::getId));
	}
}
