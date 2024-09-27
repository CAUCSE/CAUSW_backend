package net.causw.adapter.persistence.repository.userAcademicRecord;

import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAcademicRecordLogRepository extends JpaRepository<UserAcademicRecordLog, String> {

    List<UserAcademicRecordLog> findAllByTargetUserStudentIdAndTargetUserEmailAndTargetUserName(String targetUserStudentId, String targetUserEmail, String targetUserName);

}
