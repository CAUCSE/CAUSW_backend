package net.causw.app.main.domain.notification.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationCountResult;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationLogResult;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationLogReader;
import net.causw.app.main.domain.notification.notification.service.mapper.NotificationLogMapper;
import net.causw.app.main.shared.exception.errorcode.NotificationLogErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

	private final NotificationLogReader notificationLogReader;

	/**
	 * 유저 ID와 읽음 여부로 최근 7일간의 알림 로그를 조회하여 DTO 리스트로 반환합니다.
	 * @param userId 유저 ID
	 * @param isRead 읽음 여부
	 * @return 알림 로그 DTO 리스트
	 */
	@Transactional(readOnly = true)
	public List<NotificationLogResult> getNotificationList(String userId, boolean isRead) {
		List<NotificationLog> notificationLog = notificationLogReader.getNotificationList(userId, isRead,
			LocalDateTime.now());

		return notificationLog.stream()
			.map(NotificationLogMapper::toResult)
			.toList();
	}

	/**
	 * 유저 ID로 가장 최근의 읽지 않은 알림 로그를 조회하여 DTO로 반환합니다.
	 * @param userId 유저 ID
	 * @return 알림 로그 DTO, 읽지 않은 알림이 없는 경우 null
	 */
	@Transactional(readOnly = true)
	public NotificationLogResult getLatestUnread(String userId) {
		Optional<NotificationLog> notificationLog = notificationLogReader.getLatestUnread(userId);

		return notificationLog.map(NotificationLogMapper::toResult)
			.orElse(null);
	}

	/**
	 * 유저 ID로 최근 7일간의 읽지 않은 알림 로그를 최대 20개까지 조회하여 개수를 반환합니다.
	 * @param userId 유저 ID
	 * @return 읽지 않은 알림 로그 개수, 최대 10개까지 카운팅하여 반환 (20개 이상인 경우 20으로 반환)
	 */
	@Transactional(readOnly = true)
	public NotificationCountResult getNotificationLogCount(String userId) {
		List<NotificationLog> unreadNotificationLogs = notificationLogReader.findUnreadUpToLimit(userId,
			LocalDateTime.now());

		return new NotificationCountResult(unreadNotificationLogs.size());
	}

	/**
	 * 유저 ID와 알림 로그 ID로 알림 로그를 조회하여 읽음으로 변경합니다.
	 * @param userId 유저 ID
	 * @param id 알림 로그 ID
	 */
	@Transactional
	public void updateNotificationLogAsRead(String userId, String id) {
		NotificationLog notificationLog = notificationLogReader.findByIdAndUserId(id, userId)
			.orElseThrow(NotificationLogErrorCode.NOTIFICATION_LOG_NOT_FOUND::toBaseException);
		notificationLog.markAsRead();
	}

}