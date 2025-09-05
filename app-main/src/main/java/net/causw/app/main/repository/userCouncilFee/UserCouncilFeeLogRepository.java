package net.causw.app.main.repository.userCouncilFee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFeeLog;

@Repository
public interface UserCouncilFeeLogRepository extends JpaRepository<UserCouncilFeeLog, String> {
}
