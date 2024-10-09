package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query(
            value = "SELECT * " +
                    "FROM tb_notification " +
                    "WHERE user_id = :userId OR is_global = true " +
                    "ORDER BY created_at DESC " +
                    "LIMIT 4", nativeQuery = true)
    List<Notification> findUserNotice(@Param("userId") String userId);
}
