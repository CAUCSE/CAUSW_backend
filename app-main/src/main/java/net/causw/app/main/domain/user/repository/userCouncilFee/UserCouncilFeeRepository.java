package net.causw.app.main.domain.user.repository.userCouncilFee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.entity.user.User;
import net.causw.app.main.domain.user.entity.userCouncilFee.UserCouncilFee;

@Repository
public interface UserCouncilFeeRepository extends JpaRepository<UserCouncilFee, String> {
	Optional<UserCouncilFee> findByUserAndIsRefunded(User user, Boolean isRefunded);

	List<UserCouncilFee> findAllByIsJoinedService(Boolean isJoinedService);

	Optional<UserCouncilFee> findByUser(User user);

	Boolean existsByUser(User targetUser);
}
