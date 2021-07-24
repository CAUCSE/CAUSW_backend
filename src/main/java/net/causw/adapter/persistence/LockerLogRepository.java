package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LockerLogRepository extends JpaRepository<LockerLog, String> {
    List<LockerLog> findByLockerNumber(Long lockerNumber);
}
