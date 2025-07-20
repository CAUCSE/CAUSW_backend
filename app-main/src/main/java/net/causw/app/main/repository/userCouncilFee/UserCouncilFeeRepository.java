package net.causw.app.main.repository.userCouncilFee;

import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouncilFeeRepository extends JpaRepository<UserCouncilFee, String> {
    Optional<UserCouncilFee> findByUserAndIsRefunded(User user, Boolean isRefunded);

    List<UserCouncilFee> findAllByIsJoinedService(Boolean isJoinedService);

    Optional<UserCouncilFee> findByUser(User user);

    Boolean existsByUser(User targetUser);
}
