package net.causw.app.main.repository.userCouncilFee;

import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFeeLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouncilFeeLogRepository extends JpaRepository<UserCouncilFeeLog, String> {
}
