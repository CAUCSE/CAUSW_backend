package net.causw.application.spi;

import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.util.Optional;

public interface CirclePort {
    Optional<CircleDomainModel> findById(String id);

    Optional<CircleDomainModel> findByLeaderId(String leaderId);

    Optional<CircleDomainModel> findByName(String name);

    CircleDomainModel create(CircleDomainModel circleDomainModel, UserDomainModel leader);

    Optional<CircleDomainModel> updateLeader(String id, UserDomainModel newLeader);
}
