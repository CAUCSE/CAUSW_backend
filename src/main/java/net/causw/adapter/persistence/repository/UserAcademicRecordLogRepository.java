package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAcademicRecordLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAcademicRecordLogRepository extends JpaRepository<UserAcademicRecordLog, String> {

    List<UserAcademicRecordLog> findAllByTargetUser(User user);

}
