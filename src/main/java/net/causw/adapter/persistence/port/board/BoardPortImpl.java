package net.causw.adapter.persistence.port.board;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.repository.BoardRepository;
import net.causw.application.spi.BoardPort;
import net.causw.domain.model.board.BoardDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BoardPortImpl extends DomainModelMapper implements BoardPort {
    private final BoardRepository boardRepository;

    public BoardPortImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<BoardDomainModel> findById(String id) {
        return this.boardRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public List<BoardDomainModel> findAllBoard() {
        return this.boardRepository.findByIsDeletedIsFalseOrderByCreatedAtAsc()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BoardDomainModel> findAppNotice() {
        return this.boardRepository.findAppNotice().map(this::entityToDomainModel);
    }

    @Override
    public List<BoardDomainModel> findByCircleId(String circleId) {
        return this.boardRepository.findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(circleId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardDomainModel> findBasicBoards() {
        return this.boardRepository.findBasicBoards()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public BoardDomainModel createBoard(BoardDomainModel boardDomainModel) {
        return this.entityToDomainModel(this.boardRepository.save(Board.from(boardDomainModel)));
    }

    @Override
    public Optional<BoardDomainModel> updateBoard(String id, BoardDomainModel boardDomainModel) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setName(boardDomainModel.getName());
                    srcBoard.setDescription(boardDomainModel.getDescription());
                    srcBoard.setCreateRoles(String.join(",", boardDomainModel.getCreateRoleList()));

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }

    @Override
    public Optional<BoardDomainModel> deleteBoard(String id) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setIsDeleted(true);

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }

    @Override
    public Optional<BoardDomainModel> restoreBoard(String id) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setIsDeleted(false);

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }
}
