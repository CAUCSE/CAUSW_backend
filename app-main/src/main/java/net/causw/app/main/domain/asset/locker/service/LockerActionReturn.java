package net.causw.app.main.domain.asset.locker.service;

import static net.causw.global.constant.StaticValue.LOCKER_ACCESS;

import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.util.LockerAccessValidator;
import net.causw.app.main.domain.etc.textfield.service.CommonService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.util.UserRoleValidator;
import net.causw.app.main.shared.ValidatorBucket;
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
		CommonService commonService) {
		if (locker.getUser().isEmpty()) {
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.LOCKER_UNUSED);
		}

		if (!user.getId().equals(locker.getUser().get().getId()))
			ValidatorBucket.of()
				.consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
				.validate();

		// 반납도 신청 기한에만 가능
		if (!user.getRoles().contains(Role.ADMIN))
			ValidatorBucket.of()
				.consistOf(LockerAccessValidator.of(commonService.findByKeyInFlag(LOCKER_ACCESS).orElse(false)))
				.validate();

		locker.returnLocker();

		return Optional.of(locker);
	}
}
