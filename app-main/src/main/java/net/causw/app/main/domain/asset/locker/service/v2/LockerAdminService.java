package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerStatus;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockerAdminService {

	private final LockerReader lockerReader;
	private final UserReader userReader;

	@Transactional(readOnly = true)
	public Page<Locker> getLockerList(LockerListCondition condition, Pageable pageable) {
		return lockerReader.findLockerList(
			condition.location(),
			condition.isActive(),
			condition.isOccupied(),
			pageable);
	}

	@Transactional
	public void assignLocker(String lockerId, String userId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		if (LockerStatus.of(locker) != LockerStatus.AVAILABLE) {
			throw LockerErrorCode.LOCKER_NOT_AVAILABLE.toBaseException();
		}

		if (lockerReader.existsByUserId(userId)) {
			throw LockerErrorCode.LOCKER_USER_ALREADY_HAS_LOCKER.toBaseException();
		}

		User user = userReader.findUserById(userId);
		locker.register(user, null);
	}

	@Transactional
	public void extendLocker(String lockerId, LocalDateTime expiredAt) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		if (LockerStatus.of(locker) != LockerStatus.IN_USE) {
			throw LockerErrorCode.LOCKER_NOT_IN_USE.toBaseException();
		}

		locker.extendExpireDate(expiredAt);
	}

	@Transactional
	public void releaseLocker(String lockerId) {
		Locker locker = lockerReader.findByIdForWrite(lockerId);

		if (LockerStatus.of(locker) != LockerStatus.IN_USE) {
			throw LockerErrorCode.LOCKER_NOT_IN_USE.toBaseException();
		}

		locker.returnLocker();
	}
}
