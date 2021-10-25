package net.causw.application.spi;

import net.causw.domain.model.LockerLocationDomainModel;

import java.util.List;
import java.util.Optional;

public interface LockerLocationPort {
    Optional<LockerLocationDomainModel> findById(String id);

    List<LockerLocationDomainModel> findAll();
}
