package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LockerRepository extends JpaRepository<Locker, String> {
    Optional<Locker> findByLockerNumber(Long lockerNumber);

    List<Locker> findByLocation_Id(String locationId);

    @Query(value = "SELECT COUNT(l.id) " +
            "FROM TB_LOCKER AS l " +
            "LEFT JOIN TB_USER AS u ON l.id = u.locker_id " +
            "WHERE l.location_id = :location_id AND l.is_active = true AND u.id IS NULL", nativeQuery = true)
    long getEnableLockerCountByLocation(@Param("location_id") String locationId);

    @Query(value = "SELECT COUNT(*) " +
            "FROM TB_LOCKER " +
            "WHERE TB_LOCKER.location_id = :location_id", nativeQuery = true)
    long getLockerCountByLocation(@Param("location_id") String locationId);
}
