package net.causw.application.notification;

import net.causw.adapter.persistence.user.User;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void send(String targetToken, String title, String body);
    void save(String title , String body, User user);
}
