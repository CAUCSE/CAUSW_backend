package net.causw.app.main.domain.user.relation.relation.userBlock;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.user.relation.entity.userBlock.UserBlock;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {

	boolean existsByBlockerIdAndBlockeeId(String blockerId, String blockeeId);

	@Query("SELECT b.blockeeId FROM UserBlock b WHERE b.blockerId = :userId")
	Set<String> findBlockeeIdsByBlockerUserId(@Param("userId") String userId);

	@Query("SELECT b.blockerId FROM UserBlock b WHERE b.blockeeId = :userId")
	Set<String> findBlockerIdsByBlockeeUserId(@Param("userId") String userId);

	@Query("SELECT b.blockerId FROM UserBlock b WHERE b.blockeeId IN :userIds")
	Set<String> findBlockerIdsByBlockeeUserIds(Set<String> userIds);
}
