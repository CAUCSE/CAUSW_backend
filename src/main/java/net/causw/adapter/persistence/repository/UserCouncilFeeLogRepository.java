package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFeeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserCouncilFeeLogRepository extends JpaRepository<UserCouncilFeeLog, String> {
}
