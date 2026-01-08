package net.causw.app.main.domain.user.academic.repository.userAcademicRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordLog;

@Repository
public interface UserAcademicRecordLogRepository extends JpaRepository<UserAcademicRecordLog, String> {

	List<UserAcademicRecordLog> findAllByTargetUserEmailAndTargetUserName(String targetUserEmail,
		String targetUserName);

}
