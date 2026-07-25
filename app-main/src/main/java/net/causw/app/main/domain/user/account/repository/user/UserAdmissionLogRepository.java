package net.causw.app.main.domain.user.account.repository.user;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.UserAdmissionLog;
import net.causw.app.main.domain.user.account.enums.user.UserAdmissionLogAction;

@Repository
public interface UserAdmissionLogRepository extends JpaRepository<UserAdmissionLog, String> {

	Long countByActionAndCreatedAtBetween(UserAdmissionLogAction action, LocalDateTime start, LocalDateTime end);
}
