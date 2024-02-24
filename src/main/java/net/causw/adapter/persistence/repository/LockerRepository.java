package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.locker.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface LockerRepository extends JpaRepository<Locker, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Locker> findById(String id);

    Optional<Locker> findByLockerNumber(Long lockerNumber);

    Optional<Locker> findByUser_Id(String userId);

    List<Locker> findByLocation_IdOrderByLockerNumberAsc(String locationId);


    long countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(String locationId);

    long countByLocationId(String locationId);

}
