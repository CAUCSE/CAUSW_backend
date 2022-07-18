package net.causw.application.spi;

import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.util.List;
import java.util.Optional;

public interface CirclePort {
    Optional<CircleDomainModel> findById(String id);

    Optional<CircleDomainModel> findByLeaderId(String leaderId);

    List<CircleDomainModel> findAll();

    Optional<CircleDomainModel> findByName(String name);

    CircleDomainModel create(CircleDomainModel circleDomainModel);

    Optional<CircleDomainModel> update(String id, CircleDomainModel circleDomainModel);

    Optional<CircleDomainModel> updateLeader(String id, UserDomainModel newLeader);

    Optional<CircleDomainModel> delete(String id);

    Optional<CircleDomainModel> restore(String id);
}
