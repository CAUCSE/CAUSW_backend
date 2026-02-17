package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.repository.LockerRepository;
import net.causw.app.main.domain.asset.locker.repository.dto.LockerCountByLocation;
import net.causw.app.main.domain.asset.locker.repository.query.LockerQueryRepository;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockerReader {

	private final LockerRepository lockerRepository;
	private final LockerQueryRepository lockerQueryRepository;

	public Locker findByIdForWrite(String lockerId) {
		return lockerRepository.findByIdForWrite(lockerId)
			.orElseThrow(LockerErrorCode.LOCKER_NOT_FOUND::toBaseException);
	}

	public boolean existsByUserId(String userId) {
		return lockerRepository.findByUser_Id(userId).isPresent();
	}

	public Optional<Locker> findByUserId(String userId) {
		return lockerRepository.findByUser_Id(userId);
	}

	public List<Locker> findByLocationId(String locationId) {
		return lockerRepository.findByLocation_IdOrderByLockerNumberAsc(locationId);
	}

	public List<Locker> findByLocationIdWithUser(String locationId) {
		return lockerRepository.findByLocationIdWithUser(locationId);
	}

	public Page<Locker> findLockerList(String userKeyword, LockerName location, Boolean isActive, Boolean isOccupied,
		Boolean isExpired, Pageable pageable) {
		return lockerQueryRepository.findLockers(userKeyword, location, isActive, isOccupied, isExpired, pageable);
	}

	public List<Locker> findExpiredLockers(LocalDateTime targetTime) {
		return lockerQueryRepository.findAllExpiredLockers(targetTime);
	}

	public long countByLocationId(String locationId) {
		return lockerRepository.countByLocationId(locationId);
	}

	public long countAvailableByLocationId(String locationId) {
		return lockerRepository.countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(locationId);
	}

	/**
	 * location별 전체 사물함 수와 사용 가능 사물함 수를 한 번에 조회한다.
	 *
	 * @return locationId → LockerCountByLocation
	 */
	public Map<String, LockerCountByLocation> countGroupByLocation() {
		// key: locationId, value: LockerCountByLocation 그대로 쓰는 것 (identity)
		return lockerRepository.countGroupByLocation().stream()
			.collect(Collectors.toMap(LockerCountByLocation::locationId, Function.identity()));
	}
}
