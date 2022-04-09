package net.causw.application.spi;

import net.causw.domain.model.LockerDomainModel;

import java.util.List;
import java.util.Optional;

public interface LockerPort {
    Optional<LockerDomainModel> findById(String id);

    Optional<LockerDomainModel> findByLockerNumber(Long lockerNumber);

    Optional<LockerDomainModel> findByUserId(String userId);

    LockerDomainModel create(LockerDomainModel lockerDomainModel);

    Optional<LockerDomainModel> update(String id, LockerDomainModel lockerDomainModel);

    Optional<LockerDomainModel> updateLocation(String id, LockerDomainModel lockerDomainModel);

    void delete(LockerDomainModel lockerDomainModel);

    List<LockerDomainModel> findByLocationId(String locationId);

    Long countEnableLockerByLocation(String locationId);

    Long countByLocation(String locationId);
}
