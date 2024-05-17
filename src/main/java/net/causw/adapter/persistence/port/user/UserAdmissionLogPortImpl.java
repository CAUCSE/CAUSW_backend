package net.causw.adapter.persistence.port.user;

import net.causw.adapter.persistence.user.UserAdmissionLog;
import net.causw.adapter.persistence.repository.UserAdmissionLogRepository;
import net.causw.application.spi.UserAdmissionLogPort;
import net.causw.domain.model.enums.UserAdmissionLogAction;
import org.springframework.stereotype.Component;

@Component
public class UserAdmissionLogPortImpl implements UserAdmissionLogPort {
    private final UserAdmissionLogRepository userAdmissionLogRepository;

    public UserAdmissionLogPortImpl(UserAdmissionLogRepository userAdmissionLogRepository) {
        this.userAdmissionLogRepository = userAdmissionLogRepository;
    }

    @Override
    public void create(String userEmail, String userName, String adminUserEmail, String adminUserName, UserAdmissionLogAction action, String attachImage, String description) {
        this.userAdmissionLogRepository.save(
                UserAdmissionLog.of(
                        userEmail,
                        userName,
                        adminUserEmail,
                        adminUserName,
                        action,
                        attachImage,
                        description
                )
        );
    }
}
