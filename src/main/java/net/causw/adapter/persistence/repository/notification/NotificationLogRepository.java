package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.notification.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {

    @Query("SELECT nl FROM NotificationLog nl " +
            "JOIN FETCH nl.notification n " +
            "WHERE nl.user = :user " +
            "AND n.noticeType IN :types")
    List<NotificationLog> findByUserAndNotificationTypes(@Param("user") User user, @Param("types") List<NoticeType> types);

    Optional<NotificationLog> findByIdAndUser(String id, User user);
}
