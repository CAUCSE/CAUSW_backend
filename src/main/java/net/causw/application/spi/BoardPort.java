package net.causw.application.spi;

import net.causw.domain.model.board.BoardDomainModel;

import java.util.List;
import java.util.Optional;

public interface BoardPort {
    Optional<BoardDomainModel> findById(String id);

    List<BoardDomainModel> findAllBoard();

    Optional<BoardDomainModel> findAppNotice();

    List<BoardDomainModel> findByCircleId(String circleId);

    List<BoardDomainModel> findBasicBoards();

    BoardDomainModel createBoard(BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> updateBoard(String id, BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> deleteBoard(String id);

    Optional<BoardDomainModel> restoreBoard(String id);
}
