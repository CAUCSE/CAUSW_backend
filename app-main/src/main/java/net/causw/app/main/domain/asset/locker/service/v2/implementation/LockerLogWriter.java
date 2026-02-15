package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;
import net.causw.app.main.domain.asset.locker.repository.LockerLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class LockerLogWriter {

	private final LockerLogRepository lockerLogRepository;

	public void logRegister(Locker locker, User user) {
		save(locker, user.getEmail(), user.getName(), LockerLogAction.REGISTER, "사물함 신청");
	}

	public void logReturn(Locker locker, User user) {
		save(locker, user.getEmail(), user.getName(), LockerLogAction.RETURN, "사물함 반납");
	}

	public void logExtend(Locker locker, User user) {
		save(locker, user.getEmail(), user.getName(), LockerLogAction.EXTEND, "사물함 연장");
	}

	public void logEnable(Locker locker) {
		save(locker, null, null, LockerLogAction.ENABLE, "사물함 활성화");
	}

	public void logDisable(Locker locker) {
		save(locker, null, null, LockerLogAction.DISABLE, "사물함 비활성화");
	}

	private void save(Locker locker, String userEmail, String userName, LockerLogAction action, String message) {
		lockerLogRepository.save(LockerLog.of(
			locker.getLockerNumber(),
			locker.getLocation().getName(),
			userEmail,
			userName,
			action,
			message));
	}
}
