package net.causw.application.spi;

import net.causw.domain.model.UserAdmissionLogAction;

public interface UserAdmissionLogPort {
    void create(String userEmail, String userName, String adminUserEmail, String adminUserName, UserAdmissionLogAction action, String attachImage, String description);
}
