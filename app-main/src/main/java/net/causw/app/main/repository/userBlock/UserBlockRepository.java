package net.causw.app.main.repository.userBlock;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.userBlock.UserBlock;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {

	boolean existsByBlockerIdAndBlockeeId(String blockerId, String blockeeId);
}
