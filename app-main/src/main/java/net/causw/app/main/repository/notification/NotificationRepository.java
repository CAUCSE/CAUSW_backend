package net.causw.app.main.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.notification.NoticeType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
	@Query(
		value = "SELECT * " +
			"FROM tb_notification " +
			"WHERE user_id = :userId OR is_global = true " +
			"ORDER BY created_at DESC " +
			"LIMIT 4", nativeQuery = true)
	List<Notification> findUserNotice(@Param("userId") String userId);

	List<Notification> findByUserAndNoticeTypeIn(User user, List<NoticeType> noticeTypes);

	List<Notification> findByNoticeTypeIn(List<NoticeType> noticeTypes);
}
