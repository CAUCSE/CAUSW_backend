package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouncilFeeRepository extends JpaRepository<UserCouncilFee, String> {
}
