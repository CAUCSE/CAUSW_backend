package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.joinEntity.UserAcademicRecordApplicationAttachImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAcademicRecordApplicationAttachImageRepository extends JpaRepository<UserAcademicRecordApplicationAttachImage, Long> {
    List<UserAcademicRecordApplicationAttachImage> findByUserAcademicRecordApplicationId(String userAcademicRecordApplicationId);
}
