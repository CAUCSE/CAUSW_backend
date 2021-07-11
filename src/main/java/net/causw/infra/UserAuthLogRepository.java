package net.causw.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthLogRepository extends JpaRepository<UserAuthLog, String> {
}
