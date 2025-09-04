package net.causw.app.main.repository.uuidFile;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAcademicRecordApplicationAttachImage;

@Repository
public interface UserAcademicRecordApplicationAttachImageRepository
	extends JpaRepository<UserAcademicRecordApplicationAttachImage, Long> {
	List<UserAcademicRecordApplicationAttachImage> findByUserAcademicRecordApplicationId(
		String userAcademicRecordApplicationId);
}
