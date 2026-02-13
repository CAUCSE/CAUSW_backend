package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.repository.query.LockerQueryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerReader {

	private final LockerQueryRepository lockerQueryRepository;

	public Page<Locker> findLockerList(LockerName location, Boolean isActive, Boolean isOccupied, Pageable pageable) {
		return lockerQueryRepository.findLockerList(location, isActive, isOccupied, pageable);
	}
}
