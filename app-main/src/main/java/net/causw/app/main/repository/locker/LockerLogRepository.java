package net.causw.app.main.repository.locker;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.locker.LockerLog;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;

@Repository
public interface LockerLogRepository extends JpaRepository<LockerLog, String> {
	List<LockerLog> findByLockerNumber(Long lockerNumber);

	Optional<LockerLog> findTopByUserEmailAndActionOrderByCreatedAtDesc(String userEmail, LockerLogAction action);
}
