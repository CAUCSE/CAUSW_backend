package net.causw.adapter.persistence.repository.userAcademicRecord;

import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;
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

}
