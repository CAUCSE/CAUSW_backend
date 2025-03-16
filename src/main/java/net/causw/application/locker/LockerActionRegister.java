package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.locker.LockerLogAction;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.LockerAccessValidator;
import net.causw.domain.validation.LockerInUseValidator;
import net.causw.domain.validation.LockerIsDeactivatedValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static net.causw.domain.model.util.StaticValue.LOCKER_ACCESS;

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
                                MessageUtil.LOCKER_RETURN_TIME_NOT_SET
                        )
                ), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        );

        return Optional.of(locker);
    }
}
