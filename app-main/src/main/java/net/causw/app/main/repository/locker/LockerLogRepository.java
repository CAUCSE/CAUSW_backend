package net.causw.app.main.repository.locker;

import net.causw.app.main.domain.model.entity.locker.LockerLog;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LockerLogRepository extends JpaRepository<LockerLog, String> {
    List<LockerLog> findByLockerNumber(Long lockerNumber);

    Optional<LockerLog> findTopByUserEmailAndActionOrderByCreatedAtDesc(String userEmail, LockerLogAction action);
}
