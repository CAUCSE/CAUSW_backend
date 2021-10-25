package net.causw.application.spi;

import net.causw.domain.model.LockerDomainModel;

import java.util.List;
import java.util.Optional;

public interface LockerPort {
    Optional<LockerDomainModel> findById(String id);

    List<LockerDomainModel> findByLocationId(String locationId);

    Long getEnableLockerCountByLocation(String locationId);

    Long getLockerCountByLocation(String locationId);
}
