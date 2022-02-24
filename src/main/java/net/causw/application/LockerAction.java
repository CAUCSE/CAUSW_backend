package net.causw.application;

import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.UserDomainModel;

import javax.validation.Validator;
import java.util.Optional;

public interface LockerAction {
    Optional<LockerDomainModel> updateLockerDomainModel(
            LockerDomainModel lockerDomainModel,
            UserDomainModel updaterDomainModel,
            LockerPort lockerPort,
            LockerLogPort lockerLogPort
    );
}
