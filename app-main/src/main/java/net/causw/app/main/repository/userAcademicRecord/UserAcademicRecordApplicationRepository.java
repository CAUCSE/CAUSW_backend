package net.causw.app.main.repository.userAcademicRecord;

import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAcademicRecordApplicationRepository extends JpaRepository<UserAcademicRecordApplication, String> {

    Page<UserAcademicRecordApplication> findAllByAcademicRecordRequestStatus(Pageable pageable, AcademicRecordRequestStatus academicRecordRequestStatus);

    List<UserAcademicRecordApplication> findAllByAcademicRecordRequestStatusAndUser(
            AcademicRecordRequestStatus academicRecordRequestStatus,
            User user
    );

    Optional<UserAcademicRecordApplication> findDistinctTopByAcademicRecordRequestStatusAndUserOrderByCreatedAtDesc(
            AcademicRecordRequestStatus academicRecordRequestStatus,
            User user
    );

    List<UserAcademicRecordApplication> findByUserId(String userId);

    List<UserAcademicRecordApplication> findByUserAndAcademicRecordRequestStatus(User user, AcademicRecordRequestStatus academicRecordRequestStatus);
}
