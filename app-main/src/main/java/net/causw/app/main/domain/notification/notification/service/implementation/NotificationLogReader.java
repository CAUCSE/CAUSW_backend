package net.causw.app.main.domain.notification.notification.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationLogReader {
	private final NotificationLogRepository notificationLogRepository;
	private final PageableFactory pageableFactory;

	/**
	 * 알림 로그 ID와 유저 ID로 알림 로그 조회
	 * @param id 알림 로그 ID
	 * @param userId 유저 ID
	 * @return 알림 로그 Optional
	 */
	public Optional<NotificationLog> findByIdAndUserId(String id, String userId) {
		return notificationLogRepository.findByIdAndUserId(id, userId);
	}

	/**
	 * 유저 ID와 읽음 여부로 최근 7일간의 알림 로그 조회
	 * @param userId 유저 ID
	 * @param isRead 읽음 여부
	 * @param currentTime 현재 시간
	 * @return 알림 로그 리스트
	 */
	public List<NotificationLog> getNotificationList(String userId, boolean isRead, LocalDateTime currentTime) {
		LocalDateTime sevenDaysAgo = currentTime.minusDays(7);

		return notificationLogRepository.findRecentNotifications(userId, isRead, sevenDaysAgo);
	}

	/**
	 * 유저 ID로 가장 최근의 읽지 않은 알림 로그 조회
	 * @param userId 유저 ID
	 * @return 알림 로그 Optional
	 */
	public Optional<NotificationLog> getLatestUnread(String userId) {

		List<NotificationLog> notificationLog = notificationLogRepository.findByUserIdAndIsReadFalseNotification(
			userId, pageableFactory.create(0, StaticValue.HOME_NOTIFICATION_PAGE_SIZE));

		return notificationLog.stream().findFirst();
	}

	/**
	 * 유저 ID로 최근 7일간의 읽지 않은 알림 로그를 최대 20개까지 조회
	 * @param userId 유저 ID
	 * @param currentTime 현재 시간
	 * @return 알림 로그 리스트
	 */
	public List<NotificationLog> findUnreadUpToLimit(String userId, LocalDateTime currentTime) {
		LocalDateTime sevenDaysAgo = currentTime.minusDays(7);

		return notificationLogRepository.findRecentUnreadLogsUpToLimit(
			userId, sevenDaysAgo, pageableFactory.create(0, StaticValue.MAX_NOTIFICATION_COUNT));
	}
}