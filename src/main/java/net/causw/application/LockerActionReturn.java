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
public class LockerActionReturn implements LockerAction{
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel lockerUserDomainModel,
            UserDomainModel updaterDomainModel,
            Validator validator,
            LockerPort lockerPort

    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        if (lockerUserDomainModel == null) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사용 중인 사물함이 아닙니다."
            );
        } else if (!lockerUserDomainModel.equals(updaterDomainModel)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)));
        }

        lockerDomainModel.setUser(null);

        validatorBucket
                .consistOf(ConstraintValidator.of(lockerDomainModel, validator))
                .validate();

        return lockerPort.update(lockerDomainModel.getId(), lockerDomainModel);
    }
}
