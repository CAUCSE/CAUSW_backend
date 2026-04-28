package net.causw.app.main.domain.asset.file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;

@Repository
public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {

	@EntityGraph(attributePaths = {"uuidFile"})
	Optional<UserProfileImage> findByUserId(String userId);

	@EntityGraph(attributePaths = {"uuidFile"})
	List<UserProfileImage> findByUserIdIn(List<String> userIds);

	void deleteByUserId(String userId);
}
