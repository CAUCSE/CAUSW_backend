package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.domain.model.enums.LockerLogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LockerLogRepository extends JpaRepository<LockerLog, String> {
    List<LockerLog> findByLockerNumber(Long lockerNumber);

    Optional<LockerLog> findTopByUserEmailAndActionOrderByCreatedAtDesc(String userEmail, LockerLogAction action);
}
