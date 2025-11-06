package net.causw.app.main.domain.asset.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAcademicRecordApplicationAttachImage;

@Repository
public interface UserAcademicRecordApplicationAttachImageRepository
	extends JpaRepository<UserAcademicRecordApplicationAttachImage, Long> {
	List<UserAcademicRecordApplicationAttachImage> findByUserAcademicRecordApplicationId(
		String userAcademicRecordApplicationId);
}
