package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.UserAdmissionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAdmissionLogRepository extends JpaRepository<UserAdmissionLog, String> {
}
