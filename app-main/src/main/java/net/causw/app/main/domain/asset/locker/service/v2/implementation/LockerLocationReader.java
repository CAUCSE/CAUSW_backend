package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.repository.LockerLocationRepository;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockerLocationReader {

	private final LockerLocationRepository lockerLocationRepository;

	public LockerLocation findById(String locationId) {
		return lockerLocationRepository.findById(locationId)
			.orElseThrow(LockerErrorCode.LOCKER_NOT_FOUND::toBaseException);
	}

	public List<LockerLocation> findAll() {
		return lockerLocationRepository.findAllOrderByCreatedAt();
	}
}
