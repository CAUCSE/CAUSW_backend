package net.causw.application.spi;

import net.causw.domain.model.board.BoardDomainModel;

import java.util.List;
import java.util.Optional;

public interface BoardPort {
    Optional<BoardDomainModel> findById(String id);

    List<BoardDomainModel> findAll();

    Optional<BoardDomainModel> findAppNotice();

    List<BoardDomainModel> findByCircleId(String circleId);

    List<BoardDomainModel> findBasicBoards();

    BoardDomainModel create(BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> update(String id, BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> delete(String id);

    Optional<BoardDomainModel> restore(String id);
}
