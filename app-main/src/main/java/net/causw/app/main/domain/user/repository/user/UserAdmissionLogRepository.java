package net.causw.app.main.domain.user.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.entity.user.UserAdmissionLog;

@Repository
public interface UserAdmissionLogRepository extends JpaRepository<UserAdmissionLog, String> {
}
