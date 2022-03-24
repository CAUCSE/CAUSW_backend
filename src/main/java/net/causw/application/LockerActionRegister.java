package net.causw.application;

import lombok.NoArgsConstructor;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.LockerIsDeactivatedValidator;
import net.causw.domain.validation.TimePassedValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.Optional;

@NoArgsConstructor
public class LockerActionRegister implements LockerAction {
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel updaterDomainModel,
            LockerPort lockerPort,
            LockerLogPort lockerLogPort
    ) {
        ValidatorBucket validator = ValidatorBucket.of();

        if (lockerDomainModel.getUser().isPresent()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "이미 사용 중인 사물함입니다."
            );
        }

        validator.consistOf(LockerIsDeactivatedValidator.of(lockerDomainModel.getIsActive()));

        lockerLogPort.whenRegister(updaterDomainModel).ifPresentOrElse(
                createdAt -> validator
                        .consistOf(TimePassedValidator.of(createdAt))
                        .validate(),
                validator::validate);

        if (!updaterDomainModel.getRole().equals(Role.ADMIN)) {
            lockerPort.findByUserId(updaterDomainModel.getId()).ifPresent(locker -> {
                locker.returnLocker();
                lockerPort.update(
                        locker.getId(),
                        locker
                );

                lockerLogPort.create(
                        locker.getLockerNumber(),
                        updaterDomainModel,
                        LockerLogAction.RETURN,
                        ""
                );
            });
        }

        lockerDomainModel.register(updaterDomainModel);

        return lockerPort.update(
                lockerDomainModel.getId(),
                lockerDomainModel
        );
    }
}
