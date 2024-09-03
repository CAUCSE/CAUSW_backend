package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.UserAcademicRecordLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAcademicRecordApplicationLogRepository extends JpaRepository<UserAcademicRecordLog, String> {
}
