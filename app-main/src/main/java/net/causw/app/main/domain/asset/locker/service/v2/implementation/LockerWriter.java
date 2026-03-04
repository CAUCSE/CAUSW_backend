package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.repository.LockerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockerWriter {

	private final LockerRepository lockerRepository;

	public Locker returnLocker(Locker locker) {
		locker.returnLocker();
		return lockerRepository.save(locker);
	}
}
