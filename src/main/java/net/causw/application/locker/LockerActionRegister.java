package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.LockerLogAction;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.LockerAccessValidator;
import net.causw.domain.validation.LockerInUseValidator;
import net.causw.domain.validation.LockerIsDeactivatedValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static net.causw.domain.model.util.StaticValue.LOCKER_ACCESS;

@NoArgsConstructor
public class LockerActionRegister implements LockerAction {
    @Override
    public Optional<Locker> updateLocker(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ) {
        new LockerInUseValidator().validate(locker.getUser().isPresent());
        new LockerIsDeactivatedValidator().validate(locker.getIsActive());

        if (!user.getRoles().contains(Role.ADMIN)) {
            new LockerAccessValidator().validate(commonService.findByKeyInFlag(LOCKER_ACCESS).orElse(false));

            lockerService.findByUserId(user.getId()).ifPresent(existingLocker -> {
                existingLocker.returnLocker();
                LockerLog lockerLog = LockerLog.of(
                        existingLocker.getLockerNumber(),
                        existingLocker.getLocation().getName(),
                        user.getEmail(),
                        user.getName(),
                        LockerLogAction.RETURN,
                        ""
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
