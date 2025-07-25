package net.causw.app.main.repository.userAcademicRecord;

import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAcademicRecordLogRepository extends JpaRepository<UserAcademicRecordLog, String> {

    List<UserAcademicRecordLog> findAllByTargetUserStudentIdAndTargetUserEmailAndTargetUserName(String targetUserStudentId, String targetUserEmail, String targetUserName);

}
