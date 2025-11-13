package net.causw.app.main.domain.asset.locker.service;

import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.etc.textfield.service.CommonService;
import net.causw.app.main.domain.asset.locker.util.LockerIsDeactivatedValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.util.UserRoleValidator;
import net.causw.app.main.shared.ValidatorBucket;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LockerActionDisable implements LockerAction {
	@Override
	public Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerService lockerService,
		CommonService commonService
	) {
		ValidatorBucket.of()
			.consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
			.consistOf(LockerIsDeactivatedValidator.of(locker.getIsActive()))
			.validate();

		locker.deactivate();
		return Optional.of(locker);
	}
}
