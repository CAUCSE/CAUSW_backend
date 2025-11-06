package net.causw.app.main.domain.moving.service.locker;

import java.util.Optional;

import net.causw.app.main.domain.moving.model.entity.locker.Locker;
import net.causw.app.main.domain.moving.service.common.CommonService;
import net.causw.app.main.domain.user.entity.user.User;

public interface LockerAction {
	Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerService lockerService,
		CommonService commonService
	);
}
