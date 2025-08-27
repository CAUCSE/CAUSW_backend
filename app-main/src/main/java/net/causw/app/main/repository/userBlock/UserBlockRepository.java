package net.causw.app.main.repository.userBlock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.model.entity.userBlock.UserBlock;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {

	boolean existsByBlockerIdAndBlockeeId(String blockerId, String blockeeId);

	@Query("SELECT b.blockeeId FROM UserBlock b WHERE b.blockerId = :userId")
	List<String> findBlockeeIdsByBlockerUserId(@Param("userId") String userId);
}
