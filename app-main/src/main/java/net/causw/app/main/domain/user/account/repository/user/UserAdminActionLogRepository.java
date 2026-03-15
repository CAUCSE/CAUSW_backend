package net.causw.app.main.domain.user.account.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.UserAdminActionLog;

@Repository
public interface UserAdminActionLogRepository extends JpaRepository<UserAdminActionLog, String> {}
