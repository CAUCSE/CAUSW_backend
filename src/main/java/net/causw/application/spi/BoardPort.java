package net.causw.application.spi;

import net.causw.domain.model.BoardDomainModel;

import java.util.List;
import java.util.Optional;

public interface BoardPort {
    Optional<BoardDomainModel> findById(String id);

    List<BoardDomainModel> findAll();

    List<BoardDomainModel> findByCircleId(String circleId);

    BoardDomainModel create(BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> update(String id, BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> delete(String id);
}
