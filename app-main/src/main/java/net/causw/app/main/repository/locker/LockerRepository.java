package net.causw.app.main.repository.locker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.locker.Locker;

import jakarta.persistence.LockModeType;

@Repository
public interface LockerRepository extends JpaRepository<Locker, String> {

	@Query("SELECT l FROM Locker l WHERE l.id = :id")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Locker> findByIdForWrite(@Param("id") String id);

	@Query("SELECT l FROM Locker l WHERE l.id = :id")
	@Lock(LockModeType.PESSIMISTIC_READ)
	Optional<Locker> findByIdForRead(@Param("id") String id);

	Optional<Locker> findByLockerNumber(Long lockerNumber);

	Optional<Locker> findByUser_Id(String userId);

	List<Locker> findByLocation_IdOrderByLockerNumberAsc(String locationId);

	long countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(String locationId);

	long countByLocationId(String locationId);

	List<Locker> findAllByExpireDateBeforeAndUserIsNotNull(LocalDateTime expireDate);

}
