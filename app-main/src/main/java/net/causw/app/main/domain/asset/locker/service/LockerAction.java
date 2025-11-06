package net.causw.app.main.domain.asset.locker.service;

import java.util.Optional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.etc.textField.service.CommonService;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface LockerAction {
	Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerService lockerService,
		CommonService commonService
	);
}
