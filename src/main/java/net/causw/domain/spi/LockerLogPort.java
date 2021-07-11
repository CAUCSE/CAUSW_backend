package net.causw.domain.spi;

import net.causw.domain.model.LockerLogDomainModel;

public interface LockerLogPort {
    LockerLogDomainModel findById(String id);
}
