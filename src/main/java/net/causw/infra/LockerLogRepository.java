package net.causw.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerLogRepository extends JpaRepository<LockerLog, String> {
}
