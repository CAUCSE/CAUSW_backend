package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.repository.LockerRepository;
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
}
