package net.causw.app.main.service.locker;

import java.util.Optional;

import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.service.common.CommonService;

public interface LockerAction {
	Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerService lockerService,
		CommonService commonService
	);
}
