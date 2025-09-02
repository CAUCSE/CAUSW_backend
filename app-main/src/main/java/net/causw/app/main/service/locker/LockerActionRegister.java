package net.causw.app.main.service.locker;

import lombok.NoArgsConstructor;
import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.locker.LockerLog;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.service.common.CommonService;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.validation.LockerAccessValidator;
import net.causw.app.main.domain.validation.LockerInUseValidator;
import net.causw.app.main.domain.validation.LockerIsDeactivatedValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.causw.global.constant.StaticValue.LOCKER_ACCESS;

@NoArgsConstructor
public class LockerActionRegister implements LockerAction {
    @Override
    public Optional<Locker> updateLockerDomainModel(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ) {
        ValidatorBucket.of()
                .consistOf(LockerInUseValidator.of(locker.getUser().isPresent()))
                .consistOf(LockerIsDeactivatedValidator.of(locker.getIsActive()))
                .validate();

        if (!user.getRoles().contains(Role.ADMIN)) {
            ValidatorBucket.of()
                    // FIXME : 추후 Flag 대신 날짜 검증 로직으로 일괄 변경 요망
                    .consistOf(LockerAccessValidator.of(commonService.findByKeyInFlag(LOCKER_ACCESS).orElse(false)))
                    .validate();

            lockerService.findByUserId(user.getId()).ifPresent(existingLocker -> {
                lockerService.returnAndSaveLocker(existingLocker);
                LockerLog lockerLog = LockerLog.of(
                        existingLocker.getLockerNumber(),
                        existingLocker.getLocation().getName(),
                        user.getEmail(),
                        user.getName(),
                        LockerLogAction.RETURN,
                        "사물함 반납"
                );
            });
        }

        locker.register(
                user,
                LocalDateTime.parse(commonService.findByKeyInTextField(StaticValue.EXPIRED_AT).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.LOCKER_EXPIRE_TIME_NOT_SET
                        )
                ), StaticValue.LOCKER_DATE_TIME_FORMATTER)
        );

        return Optional.of(locker);
    }
}
