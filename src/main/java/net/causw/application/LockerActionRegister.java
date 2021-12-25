package net.causw.application;

import lombok.NoArgsConstructor;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.LockerIsDeactivatedValidator;
import net.causw.domain.validation.ValidatorBucket;

import javax.validation.Validator;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionRegister implements LockerAction {
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel ownerDomainModel,
            UserDomainModel updaterDomainModel,
            Validator validator,
            LockerPort lockerPort
    ) {
        if (ownerDomainModel != null) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "이미 사용 중인 사물함입니다."
            );
        }

        ValidatorBucket.of()
                .consistOf(LockerIsDeactivatedValidator.of(lockerDomainModel.getIsActive()))
                .validate();

        lockerDomainModel.register(updaterDomainModel);

        return lockerPort.update(
                lockerDomainModel.getId(),
                lockerDomainModel
        );
    }
}
