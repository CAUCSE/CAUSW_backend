package net.causw.application.spi;

import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;

import java.util.Optional;

public interface BoardPort {
    Optional<BoardDomainModel> findById(String id);
    BoardDomainModel create(BoardDomainModel boardDomainModel, Optional<CircleDomainModel> circleDomainModel);
    Optional<BoardDomainModel> update(String id, BoardDomainModel boardDomainModel);
    Optional<BoardDomainModel> delete(String id);
}
