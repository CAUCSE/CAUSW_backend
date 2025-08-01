package net.causw.app.main.service.locker;

import static net.causw.global.constant.StaticValue.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.validation.ExtendLockerExpiredAtValidator;
import net.causw.app.main.domain.validation.LockerExtendAccessValidator;
import net.causw.app.main.domain.validation.UserRoleValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.service.common.CommonService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LockerActionExtend implements LockerAction {
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

		// 사물함 보유자와 신청자가 같은지 확인
		if (!user.getId().equals(locker.getUser().get().getId()))
			ValidatorBucket.of()
				.consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
				.validate();
		// 연장 신청 기간인지 확인
		if (!user.getRoles().contains(Role.ADMIN)) {
			ValidatorBucket.of()
				.consistOf(LockerExtendAccessValidator.of(commonService.findByKeyInFlag(LOCKER_EXTEND).orElse(false)))
				.validate();
		}
		// 연장일 확인
		LocalDateTime expiredAtToExtend = LocalDateTime.parse(
			commonService.findByKeyInTextField(StaticValue.EXPIRED_AT).orElseThrow(
				() -> new InternalServerException(
					ErrorCode.INTERNAL_SERVER,
					MessageUtil.LOCKER_RETURN_TIME_NOT_SET
				)
			), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

		Optional.ofNullable(locker.getExpireDate()).ifPresent(expiredAt ->
			ValidatorBucket.of()
				.consistOf(ExtendLockerExpiredAtValidator.of(
					expiredAt,
					expiredAtToExtend))
				.validate());

		locker.extendExpireDate(expiredAtToExtend);
		return Optional.of(locker);
	}
}
