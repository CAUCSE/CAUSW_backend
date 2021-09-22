package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.BoardRepository;
import net.causw.adapter.persistence.Circle;
import net.causw.application.spi.BoardPort;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BoardPortImpl implements BoardPort {
    private final BoardRepository boardRepository;

    public BoardPortImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<BoardDomainModel> findById(String id) {
        return this.boardRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public BoardDomainModel create(BoardDomainModel boardDomainModel, Optional<CircleDomainModel> circleDomainModel) {
        Circle circle = circleDomainModel.map(Circle::from).orElse(null);

        return this.entityToDomainModel(this.boardRepository.save(Board.of(
                boardDomainModel.getName(),
                boardDomainModel.getDescription(),
                boardDomainModel.getCreateRoleList().stream().map(Object::toString).collect(Collectors.joining(",")),
                boardDomainModel.getModifyRoleList().stream().map(Object::toString).collect(Collectors.joining(",")),
                boardDomainModel.getReadRoleList().stream().map(Object::toString).collect(Collectors.joining(",")),
                boardDomainModel.getIsDeleted(),
                circle
        )));
    }

    @Override
    public Optional<BoardDomainModel> update(String id, BoardDomainModel boardDomainModel) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setName(boardDomainModel.getName());
                    srcBoard.setDescription(boardDomainModel.getDescription());
                    srcBoard.setCreateRoles(boardDomainModel.getCreateRoleList().stream().map(Object::toString).collect(Collectors.joining(",")));
                    srcBoard.setModifyRoles(boardDomainModel.getModifyRoleList().stream().map(Object::toString).collect(Collectors.joining(",")));
                    srcBoard.setReadRoles(boardDomainModel.getReadRoleList().stream().map(Object::toString).collect(Collectors.joining(",")));

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }

    private BoardDomainModel entityToDomainModel(Board board) {
        return BoardDomainModel.of(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getModifyRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getReadRoles().split(","))),
                board.getIsDeleted(),
                board.getCircle().getId()
        );
    }
}
