package net.causw.app.main.domain.asset.locker.service.v1;

import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.util.LockerIsActiveValidator;
import net.causw.app.main.domain.etc.textfield.service.v1.CommonService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.util.UserRoleValidator;
import net.causw.app.main.shared.ValidatorBucket;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LockerActionEnable implements LockerAction {
	@Override
	public Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerV1Service lockerV1Service,
		CommonService commonService) {
		ValidatorBucket.of()
			.consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
			.consistOf(LockerIsActiveValidator.of(locker.getIsActive()))
			.validate();

		locker.activate();

		return Optional.of(locker);
	}
}
