package net.causw.app.main.service.locker;

import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.validation.UserRoleValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.service.common.CommonService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LockerActionReturn implements LockerAction {
	@Override
	public Optional<Locker> updateLockerDomainModel(
		Locker locker,
		User user,
		LockerService lockerService,
		CommonService commonService
	) {
		if (locker.getUser().isEmpty()) {
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.LOCKER_UNUSED
			);
		}

		if (!user.getId().equals(locker.getUser().get().getId()))
			ValidatorBucket.of()
				.consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
				.validate();

		locker.returnLocker();

		return Optional.of(locker);
	}
}
