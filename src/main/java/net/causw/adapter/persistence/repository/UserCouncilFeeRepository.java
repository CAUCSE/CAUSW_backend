package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCouncilFeeRepository extends JpaRepository<UserCouncilFee, String> {
    Optional<UserCouncilFee> findByUserAndIsRefunded(User user, Boolean isRefunded);
}
