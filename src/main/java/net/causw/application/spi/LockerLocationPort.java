package net.causw.application.spi;

import net.causw.domain.model.LockerLocationDomainModel;

import java.util.List;
import java.util.Optional;

public interface LockerLocationPort {
    Optional<LockerLocationDomainModel> findById(String id);

    Optional<LockerLocationDomainModel> findByName(String name);

    List<LockerLocationDomainModel> findAll();

    LockerLocationDomainModel create(LockerLocationDomainModel lockerLocationDomainModel);

    Optional<LockerLocationDomainModel> update(String id, LockerLocationDomainModel lockerLocationDomainModel);

    void delete(LockerLocationDomainModel lockerLocationDomainModel);
}
