package net.causw.app.main.domain.finance.usercouncilfee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.finance.usercouncilfee.entity.UserCouncilFeeLog;

@Repository
public interface UserCouncilFeeLogRepository extends JpaRepository<UserCouncilFeeLog, String> {
}
