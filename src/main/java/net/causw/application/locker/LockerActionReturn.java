package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.application.spi.TextFieldPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.locker.LockerDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionReturn implements LockerAction {
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel updaterDomainModel,
            LockerPort lockerPort,
            LockerLogPort lockerLogPort,
            FlagPort flagPort,
            TextFieldPort textFieldPort
    ) {
        if (lockerDomainModel.getUser().isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.LOCKER_UNUSED
            );
        }

        if (!updaterDomainModel.getId().equals(lockerDomainModel.getUser().get().getId()))
            ValidatorBucket.of()
                    .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                    .validate();

        lockerDomainModel.returnLocker();

        return lockerPort.update(
                lockerDomainModel.getId(),
                lockerDomainModel
        );
    }
}
