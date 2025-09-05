package net.causw.app.main.service.locker;

import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.validation.LockerIsActiveValidator;
import net.causw.app.main.domain.validation.UserRoleValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.service.common.CommonService;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LockerActionEnable implements LockerAction {
	@Override
	public Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerService lockerService,
		CommonService commonService
	) {
		ValidatorBucket.of()
			.consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
			.consistOf(LockerIsActiveValidator.of(locker.getIsActive()))
			.validate();

		locker.activate();

		return Optional.of(locker);
	}
}
