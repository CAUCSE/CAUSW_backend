package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.userCouncilFee.UserCouncilFeeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouncilFeeLogRepository extends JpaRepository<UserCouncilFeeLog, String> {
}
