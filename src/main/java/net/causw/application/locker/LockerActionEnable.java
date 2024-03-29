package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.application.spi.TextFieldPort;
import net.causw.domain.model.locker.LockerDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.LockerIsActiveValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionEnable implements LockerAction {
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel updaterDomainModel,
            LockerPort lockerPort,
            LockerLogPort lockerLogPort,
            FlagPort flagPort,
            TextFieldPort textFieldPort
    ) {
        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(LockerIsActiveValidator.of(lockerDomainModel.getIsActive()))
                .validate();

        lockerDomainModel.activate();

        return lockerPort.update(lockerDomainModel.getId(), lockerDomainModel);
    }
}
