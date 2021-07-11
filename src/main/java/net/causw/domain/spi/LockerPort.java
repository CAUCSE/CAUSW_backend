package net.causw.domain.spi;

import net.causw.domain.model.LockerDomainModel;

public interface LockerPort {
    LockerDomainModel findById(String id);

    LockerDomainModel findByLockerNumber(Long lockerNumber);
}
