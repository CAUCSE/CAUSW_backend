package net.causw.app.main.domain.notification.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {

	@Query("SELECT nl FROM NotificationLog nl " +
		"JOIN FETCH nl.notification n " +
		"WHERE nl.user.id = :userId " +
		"AND nl.isRead = :isRead " +
		"AND nl.createdAt >= :sevenDaysAgo " +
		"ORDER BY nl.createdAt DESC")
	List<NotificationLog> findRecentNotifications(
		@Param("userId") String userId,
		@Param("isRead") boolean isRead,
		@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

	@Query("SELECT nl FROM NotificationLog nl " +
		"JOIN FETCH nl.notification n " +
		"WHERE nl.user = :user " +
		"AND n.noticeType IN :types " +
		"ORDER BY nl.createdAt DESC")
	Page<NotificationLog> findByUserAndNotificationTypes(@Param("user") User user,
		@Param("types") List<NoticeType> types, Pageable pageable);

	@Query("SELECT nl FROM NotificationLog nl " +
		"JOIN FETCH nl.notification n " +
		"WHERE nl.user = :user " +
		"AND nl.isRead = false " +
		"AND n.noticeType IN :types " +
		"ORDER BY nl.createdAt DESC")
	List<NotificationLog> findByUserAndIsReadFalseNotificationTypes(@Param("user") User user,
		@Param("types") List<NoticeType> types, Pageable pageable);

	@Query("SELECT nl FROM NotificationLog nl " +
		"WHERE nl.user.id = :userId " +
		"AND nl.isRead = false " +
		"ORDER BY nl.createdAt DESC")
	List<NotificationLog> findByUserIdAndIsReadFalseNotification(@Param("userId") String userId, Pageable pageable);

	Optional<NotificationLog> findByIdAndUser(String id, User user);

	Optional<NotificationLog> findByIdAndUserId(String id, String userId);

	@Query("SELECT nl FROM NotificationLog nl " +
		"WHERE nl.user.id = :userId " +
		"AND nl.isRead = false")
	List<NotificationLog> findByUserIdUnreadLogsUpToLimit(@Param("userId") String userId, Pageable pageable);

	@Query("SELECT nl FROM NotificationLog nl " +
		"WHERE nl.user = :user " +
		"AND nl.isRead = false")
	List<NotificationLog> findUnreadLogsUpToLimit(@Param("user") User user, Pageable pageable);

}
