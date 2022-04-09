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

    Optional<Locker> findByUser_Id(String userId);

    List<Locker> findByLocation_IdOrderByLockerNumberAsc(String locationId);

    @Query(value = "SELECT COUNT(*) " +
            "FROM TB_LOCKER  " +
            "WHERE location_id = :location_id AND is_active = true AND user_id IS NULL", nativeQuery = true)
    long countEnableLockerByLocation(@Param("location_id") String locationId);

    @Query(value = "SELECT COUNT(*) " +
            "FROM TB_LOCKER " +
            "WHERE TB_LOCKER.location_id = :location_id", nativeQuery = true)
    long countByLocation(@Param("location_id") String locationId);
}
