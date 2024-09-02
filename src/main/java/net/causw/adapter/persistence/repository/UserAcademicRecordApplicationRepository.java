package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.UserAcademicRecordApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAcademicRecordApplicationRepository extends JpaRepository<UserAcademicRecordApplication, String> {
}
