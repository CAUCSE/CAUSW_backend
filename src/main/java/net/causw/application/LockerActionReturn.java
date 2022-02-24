package net.causw.application;

import lombok.NoArgsConstructor;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionReturn implements LockerAction{
    @Override
    public Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel updaterDomainModel,
            LockerPort lockerPort,
            LockerLogPort lockerLogPort
    ) {
        if (lockerDomainModel.getUser().isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사용 중인 사물함이 아닙니다."
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
