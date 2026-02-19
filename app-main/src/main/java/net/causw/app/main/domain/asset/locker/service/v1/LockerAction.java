package net.causw.app.main.domain.asset.locker.service.v1;

import java.util.Optional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.etc.textfield.service.v1.CommonService;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface LockerAction {
	Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerV1Service lockerV1Service,
		CommonService commonService);
}
