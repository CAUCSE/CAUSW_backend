package net.causw.app.main.domain.asset.locker.service.v1;

import static net.causw.global.constant.StaticValue.LOCKER_ACCESS;

import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.service.v1.validators.LockerAccessValidator;
import net.causw.app.main.domain.etc.textfield.service.v1.CommonService;
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
		LockerV1Service lockerV1Service,
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

		locker.returnLockerV1();

		return Optional.of(locker);
	}
}
