package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.notification.NotificationLog;
import net.causw.adapter.persistence.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {

    List<NotificationLog> findByUserAndNotificationIn(User user, List<Notification> notification);

}
