package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.joinEntity.UserAcademicRecordApplicationAttachImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAcademicRecordApplicationAttachImageRepository extends JpaRepository<UserAcademicRecordApplicationAttachImage, Long> {
}
