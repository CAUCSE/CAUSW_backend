package net.causw.app.main.domain.asset.locker.service.v2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.service.v2.dto.LockerListCondition;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockerAdminService {

	private final LockerReader lockerReader;

	@Transactional(readOnly = true)
	public Page<Locker> getLockerList(LockerListCondition condition, Pageable pageable) {
		return lockerReader.findLockerList(
			condition.location(),
			condition.isActive(),
			condition.isOccupied(),
			pageable);
	}
}
