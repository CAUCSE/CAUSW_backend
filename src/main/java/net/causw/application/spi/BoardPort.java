package net.causw.application.spi;

import net.causw.domain.model.board.BoardDomainModel;

import java.util.List;
import java.util.Optional;

public interface BoardPort {
    Optional<BoardDomainModel> findById(String id);

    List<BoardDomainModel> findAllBoard(List<String> circleIdList);
    List<BoardDomainModel> findAllBoard();
    List<BoardDomainModel> findAllBoard(boolean isDeleted);

    Optional<BoardDomainModel> findAppNotice();

    List<BoardDomainModel> findByCircleId(String circleId);


    BoardDomainModel createBoard(BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> updateBoard(String id, BoardDomainModel boardDomainModel);

    Optional<BoardDomainModel> deleteBoard(String id);

    Optional<BoardDomainModel> restoreBoard(String id);

    List<BoardDomainModel> deleteAllCircleBoard(String circleId);

}
