package net.causw.application;

import lombok.NoArgsConstructor;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
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
        if (lockerDomainModel.getIsActive()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "이미 사용 가능한 사물함입니다."
            );
        }

        lockerDomainModel = LockerDomainModel.of(
                lockerDomainModel.getId(),
                lockerDomainModel.getLockerNumber(),
                true,
                null,
                lockerDomainModel.getUser().orElse(null),
                lockerDomainModel.getLockerLocation()
        );


        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerDomainModel, validator))
                .validate();

        return lockerPort.update(lockerDomainModel.getId(), lockerDomainModel);
    }
}
