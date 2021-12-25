package net.causw.application;

import lombok.NoArgsConstructor;
import net.causw.application.spi.LockerPort;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.LockerIsActiveValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionEnable implements LockerAction {
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel lockerUserDomainModel,
            UserDomainModel updaterDomainModel,
            Validator validator,
            LockerPort lockerPort
    ) {
        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(LockerIsActiveValidator.of(lockerDomainModel.getIsActive()))
                .validate();

        lockerDomainModel.activate();

        System.out.println(lockerPort.update(lockerDomainModel.getId(), lockerDomainModel));

        return lockerPort.update(lockerDomainModel.getId(), lockerDomainModel);
    }
}
