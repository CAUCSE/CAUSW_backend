package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.BoardRepository;
import net.causw.application.spi.BoardPort;
import net.causw.domain.model.BoardDomainModel;
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
    public List<BoardDomainModel> findAll() {
        return this.boardRepository.findByCircle_IdIsNullAndIsDeletedIsFalseOrderByCreatedAtAsc()
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
    public List<BoardDomainModel> findOldest3Boards() {
        return this.boardRepository.findTop3ByCircle_IdIsNullAndIsDeletedIsFalseOrderByCreatedAtAsc()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardDomainModel> findHomeBoards() {
        return this.boardRepository.findHomeBoards()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public BoardDomainModel create(BoardDomainModel boardDomainModel) {
        return this.entityToDomainModel(this.boardRepository.save(Board.from(boardDomainModel)));
    }

    @Override
    public Optional<BoardDomainModel> update(String id, BoardDomainModel boardDomainModel) {
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
    public Optional<BoardDomainModel> delete(String id) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setIsDeleted(true);

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }
}
