package net.causw.app.main.repository.user;

import net.causw.app.main.domain.model.entity.user.UserAdmissionLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAdmissionLogRepository extends JpaRepository<UserAdmissionLog, String> {
}
