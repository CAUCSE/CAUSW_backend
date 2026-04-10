package net.causw.app.main.domain.notification.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.entity.QNotification;
import net.causw.app.main.domain.notification.notification.entity.QNotificationLog;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationLogQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 알림 로그 ID와 유저 ID로 알림 로그 조회
	 */
	public Optional<NotificationLog> findNotificationLogByIdAndUserId(String id, String userId) {
		QNotificationLog nl = QNotificationLog.notificationLog;

		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(nl)
				.where(
					nl.id.eq(id),
					nl.user.id.eq(userId))
				.fetchOne());
	}

	/**
	 * 유저 ID와 읽음 여부로 최근 7일간의 알림 로그 조회 (notification JOIN FETCH) (v1 알림 제외)
	 */
	public List<NotificationLog> findNotificationLogByUserIdAndIsRead(String userId, boolean isRead,
		LocalDateTime sevenDaysAgo) {
		QNotificationLog nl = QNotificationLog.notificationLog;
		QNotification n = QNotification.notification;

		return jpaQueryFactory
			.selectFrom(nl)
			.join(nl.notification, n).fetchJoin()
			.where(
				nl.user.id.eq(userId),
				nl.isRead.eq(isRead),
				nl.createdAt.goe(sevenDaysAgo),
				n.noticeType.notIn(NoticeType.V1_TYPES))
			.orderBy(nl.createdAt.desc())
			.fetch();
	}

	/**
	 * 유저 ID로 가장 최근의 읽지 않은 알림 로그 단건 조회 (v1 알림 제외)
	 */
	public Optional<NotificationLog> findLatestUnreadByUserId(String userId) {
		QNotificationLog nl = QNotificationLog.notificationLog;
		QNotification n = QNotification.notification;

		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(nl)
				.join(nl.notification, n).fetchJoin()
				.where(
					nl.user.id.eq(userId),
					nl.isRead.isFalse(),
					n.noticeType.notIn(NoticeType.V1_TYPES))
				.orderBy(nl.createdAt.desc())
				.fetchFirst());
	}

	/**
	 * 유저 ID로 최근 7일간의 읽지 않은 알림 개수를 카운트 (v1 알림 제외)
	 */
	public long countRecentUnread(String userId, LocalDateTime sevenDaysAgo) {
		QNotificationLog nl = QNotificationLog.notificationLog;
		QNotification n = QNotification.notification;

		Long count = jpaQueryFactory
			.select(nl.count())
			.from(nl)
			.join(nl.notification, n)
			.where(
				nl.user.id.eq(userId),
				nl.isRead.isFalse(),
				nl.createdAt.goe(sevenDaysAgo),
				n.noticeType.notIn(NoticeType.V1_TYPES))
			.fetchOne();

		// npe 방지 위해 null 체크 후 0L 반환
		return count != null ? count : 0L;
	}

	/**
	 * 유저 ID로 최근 7일간의 읽지 않은 알림 로그를 최대 N개까지 조회 (v1 알림 제외)
	 */
	public List<NotificationLog> findRecentUnreadLogsUpToLimit(String userId, LocalDateTime sevenDaysAgo,
		Pageable pageable) {
		QNotificationLog notificationLog = QNotificationLog.notificationLog;
		QNotification notification = QNotification.notification;

		return jpaQueryFactory
			.selectFrom(notificationLog)
			.join(notificationLog.notification, notification).fetchJoin()
			.where(
				notificationLog.user.id.eq(userId),
				notificationLog.isRead.isFalse(),
				notificationLog.createdAt.goe(sevenDaysAgo),
				notification.noticeType.notIn(NoticeType.V1_TYPES))
			.orderBy(notificationLog.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}
}
